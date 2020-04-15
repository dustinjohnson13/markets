package markets.api;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MarketClock {

    private final Clock clock;

    public MarketClock(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime nowUTCDateTime() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

}
