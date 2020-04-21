package markets;

import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Instrument;
import markets.api.MarketClock;
import markets.api.RequestException;
import markets.api.Trader;
import markets.traders.CoinFlip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static markets.api.Instrument.EUR_USD;
import static markets.api.Instrument.GBP_USD;

public class CheckMarkets {

    private static final Logger LOG = LoggerFactory.getLogger(CheckMarkets.class);

    public void run(MarketClock clock, BrokerAPI api) {

        LOG.info("Current time: {}", clock.nowUTCDateTime());

        List<Trader> traders = Arrays.asList(
            new CoinFlip("101-001-14085577-002", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0100), GBP_USD),// Coin Toss 50/100
            new CoinFlip("101-001-14085577-003", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0150), GBP_USD),// Coin Toss 50/150
            new CoinFlip("101-001-14085577-004", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0200), GBP_USD),// Coin Toss 100/200
            new CoinFlip("101-001-14085577-005", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0300), GBP_USD),// Coin Toss 100/300

            new CoinFlip("101-001-14085577-007", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0100), EUR_USD),// Coin Toss 50/100
            new CoinFlip("101-001-14085577-008", BigDecimal.valueOf(0.0050), BigDecimal.valueOf(0.0150), EUR_USD),// Coin Toss 50/150
            new CoinFlip("101-001-14085577-009", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0200), EUR_USD),// Coin Toss 100/200
            new CoinFlip("101-001-14085577-010", BigDecimal.valueOf(0.0100), BigDecimal.valueOf(0.0300), EUR_USD)// Coin Toss 100/300
        );

        traders.forEach(trader -> {
            String id = trader.getId();
            Account account;

            try {
                account = api.account(id);
            } catch (RequestException e) {
                LOG.error("{}: Error accessing account!", id, e);
                return;
            }

            int openPositions = account.getOpenPositionsCount();
            if (openPositions > 0) {
                LOG.info("{} already has a position open.", id);
                return;
            }

            trader.update(account, api);
        });
    }

}
