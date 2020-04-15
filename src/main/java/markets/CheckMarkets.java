package markets;

import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.MarketClock;
import markets.api.Price;
import markets.api.RequestException;
import markets.oanda.OandaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.math.RoundingMode.HALF_DOWN;

public class CheckMarkets {

    private static final Logger LOG = LoggerFactory.getLogger(CheckMarkets.class);

    private static final String EUR_USD = "EUR_USD";
    private static final String GBP_USD = "GBP_USD";

    public void run(MarketClock clock) {

        LOG.info("Current time: {}", clock.nowUTCDateTime());

        BrokerAPI api = OandaAPI.create();

        List<String> accountIds = Arrays.asList(
                "101-001-14085577-002", // Coin Toss 50/100
                "101-001-14085577-003", // Coin Toss 50/150
                "101-001-14085577-004", // Coin Toss 100/200
                "101-001-14085577-005", // Coin Toss 100/300

                "101-001-14085577-007", // Coin Toss 50/100
                "101-001-14085577-008", // Coin Toss 50/150
                "101-001-14085577-009", // Coin Toss 100/200
                "101-001-14085577-010" // Coin Toss 100/300
        );

        Map<String, List<Double>> targets = new HashMap<String, List<Double>>() {{
            put("101-001-14085577-002", Arrays.asList(0.0050, 0.0100));
            put("101-001-14085577-003", Arrays.asList(0.0050, 0.0150));
            put("101-001-14085577-004", Arrays.asList(0.0100, 0.0200));
            put("101-001-14085577-005", Arrays.asList(0.0100, 0.0300));
            put("101-001-14085577-007", Arrays.asList(0.0050, 0.0100));
            put("101-001-14085577-008", Arrays.asList(0.0050, 0.0150));
            put("101-001-14085577-009", Arrays.asList(0.0100, 0.0200));
            put("101-001-14085577-010", Arrays.asList(0.0100, 0.0300));
        }};

        Map<String, String> instruments = new HashMap<String, String>() {{
            put("101-001-14085577-002", GBP_USD);
            put("101-001-14085577-003", GBP_USD);
            put("101-001-14085577-004", GBP_USD);
            put("101-001-14085577-005", GBP_USD);
            put("101-001-14085577-007", EUR_USD);
            put("101-001-14085577-008", EUR_USD);
            put("101-001-14085577-009", EUR_USD);
            put("101-001-14085577-010", EUR_USD);
        }};

        for (String id : accountIds) {
            String instrument = instruments.get(id);
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
                prices = api.price(id, instrument);
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
                api.marketOrder(id, instrument, orderUnits, stopLoss, takeProfit);
            } catch (RequestException e) {
                LOG.error("{}: Error opening order!", id, e);
            }
        }
    }

}
