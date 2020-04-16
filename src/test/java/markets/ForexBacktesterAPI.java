package markets;

import markets.api.Account;
import markets.api.BrokerAPI;
import markets.api.Candlestick;
import markets.api.MarketClock;
import markets.api.Price;
import markets.api.RequestException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

public class ForexBacktesterAPI implements BrokerAPI {

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(100);

    private final Map<String, Account> accountById = new HashMap<>();
    private final Map<String, Order> openOrderByAccountId = new HashMap<>();

    private final NavigableMap<LocalDateTime, Candlestick> eurUsdData;
    private final NavigableMap<LocalDateTime, Candlestick> gbpUsdData;
    private MarketClock marketClock;

    public ForexBacktesterAPI(MarketClock marketClock,
                              NavigableMap<LocalDateTime, Candlestick> eurUsdData,
                              NavigableMap<LocalDateTime, Candlestick> gbpUsdData) {
        this.marketClock = marketClock;
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

            Price price;
            try {
                price = price(accountId, order.getSymbol());
            } catch (RequestException e) {
                // No data for this symbol + time
                continue;
            }

            BigDecimal profitLoss = order.profitLoss(price);
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
    public Price price(String accountId, String symbol) throws RequestException {
        LocalDateTime now = marketClock.nowUTCDateTime();

        Candlestick candlestick = dataForSymbol(symbol).get(now);
        if (candlestick == null) {
            throw new RequestException("No data for " + now, new IllegalArgumentException());
        }

        return new Price(candlestick.getBid().getOpen(), candlestick.getAsk().getOpen());
    }

    @Override
    public void marketOrder(String accountId, String symbol, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException {
        Price price = price(accountId, symbol);
        BigDecimal currentPrice = units > 1 ? price.getCloseoutAsk() : price.getCloseoutBid();
//        BigDecimal costBasis = currentPrice.multiply(BigDecimal.valueOf(abs(units)));

        Account account = account(accountId);
        Account newAccount = new Account(accountId, account.getOpenPositionsCount() + 1, account.getBalance()/*.subtract(costBasis)*/);
        Order order = new Order(symbol, units, currentPrice, stopLoss, takeProfit);

        accountById.put(accountId, newAccount);
        openOrderByAccountId.put(accountId, order);
    }

    @Override
    public List<Candlestick> candles(String symbol, LocalDateTime easternFrom, LocalDateTime easternTo) throws RequestException {
        return null;
    }

    private NavigableMap<LocalDateTime, Candlestick> dataForSymbol(String symbol) {
        return "EUR_USD".equals(symbol) ? eurUsdData : gbpUsdData;
    }

    public void printAccounts() {
        accountById.values().forEach(System.out::println);
    }
}
