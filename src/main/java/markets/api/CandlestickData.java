package markets.api;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;

public class CandlestickData {

    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;

    public CandlestickData(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("open", open)
                .add("high", high)
                .add("low", low)
                .add("close", close)
                .toString();
    }
}
