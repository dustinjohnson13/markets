package markets.live;

import markets.CheckMarkets;
import markets.api.BrokerAPI;
import markets.api.MarketClock;
import markets.api.Trader;
import markets.oanda.OandaAPI;
import markets.traders.CoinFlip;
import markets.traders.RandomBollingerBand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.time.Clock.systemUTC;
import static markets.api.Instrument.EUR_USD;
import static markets.api.Instrument.GBP_USD;

public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    private static final List<Trader> traders = Arrays.asList(
            new CoinFlip("101-001-14085577-002", "Coin Toss 50/100 (GBP)", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0100), GBP_USD),
            new CoinFlip("101-001-14085577-003", "Coin Toss 50/150 (GBP)", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0150), GBP_USD),
            new CoinFlip("101-001-14085577-004", "Coin Toss 100/200 (GBP)", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0200), GBP_USD),
            new CoinFlip("101-001-14085577-005", "Coin Toss 100/300 (GBP)", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0300), GBP_USD),

            new CoinFlip("101-001-14085577-007", "Coin Toss 50/100", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0100), EUR_USD),
            new CoinFlip("101-001-14085577-008", "Coin Toss 50/150", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0150), EUR_USD),
            new CoinFlip("101-001-14085577-009", "Coin Toss 100/200", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0200), EUR_USD),
            new CoinFlip("101-001-14085577-010", "Coin Toss 100/300", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0300), EUR_USD),

            new RandomBollingerBand("101-001-14085577-011", "Random Bollinger Band (EUR)", EUR_USD),
            new RandomBollingerBand("101-001-14085577-012", "Random Bollinger Band (GBP)", GBP_USD)
    );

    public static void main(String[] args) {
        MarketClock clock = new MarketClock(systemUTC());
        BrokerAPI api = OandaAPI.create();

        new CheckMarkets().run(clock, api, traders);
    }

}
