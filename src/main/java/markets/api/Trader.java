package markets.api;

public interface Trader {

    String getName();

    void update(Account account, BrokerAPI api, MarketClock clock);

}
