package markets.traders;

import com.google.common.base.MoreObjects;
import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Instrument;
import markets.api.MarketClock;
import markets.api.Price;
import markets.api.RequestException;
import markets.api.Trader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Random;

import static java.math.BigDecimal.valueOf;
import static markets.TraderUtil.calculatePositionSize;

public class CoinFlip implements Trader {

    private static final Logger LOG = LoggerFactory.getLogger(CoinFlip.class);

    private final String name;
    private final BigDecimal stopLossPips;
    private final BigDecimal takeProfitPips;
    private final Instrument instrument;

    public CoinFlip(String name, BigDecimal stopLossPips, BigDecimal takeProfitPips, Instrument instrument) {
        this.name = name;
        this.stopLossPips = stopLossPips;
        this.takeProfitPips = takeProfitPips;
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

        boolean shortInstrument = new Random().nextBoolean();
        BigDecimal price = shortInstrument ? prices.getCloseoutBid() : prices.getCloseoutAsk();
        BigDecimal stopLoss = shortInstrument ? price.add(stopLossPips) : price.subtract(stopLossPips);

        BigDecimal balanceDollars = account.getBalance();
        int units = calculatePositionSize(balanceDollars, valueOf(0.005), price, stopLoss);  // Risk half a percent
        int orderUnits = shortInstrument ? -units : units;

        BigDecimal takeProfit = shortInstrument ? price.subtract(takeProfitPips) :
                price.add(takeProfitPips);

        try {
            api.marketOrder(id, instrument, orderUnits, stopLoss, takeProfit);
        } catch (RequestException e) {
            LOG.error("{}: Error opening order!", id, e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("stopLossPips", stopLossPips)
                .add("takeProfitPips", takeProfitPips)
                .add("instrument", instrument)
                .toString();
    }
}
