package markets.api;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

public class MarketClock {

    private final Clock clock;

    public MarketClock(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime nowUTCDateTime() {
        return ofInstant(clock.instant(), UTC)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

}
