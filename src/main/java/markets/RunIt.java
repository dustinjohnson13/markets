package markets;

import com.google.common.collect.ImmutableMap;
import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Price;
import markets.api.RequestException;
import markets.oanda.OandaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.math.RoundingMode.HALF_DOWN;

public class RunIt {

    private static final Logger LOG = LoggerFactory.getLogger(RunIt.class);

    private static final String EUR_USD = "EUR_USD";

    public static void main(String[] args) {

        BrokerAPI api = OandaAPI.create();

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
            List<Double> slTp = targets.get(id);
            BigDecimal stopLossPips = BigDecimal.valueOf(slTp.get(0));
            BigDecimal takeProfitPips = BigDecimal.valueOf(slTp.get(1));
            Account account;

            try {
                account = api.account(id);
            } catch (RequestException e) {
                LOG.error("{}: Error accessing account!", id, e);
                continue;
            }

            int openPositions = account.getOpenPositionsCount();
            if (openPositions > 0) {
                LOG.info("{} already has a position open.", id);
                continue;
            }

            Price prices;
            try {
                prices = api.price(id, EUR_USD);
            } catch (RequestException e) {
                LOG.error("{}: Error accessing pricing!", id, e);
                continue;
            }

            boolean shortInstrument = new Random().nextBoolean();
            BigDecimal price = shortInstrument ? prices.getCloseoutAsk() : prices.getCloseoutBid();

            BigDecimal balanceDollars = account.getBalance();
            int units = balanceDollars
                    .multiply(BigDecimal.valueOf(0.005)) // Risk half a percent
                    .divide(stopLossPips, 4, HALF_DOWN)
                    .intValue();

            int orderUnits = shortInstrument ? -units : units;
            BigDecimal stopLoss = shortInstrument ? price.add(stopLossPips) :
                    price.subtract(stopLossPips);
            BigDecimal takeProfit = shortInstrument ? price.subtract(takeProfitPips) :
                    price.add(takeProfitPips);

            try {
                api.marketOrder(id, EUR_USD, orderUnits, stopLoss, takeProfit);
            } catch (RequestException e) {
                LOG.error("{}: Error opening order!", id, e);
            }
        }
    }

}
