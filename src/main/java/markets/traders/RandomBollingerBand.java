package markets.traders;

import com.google.common.base.MoreObjects;
import markets.analysis.BollingerBand;
import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Candlestick;
import markets.api.Instrument;
import markets.api.MarketClock;
import markets.api.Price;
import markets.api.RequestException;
import markets.api.Trader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toList;
import static markets.TraderUtil.calculatePositionSize;
import static markets.analysis.TechnicalAnalysis.bollingerBands;

public class RandomBollingerBand implements Trader {

    private static final Logger LOG = LoggerFactory.getLogger(RandomBollingerBand.class);

    private final String name;
    private final Instrument instrument;

    public RandomBollingerBand(String name, Instrument instrument) {
        this.name = name;
        this.instrument = instrument;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(Account account, BrokerAPI api, MarketClock clock) {
        String id = account.getId();
        Price prices;
        try {
            prices = api.price(id, instrument);
        } catch (RequestException e) {
            LOG.error("{}: Error accessing pricing!", id, e);
            return;
        }

        LocalDateTime now = clock.nowUTCDateTime();

        List<Candlestick> candles;
        try {
            candles = api.candles(instrument, now.minusDays(15), now);
        } catch (RequestException e) {
            LOG.error("{}: Error accessing candles!", id, e);
            return;
        }

        if (candles.size() < 21) {
            LOG.info("Only {} candles. Skipping...", candles.size());
            return;
        }

        List<BigDecimal> closePrices = candles.stream()
                .map(it -> it.getBid().getClose())
                .collect(toList());

        List<BollingerBand> bollingerBands = bollingerBands(20, 2, closePrices);
        BollingerBand mostRecentBand = bollingerBands.get(bollingerBands.size() - 1);

        boolean shortInstrument = new Random().nextBoolean();
        BigDecimal price = shortInstrument ? prices.getCloseoutBid() : prices.getCloseoutAsk();
        BigDecimal takeProfit = shortInstrument ?
                mostRecentBand.getLowerBand() :
                mostRecentBand.getUpperBand();
        takeProfit = takeProfit.setScale(4, HALF_UP);

        BigDecimal takeProfitPips = shortInstrument ? price.subtract(takeProfit) : takeProfit.subtract(price);
        if (takeProfitPips.compareTo(valueOf(0.0003)) < 0) { // Skip if less than 3 pips
            return;
        }

        BigDecimal stopLoss = shortInstrument ? price.add(takeProfitPips.multiply(valueOf(.75))) :
                price.subtract(takeProfitPips.multiply(valueOf(.75)));
        stopLoss = stopLoss.setScale(4, HALF_UP);

        BigDecimal balanceDollars = account.getBalance();
        int units = calculatePositionSize(balanceDollars, valueOf(0.005), price, stopLoss);  // Risk half a percent
        int orderUnits = shortInstrument ? -units : units;

        try {
            api.marketOrder(id, instrument, orderUnits, stopLoss, takeProfit);
        } catch (RequestException e) {
            LOG.error("{}: Error opening order! balance={}, orderUnits={}, stopLoss={}, takeProfit={}",
                    id, balanceDollars, orderUnits, stopLoss, takeProfit, e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("instrument", instrument)
                .toString();
    }
}
