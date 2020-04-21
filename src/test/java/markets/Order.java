package markets;

import com.google.common.base.MoreObjects;
import markets.api.Candlestick;
import markets.api.CandlestickData;
import markets.api.Instrument;
import markets.api.Price;

import java.math.BigDecimal;
import java.util.Objects;

public class Order {
    private final Instrument instrument;
    private final int units;
    private final BigDecimal price;
    private final BigDecimal stopLoss;
    private final BigDecimal takeProfit;

    public Order(Instrument instrument, int units, BigDecimal price, BigDecimal stopLoss, BigDecimal takeProfit) {
        this.instrument = instrument;
        this.units = units;
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
    }

    public Instrument getInstrument() {
        return instrument;
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
                Objects.equals(instrument, order.instrument) &&
                Objects.equals(price, order.price) &&
                Objects.equals(stopLoss, order.stopLoss) &&
                Objects.equals(takeProfit, order.takeProfit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, units, price, stopLoss, takeProfit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
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
                return getTakeProfit().subtract(getPrice())
                        .multiply(BigDecimal.valueOf(getUnits()));
            }
        }

        return null;
    }

    /**
     * Checks whether there's a profit or loss on the candle low. If not, then checks the candle high.
     * If the stop loss or take profit was hit at any point in the hourly candle, it'll return a non-null value.
     * <p>
     * If candle was green, it checks from low to high. If red, then high to low.
     */
    public BigDecimal profitLoss(Candlestick candlestick) {
        CandlestickData bid = candlestick.getBid();
        CandlestickData ask = candlestick.getAsk();

        BigDecimal firstBid = bid.getLow();
        BigDecimal firstAsk = ask.getLow();
        BigDecimal secondAsk = ask.getHigh();
        BigDecimal secondBid = bid.getHigh();

        if (candlestick.isRed()) {
            BigDecimal oldFirstBid = firstBid;
            BigDecimal oldFirstAsk = firstAsk;

            firstBid = secondBid;
            firstAsk = secondAsk;

            secondBid = oldFirstBid;
            secondAsk = oldFirstAsk;
        }

        Price openPrice = new Price(firstBid, firstAsk);
        BigDecimal profitLoss = profitLoss(openPrice);

        if (profitLoss == null) {
            Price closePrice = new Price(secondBid, secondAsk);
            profitLoss = profitLoss(closePrice);
        }

        return profitLoss;
    }
}
