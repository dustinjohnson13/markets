package markets.live;

import markets.CheckMarkets;
import markets.api.MarketClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.Clock.systemUTC;

public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        MarketClock clock = new MarketClock(systemUTC());

        new CheckMarkets().run(clock);
    }

}
