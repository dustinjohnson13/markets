package markets;

import com.google.common.base.MoreObjects;

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
}
