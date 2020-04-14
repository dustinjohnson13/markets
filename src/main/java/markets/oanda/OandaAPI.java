package markets.oanda;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountGetResponse;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.pricing_common.PriceValue;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.transaction.OrderCancelTransaction;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Price;
import markets.api.RequestException;

import java.math.BigDecimal;
import java.util.Random;

import static java.util.Collections.singleton;

public class OandaAPI implements BrokerAPI {

    private static final String ENDPOINT = "https://api-fxpractice.oanda.com";
    // TODO: Token should be injected
    private static final String TOKEN = "5e02279e018a8b4e9869ea5fb17bfc88-eb15bcedffb3eb62fc26252a2d3f2841";

    private final Context context;

    private OandaAPI() {
        context = new ContextBuilder(ENDPOINT)
                .setApplication("")
                .setToken(TOKEN)
                .build();
    }

    public static BrokerAPI create() {
        return new OandaAPI();
    }

    @Override
    public Account account(String id) throws RequestException {
        try {
            com.oanda.v20.account.Account oandaAccount = context.account.get(new AccountID(id)).getAccount();
            return new Account(oandaAccount.getOpenPositionCount().intValue(), oandaAccount.getBalance().bigDecimalValue());
        } catch (Exception e) {
            throw new RequestException("Unable to retrieve account " + id, e);
        }
    }

    @Override
    public Price price(String accountId, String symbol) throws RequestException {
        try {
            PricingGetResponse response = context.pricing.get(new PricingGetRequest(new AccountID(accountId),
                    singleton(new InstrumentName(symbol))));
            ClientPrice oandaPrice = response.getPrices().iterator().next();
            return new Price(oandaPrice.getCloseoutBid().bigDecimalValue(), oandaPrice.getCloseoutAsk().bigDecimalValue());
        } catch (Exception e) {
            throw new RequestException("Unable to retrieve prices", e);
        }
    }

    @Override
    public void marketOrder(String accountId, String symbol, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(new AccountID(accountId));
        orderCreateRequest.setOrder(new MarketOrderRequest()
                .setInstrument(new InstrumentName(symbol))
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
}
