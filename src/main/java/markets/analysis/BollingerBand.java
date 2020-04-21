package markets.analysis;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Objects;

public class BollingerBand {
    private final BigDecimal upperBand;
    private final BigDecimal middleBand;
    private final BigDecimal lowerBand;

    public BollingerBand(BigDecimal upperBand, BigDecimal middleBand, BigDecimal lowerBand) {
        this.upperBand = upperBand;
        this.middleBand = middleBand;
        this.lowerBand = lowerBand;
    }

    public BigDecimal getUpperBand() {
        return upperBand;
    }

    public BigDecimal getMiddleBand() {
        return middleBand;
    }

    public BigDecimal getLowerBand() {
        return lowerBand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BollingerBand that = (BollingerBand) o;
        return Objects.equals(upperBand, that.upperBand) &&
                Objects.equals(middleBand, that.middleBand) &&
                Objects.equals(lowerBand, that.lowerBand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upperBand, middleBand, lowerBand);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("upperBand", upperBand)
                .add("middleBand", middleBand)
                .add("lowerBand", lowerBand)
                .toString();
    }
}
