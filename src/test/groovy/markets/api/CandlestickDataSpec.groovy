package markets.api

import spock.lang.Specification
import spock.lang.Unroll

class CandlestickDataSpec extends Specification {

    @Unroll
    def 'should evaluate isGreen and isRed correctly: expectedGreen=#expectedGreen, expectedRed=#expectedRed'() {

        def actualGreen = candle.isGreen()
        def actualRed = candle.isRed()

        expect:
        actualGreen == expectedGreen
        actualRed == expectedRed

        where:
        candle                                  | expectedGreen | expectedRed
        new CandlestickData(1.1, 1.4, 1.0, 1.3) | true          | false
        new CandlestickData(1.3, 1.4, 1.0, 1.1) | false         | true
        new CandlestickData(1.1, 1.4, 1.0, 1.1) | false         | false
    }

}
