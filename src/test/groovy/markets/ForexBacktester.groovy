package markets

import markets.api.Candlestick
import markets.api.CandlestickData
import markets.api.MarketClock

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import static java.time.ZoneOffset.UTC

class ForexBacktester {

    static void main(String[] args) {

        NavigableMap<LocalDateTime, Candlestick> eurUsdData = loadInstrumentData("EUR_USD");
        NavigableMap<LocalDateTime, Candlestick> gbpUsdData = loadInstrumentData("GBP_USD");

        eurUsdData.keySet().each { currentTime ->
            def fixedTime = Clock.fixed(currentTime.toInstant(UTC), ZoneId.of("UTC"))
            def marketClock = new MarketClock(fixedTime)

            println(currentTime)
        }

    }

    static NavigableMap<LocalDateTime, Candlestick> loadInstrumentData(String symbol) {
        NavigableMap<LocalDateTime, Candlestick> data = new TreeMap<>();

        ForexBacktester.getResourceAsStream("/${symbol}-2010-2020.csv").eachLine {
            def columns = it.split(",")
            int col = 0
            def utcTime = LocalDateTime.parse(columns[col++], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            CandlestickData bid = new CandlestickData(
                    columns[col++].toBigDecimal(),
                    columns[col++].toBigDecimal(),
                    columns[col++].toBigDecimal(),
                    columns[col++].toBigDecimal()
            )
            CandlestickData ask = new CandlestickData(
                    columns[col++].toBigDecimal(),
                    columns[col++].toBigDecimal(),
                    columns[col++].toBigDecimal(),
                    columns[col++].toBigDecimal()
            )
            data.put(utcTime, new Candlestick(utcTime, bid, ask))
        }

        data
    }
}
