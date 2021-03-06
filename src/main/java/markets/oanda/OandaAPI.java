package markets.oanda;

import com.google.common.collect.Range;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.transaction.OrderCancelTransaction;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Candlestick;
import markets.api.CandlestickData;
import markets.api.Instrument;
import markets.api.Price;
import markets.api.RequestException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

public class OandaAPI implements BrokerAPI {

    private static final String ENDPOINT = "https://api-fxpractice.oanda.com";

    private final Context context;

    private OandaAPI(String oandaPracticeApiToken, String oandaLiveApiToken) {
        context = new ContextBuilder(ENDPOINT)
                .setApplication("")
                .setToken(oandaPracticeApiToken)
                .build();
    }

    public static BrokerAPI create(String oandaPracticeApiToken, String oandaLiveApiToken) {
        return new OandaAPI(oandaPracticeApiToken, oandaLiveApiToken);
    }

    @Override
    public Account account(String id) throws RequestException {
        try {
            com.oanda.v20.account.Account oandaAccount = context.account.get(new AccountID(id)).getAccount();
            return new Account(id, oandaAccount.getOpenPositionCount().intValue(), oandaAccount.getBalance().bigDecimalValue());
        } catch (Exception e) {
            throw new RequestException("Unable to retrieve account " + id, e);
        }
    }

    @Override
    public Price price(String accountId, Instrument instrument) throws RequestException {
        try {
            PricingGetResponse response = context.pricing.get(new PricingGetRequest(new AccountID(accountId),
                    singleton(new InstrumentName(instrument.getSymbol()))));
            ClientPrice oandaPrice = response.getPrices().iterator().next();
            return new Price(oandaPrice.getCloseoutBid().bigDecimalValue(), oandaPrice.getCloseoutAsk().bigDecimalValue());
        } catch (Exception e) {
            throw new RequestException("Unable to retrieve prices", e);
        }
    }

    @Override
    public void marketOrder(String accountId, Instrument instrument, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(new AccountID(accountId));
        orderCreateRequest.setOrder(new MarketOrderRequest()
                .setInstrument(new InstrumentName(instrument.getSymbol()))
                .setUnits(units)
                .setStopLossOnFill(new StopLossDetails()
                        .setPrice(stopLoss)
                )
                .setTakeProfitOnFill(new TakeProfitDetails()
                        .setPrice(takeProfit)
                )
        );

        try {
            OrderCreateResponse orderCreateResponse = context.order.create(orderCreateRequest);
            OrderCancelTransaction cancelTransaction = orderCreateResponse.getOrderCancelTransaction();
            if (cancelTransaction != null) {
                throw new RequestException(String.format("Unable to open order: %s", cancelTransaction.getReason()),
                        new IllegalArgumentException(cancelTransaction.getReason().toString()));
            }
        } catch (RequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RequestException("Unable to open order", e);
        }
    }

    @Override
    public List<Candlestick> candles(Instrument instrument, LocalDateTime utcFrom, LocalDateTime utcTo) throws RequestException {

        long hours = Duration.between(utcFrom, utcTo).toHours();
        long wholeRequests = hours / 5000;
        long remainingHours = hours % 5000;

        List<Range<LocalDateTime>> requestRanges = new ArrayList<>();
        LocalDateTime requestStart = utcFrom;
        for (int i = 0; i < wholeRequests; i++) {
            LocalDateTime requestEnd = requestStart.plusHours(5000);
            requestRanges.add(Range.closed(requestStart, requestEnd));
            requestStart = requestEnd.plusHours(1);
        }
        requestRanges.add(Range.closed(requestStart, requestStart.plusHours(remainingHours)));

        List<InstrumentCandlesResponse> responses = new ArrayList<>(requestRanges.size());
        for (Range<LocalDateTime> requestRange : requestRanges) {
            responses.add(retrieveCandles(instrument, requestRange.lowerEndpoint(), requestRange.upperEndpoint()));
        }

        return responses.stream()
                .flatMap(it -> it.getCandles().stream())
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private InstrumentCandlesResponse retrieveCandles(Instrument instrument, LocalDateTime utcFrom, LocalDateTime utcTo) throws RequestException {

        String from = utcFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + ".0Z";
        String to = utcTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + ".0Z";

        try {
            InstrumentCandlesResponse response = context.instrument.candles(new InstrumentCandlesRequest(
                    new InstrumentName(instrument.getSymbol()))
                    .setAlignmentTimezone("America/New_York")
                    .setPrice("BA")
                    .setFrom(from)
                    .setTo(to)
                    .setGranularity(CandlestickGranularity.H1)
                    .setIncludeFirst(true)
            );
            return response;
        } catch (Exception e) {
            throw new RequestException("Unable to retrieve candles", e);
        }
    }

    private Candlestick convert(com.oanda.v20.instrument.Candlestick oandaVersion) {
        CandlestickData bid = convert(oandaVersion.getBid());
        CandlestickData ask = convert(oandaVersion.getAsk());

        LocalDateTime time = LocalDateTime.parse(oandaVersion.getTime().toString().replaceAll("\\..*", ""),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        return new Candlestick(time, bid, ask);
    }

    private CandlestickData convert(com.oanda.v20.instrument.CandlestickData oandaVersion) {
        return new CandlestickData(oandaVersion.getO().bigDecimalValue(), oandaVersion.getH().bigDecimalValue(),
                oandaVersion.getL().bigDecimalValue(), oandaVersion.getC().bigDecimalValue());
    }
}
