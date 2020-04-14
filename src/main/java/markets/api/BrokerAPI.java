package markets.api;

import java.math.BigDecimal;

public interface BrokerAPI {

    Account account(String id) throws RequestException;

    Price price(String accountId, String symbol) throws RequestException;

    void marketOrder(String accountId, String symbol, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException;
}
