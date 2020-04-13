package markets;

import com.google.common.collect.ImmutableMap;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.Account;
import com.oanda.v20.account.AccountGetResponse;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.InstrumentPriceResponse;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.pricing_common.Price;
import com.oanda.v20.pricing_common.PriceValue;
import com.oanda.v20.primitives.Instrument;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.transaction.OrderCancelTransaction;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Collections.singleton;

public class RunIt {

    private static final Logger LOG = LoggerFactory.getLogger(RunIt.class);

    private static final InstrumentName EURUSD = new InstrumentName("EUR_USD");

    public static void main(String[] args) throws IOException {
        Context context = new ContextBuilder("https://api-fxpractice.oanda.com")
                .setApplication("")
                .setToken("5e02279e018a8b4e9869ea5fb17bfc88-eb15bcedffb3eb62fc26252a2d3f2841")
                .build();

        List<String> accountIds = Arrays.asList(
                "101-001-14085577-007", // Coin Toss 50/100
                "101-001-14085577-008", // Coin Toss 50/150
                "101-001-14085577-009", // Coin Toss 100/200
                "101-001-14085577-010" // Coin Toss 100/300
        );

        Map<String, List<Double>> targets = ImmutableMap.of(
                "101-001-14085577-007", Arrays.asList(0.0050, 0.0100),
                "101-001-14085577-008", Arrays.asList(0.0050, 0.0150),
                "101-001-14085577-009", Arrays.asList(0.0100, 0.0200),
                "101-001-14085577-010", Arrays.asList(0.0100, 0.0300)
        );

        for (String id : accountIds) {
            AccountID accountId = new AccountID(id);
            List<Double> slTp = targets.get(id);
            BigDecimal stopLossPips = BigDecimal.valueOf(slTp.get(0));
            BigDecimal takeProfitPips = BigDecimal.valueOf(slTp.get(1));
            AccountGetResponse accountGetResponse;

            try {
                accountGetResponse = context.account.get(accountId);
            } catch (Exception e) {
                LOG.error("{}: Error accessing account!", id, e);
                continue;
            }

            Account account = accountGetResponse.getAccount();

            int openPositions = account.getOpenPositionCount().intValue();
            if (openPositions > 0) {
                LOG.info("{} already has a position open.", accountId);
                continue;
            }

            PricingGetResponse pricingGetResponse;
            try {
                pricingGetResponse = context.pricing.get(new PricingGetRequest(accountId, singleton(EURUSD)));
            } catch (Exception e) {
                LOG.error("{}: Error accessing pricing!", id, e);
                continue;
            }

            boolean shortInstrument = new Random().nextBoolean();
            PriceValue price = shortInstrument ? pricingGetResponse.getPrices().iterator().next().getCloseoutAsk() :
                    pricingGetResponse.getPrices().iterator().next().getCloseoutBid();

            BigDecimal balanceDollars = account.getBalance().bigDecimalValue();
            int units = balanceDollars
                    .multiply(BigDecimal.valueOf(0.005)) // Risk half a percent
                    .divide(stopLossPips, 4, HALF_DOWN)
                    .intValue();

            OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountId);
            orderCreateRequest.setOrder(new MarketOrderRequest()
                    .setInstrument(EURUSD)
                    .setUnits(shortInstrument ? -units : units)
                    .setStopLossOnFill(new StopLossDetails()
                            .setPrice(shortInstrument ? price.bigDecimalValue().add(stopLossPips) :
                                    price.bigDecimalValue().subtract(stopLossPips))
                    )
                    .setTakeProfitOnFill(new TakeProfitDetails()
                            .setPrice(shortInstrument ? price.bigDecimalValue().subtract(takeProfitPips) :
                                    price.bigDecimalValue().add(takeProfitPips))
                    )
            );

            try {
                OrderCreateResponse orderCreateResponse = context.order.create(orderCreateRequest);
                OrderCancelTransaction cancelTransaction = orderCreateResponse.getOrderCancelTransaction();
                if (cancelTransaction != null) {
                    LOG.error("{}: Unable to open order: {}", id, cancelTransaction.getReason());
                }
            } catch (Exception e) {
                LOG.error("Error creating order!", e);
                continue;
            }
        }
    }

}
