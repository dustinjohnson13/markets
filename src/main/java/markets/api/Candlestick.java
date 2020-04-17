package markets.api;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;

public class Candlestick {
    private final LocalDateTime utcTime;
    private final CandlestickData bid;
    private final CandlestickData ask;

    public Candlestick(LocalDateTime utcTime, CandlestickData bid, CandlestickData ask) {
        this.utcTime = utcTime;
        this.bid = bid;
        this.ask = ask;
    }

    public LocalDateTime getUtcTime() {
        return utcTime;
    }

    public CandlestickData getBid() {
        return bid;
    }

    public CandlestickData getAsk() {
        return ask;
    }

    public boolean isGreen() {
        return bid.isGreen();
    }

    public boolean isRed() {
        return bid.isRed();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("time", utcTime)
                .add("bid", bid)
                .add("ask", ask)
                .toString();
    }

}
