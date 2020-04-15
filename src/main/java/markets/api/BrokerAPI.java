package markets.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BrokerAPI {

    Account account(String id) throws RequestException;

    Price price(String accountId, String symbol) throws RequestException;

    void marketOrder(String accountId, String symbol, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException;

    List<Candlestick> candles(String symbol, LocalDateTime easternFrom, LocalDateTime easternTo) throws RequestException;
}
