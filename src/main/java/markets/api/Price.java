package markets.api;

import java.math.BigDecimal;

public class Price {

    private final BigDecimal closeoutBid;
    private final BigDecimal closeoutAsk;

    public Price(BigDecimal closeoutBid, BigDecimal closeoutAsk) {
        this.closeoutBid = closeoutBid;
        this.closeoutAsk = closeoutAsk;
    }

    public BigDecimal getCloseoutBid() {
        return closeoutBid;
    }

    public BigDecimal getCloseoutAsk() {
        return closeoutAsk;
    }
}
