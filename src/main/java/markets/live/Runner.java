package markets.live;

import markets.CheckMarkets;
import markets.api.BrokerAPI;
import markets.api.MarketClock;
import markets.oanda.OandaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.Clock.systemUTC;

public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        MarketClock clock = new MarketClock(systemUTC());
        BrokerAPI api = OandaAPI.create();

        new CheckMarkets().run(clock, api);
    }

}
