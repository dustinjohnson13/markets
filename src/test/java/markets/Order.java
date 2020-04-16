package markets;

import com.google.common.base.MoreObjects;
import markets.api.Price;

import java.math.BigDecimal;
import java.util.Objects;

public class Order {
    private final String symbol;
    private final int units;
    private final BigDecimal price;
    private final BigDecimal stopLoss;
    private final BigDecimal takeProfit;

    public Order(String symbol, int units, BigDecimal price, BigDecimal stopLoss, BigDecimal takeProfit) {
        this.symbol = symbol;
        this.units = units;
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getUnits() {
        return units;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public BigDecimal getTakeProfit() {
        return takeProfit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return units == order.units &&
                Objects.equals(symbol, order.symbol) &&
                Objects.equals(price, order.price) &&
                Objects.equals(stopLoss, order.stopLoss) &&
                Objects.equals(takeProfit, order.takeProfit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, units, price, stopLoss, takeProfit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("units", units)
                .add("price", price)
                .add("stopLoss", stopLoss)
                .add("takeProfit", takeProfit)
                .toString();
    }

    public boolean isStoppedOut(Price price) {
        if (stopLoss == null) {
            return false;
        }

        // Buyers get ask, Sellers get bid price
        if (units > 0) {
            return price.getCloseoutBid().compareTo(stopLoss) <= 0;
        } else {
            return price.getCloseoutAsk().compareTo(stopLoss) >= 0;
        }
    }

    public boolean isTakeProfit(Price price) {
        if (takeProfit == null) {
            return false;
        }

        // Buyers get ask, Sellers get bid price
        if (units > 0) {
            return price.getCloseoutBid().compareTo(takeProfit) >= 0;
        } else {
            return price.getCloseoutAsk().compareTo(takeProfit) <= 0;
        }
    }

    public BigDecimal profitLoss(Price price) {
        if (isStoppedOut(price)) {
            if (getUnits() > 0) {
                return getStopLoss().subtract(getPrice())
                        .multiply(BigDecimal.valueOf(getUnits()));
            } else {
                return getStopLoss().subtract(getPrice())
                        .multiply(BigDecimal.valueOf(getUnits()));
            }
        }

        if (isTakeProfit(price)) {
            if (getUnits() > 0) {
                return getTakeProfit().subtract(getPrice())
                        .multiply(BigDecimal.valueOf(getUnits()));
            } else {
                return getTakeProfit().subtract( getPrice())
                        .multiply(BigDecimal.valueOf(getUnits()));
            }
        }

        return null;
    }
}
