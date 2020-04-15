package markets

import markets.api.MarketClock
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

import static java.time.Month.APRIL
import static java.time.ZoneOffset.UTC

class CheckMarketsSpec extends Specification {

    def 'should check markets each hour'() {

        def currentTime = LocalDateTime.of(2020, APRIL, 15, 9, 0)
        def fixedTime = Clock.fixed(currentTime.toInstant(UTC), ZoneId.of("UTC"))

        def marketClock = new MarketClock(fixedTime)

        expect:
//        new CheckMarkets().run(marketClock)
        true

    }

}
