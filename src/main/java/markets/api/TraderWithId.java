package markets.api;

public class TraderWithId implements Trader {
    private final String id;
    private final Trader trader;

    public TraderWithId(String id, Trader trader) {
        this.id = id;
        this.trader = trader;
    }

    public String getId() {
        return id;
    }

    public Trader getTrader() {
        return trader;
    }

    @Override
    public String getName() {
        return trader.getName();
    }

    @Override
    public void update(Account account, BrokerAPI api, MarketClock clock) {
        trader.update(account, api, clock);
    }
}
