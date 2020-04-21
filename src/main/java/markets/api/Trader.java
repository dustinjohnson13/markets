package markets.api;

public interface Trader {

    String getId();

    void update(Account account, BrokerAPI api);

}
