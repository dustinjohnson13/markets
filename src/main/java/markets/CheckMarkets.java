package markets;

import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.MarketClock;
import markets.api.RequestException;
import markets.api.Trader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CheckMarkets {

    private static final Logger LOG = LoggerFactory.getLogger(CheckMarkets.class);

    public void run(MarketClock clock, BrokerAPI api, List<Trader> traders) {

        LOG.info("Current time: {}", clock.nowUTCDateTime());

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

            trader.update(account, api, clock);
        });
    }

}
