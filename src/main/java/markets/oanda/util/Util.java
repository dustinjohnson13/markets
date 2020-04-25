package markets.oanda.util;

import markets.api.BrokerAPI;
import markets.api.Candlestick;
import markets.api.CandlestickData;
import markets.api.Instrument;
import markets.oanda.OandaAPI;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.JANUARY;
import static markets.api.Instrument.GBP_USD;

public class Util {

    /**
     * @param args
     *
     * [0] = Oanda practice api token
     * [1] = Oanda live api token
     */
    public static void main(String[] args) throws Exception {
        String oandaPracticeApiToken = args[0];
        String oandaLiveApiToken = args[1];

        BrokerAPI api = OandaAPI.create(oandaPracticeApiToken, oandaLiveApiToken);

        LocalDateTime from = LocalDateTime.of(2010, JANUARY, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, JANUARY, 1, 0, 0);

        StringBuilder sb = new StringBuilder();

        Instrument instrument = GBP_USD;
        List<Candlestick> candles = api.candles(instrument, from, to);
        candles.forEach(it -> {
            CandlestickData bid = it.getBid();
            CandlestickData ask = it.getAsk();
            sb.append(it.getUtcTime()).append(",")
                    .append(bid.getOpen()).append(",")
                    .append(bid.getHigh()).append(",")
                    .append(bid.getLow()).append(",")
                    .append(bid.getClose()).append(",")
                    .append(ask.getOpen()).append(",")
                    .append(ask.getHigh()).append(",")
                    .append(ask.getLow()).append(",")
                    .append(ask.getClose()).append("\n");
        });

        Files.write(new File(instrument.getSymbol() + "-2010-2020.csv").toPath(), sb.toString().getBytes());
    }

}
