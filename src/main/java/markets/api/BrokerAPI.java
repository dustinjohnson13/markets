package markets.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BrokerAPI {

    Account account(String id) throws RequestException;

    Price price(String accountId, Instrument instrument) throws RequestException;

    void marketOrder(String accountId, Instrument instrument, int units, BigDecimal stopLoss, BigDecimal takeProfit) throws RequestException;

    List<Candlestick> candles(Instrument instrument, LocalDateTime utcFrom, LocalDateTime utcTo) throws RequestException;
}
