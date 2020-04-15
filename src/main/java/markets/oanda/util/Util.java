package markets.oanda.util;

import markets.api.BrokerAPI;
import markets.api.Candlestick;
import markets.api.RequestException;
import markets.oanda.OandaAPI;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.APRIL;

public class Util {

    public static void main(String[] args) throws RequestException {
        BrokerAPI api = OandaAPI.create();

        LocalDateTime from = LocalDateTime.of(2020, APRIL, 15, 3, 0);
        LocalDateTime to = LocalDateTime.of(2020, APRIL, 15, 9, 0);

        List<Candlestick> candles = api.candles("EUR_USD", from, to);
        candles.forEach(System.out::println);
    }

}
