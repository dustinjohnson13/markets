package markets

import spock.lang.Specification
import spock.lang.Unroll

import static markets.TraderUtil.calculatePositionSize

class TraderUtilSpec extends Specification {

    @Unroll
    def "should calculate position size correctly: expected=#expected"() {

        def actual = calculatePositionSize(balance, percentageLoss, price, stopLoss)

        expect:
        actual == expected

        where:
        balance | percentageLoss | price  | stopLoss | expected
        100.0   | 0.005          | 1.1234 | 1.1134   | 50
        100.0   | 0.01           | 1.1234 | 1.1134   | 100
        100.0   | 0.005          | 1.1234 | 1.1334   | 50
        100.0   | 0.01           | 1.1234 | 1.1334   | 100
        200.0   | 0.005          | 1.1234 | 1.1134   | 100
        200.0   | 0.01           | 1.1234 | 1.1134   | 200
    }

}
