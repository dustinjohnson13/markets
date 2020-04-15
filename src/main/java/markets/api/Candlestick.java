package markets.api;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;

public class Candlestick {
    private final CandlestickData bid;
    private final CandlestickData ask;
    private final LocalDateTime utcTime;

    public Candlestick(LocalDateTime utcTime, CandlestickData bid, CandlestickData ask) {
        this.utcTime = utcTime;
        this.bid = bid;
        this.ask = ask;
    }

    public CandlestickData getBid() {
        return bid;
    }

    public CandlestickData getAsk() {
        return ask;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bid", bid)
                .add("ask", ask)
                .add("time", utcTime)
                .toString();
    }
}
