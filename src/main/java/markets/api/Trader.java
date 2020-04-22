package markets.api;

public interface Trader {

    String getId();

    String getName();

    void update(Account account, BrokerAPI api);

}
