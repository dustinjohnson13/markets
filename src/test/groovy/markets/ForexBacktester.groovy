package markets

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import markets.api.Candlestick
import markets.api.CandlestickData
import markets.api.MarketClock
import markets.api.Trader
import markets.api.TraderWithId
import markets.traders.RandomBollingerBand

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import static java.time.ZoneOffset.UTC
import static markets.api.Instrument.EUR_USD
import static markets.api.Instrument.GBP_USD

class ForexBacktester {

    private static final int INSTANCES_PER_TRADER = 10;

    private static final List<Trader> traderTemplates = Arrays.asList(
            new RandomBollingerBand("Random Bollinger Band (EUR)", EUR_USD),
            new RandomBollingerBand("Random Bollinger Band (GBP)", GBP_USD)
    );

    static void main(String[] args) {

        Multimap<String, TraderWithId> tradersByName = HashMultimap.create();
        traderTemplates.forEach({
            for (int i = 0; i < INSTANCES_PER_TRADER; i++) {
                String id = it.getName() + "[" + i + "]"
                tradersByName.put(it.getName(), new TraderWithId(id, it));
            }
        })

        NavigableMap<LocalDateTime, Candlestick> eurUsdData = loadInstrumentData("EUR_USD");
        NavigableMap<LocalDateTime, Candlestick> gbpUsdData = loadInstrumentData("GBP_USD");

        def checkMarkets = new CheckMarkets()
        def api = new ForexBacktesterAPI(null, tradersByName.values(), eurUsdData, gbpUsdData)

        eurUsdData.keySet().each { currentTime ->
            def fixedTime = Clock.fixed(currentTime.toInstant(UTC), ZoneId.of("UTC"))
            def marketClock = new MarketClock(fixedTime)

            api.update(marketClock)

            checkMarkets.run(marketClock, api, tradersByName.values())
        }

        api.printAccounts();
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
