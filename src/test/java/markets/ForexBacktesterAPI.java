package markets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Candlestick;
import markets.api.Instrument;
import markets.api.MarketClock;
import markets.api.Price;
import markets.api.RequestException;
import markets.api.TraderWithId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import static java.lang.System.out;
import static java.util.Comparator.comparing;
import static markets.api.Instrument.EUR_USD;

public class ForexBacktesterAPI implements BrokerAPI {

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(100);

    private final Map<String, Account> accountById = new HashMap<>();
    private final Map<String, Order> openOrderByAccountId = new HashMap<>();

    private final ImmutableMap<String, TraderWithId> tradersById;
    private final NavigableMap<LocalDateTime, Candlestick> eurUsdData;
    private final NavigableMap<LocalDateTime, Candlestick> gbpUsdData;
    private MarketClock marketClock;

    public ForexBacktesterAPI(MarketClock marketClock,
                              Collection<TraderWithId> traders,
                              NavigableMap<LocalDateTime, Candlestick> eurUsdData,
                              NavigableMap<LocalDateTime, Candlestick> gbpUsdData) {
        this.marketClock = marketClock;
        this.tradersById = Maps.uniqueIndex(traders, TraderWithId::getId);
        this.eurUsdData = eurUsdData;
        this.gbpUsdData = gbpUsdData;
    }

    void update(MarketClock marketClock) throws RequestException {
        this.marketClock = marketClock;

        closeOrders();
    }

    /**
     * Closes orders that had a stop loss or take profit triggered.
     */
    private void closeOrders() throws RequestException {
        for (Iterator<Entry<String, Order>> iter = openOrderByAccountId.entrySet().iterator(); iter.hasNext(); ) {
            Entry<String, Order> entry = iter.next();
            String accountId = entry.getKey();
            Order order = entry.getValue();

            Candlestick candlestick;
            try {
                candlestick = getCandlestick(order.getInstrument(), marketClock.nowUTCDateTime());
            } catch (RequestException e) {
                // No data for this symbol + time
                continue;
            }

            BigDecimal profitLoss = order.profitLoss(candlestick);
            if (profitLoss != null) {
                iter.remove();
                Account oldAccount = account(accountId);
                Account newAccount = new Account(accountId, oldAccount.getOpenPositionsCount() - 1, oldAccount.getBalance().add(profitLoss));
                accountById.put(accountId, newAccount);
            }
        }
    }

    @Override
    public Account account(String id) {
        accountById.putIfAbsent(id, new Account(id, 0, INITIAL_BALANCE));

        return accountById.get(id);
    }

    @Override
    public Price price(String accountId, Instrument instrument) throws RequestException {
        LocalDateTime now = marketClock.nowUTCDateTime();

        Candlestick candlestick = getCandlestick(instrument, now);

        return new Price(candlestick.getBid().getOpen(), candlestick.getAsk().getOpen());
    }

    @Override
    public void marketOrder(String accountId, Instrument instrument, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException {
        Price price = price(accountId, instrument);
        BigDecimal currentPrice = units > 1 ? price.getCloseoutAsk() : price.getCloseoutBid();
//        BigDecimal costBasis = currentPrice.multiply(BigDecimal.valueOf(abs(units)));

        Account account = account(accountId);
        Account newAccount = new Account(accountId, account.getOpenPositionsCount() + 1, account.getBalance()/*.subtract(costBasis)*/);
        Order order = new Order(instrument, units, currentPrice, stopLoss, takeProfit);

        accountById.put(accountId, newAccount);
        openOrderByAccountId.put(accountId, order);
    }

    @Override
    public List<Candlestick> candles(Instrument instrument, LocalDateTime utcFrom, LocalDateTime utcTo) throws RequestException {
        if (marketClock.nowUTCDateTime().isBefore(utcTo)) {
            throw new IllegalArgumentException("HTTP 400 : Invalid value specified for 'to'. Time is in the future");
        }
        return new ArrayList<>(instrumentData(instrument).subMap(utcFrom, utcTo).values());
    }

    public void printAccounts() {
        accountById.values()
                .stream()
                .sorted(comparing(Account::getId))
                .forEach(it -> out.println(tradersById.get(it.getId()).getName() + ": " + it));
    }

    private Candlestick getCandlestick(Instrument instrument, LocalDateTime now) throws RequestException {
        Candlestick candlestick = instrumentData(instrument).get(now);
        if (candlestick == null) {
            throw new RequestException("No data for " + now, new IllegalArgumentException());
        }
        return candlestick;
    }

    private NavigableMap<LocalDateTime, Candlestick> instrumentData(Instrument instrument) {
        return EUR_USD.equals(instrument) ? eurUsdData : gbpUsdData;
    }

}
