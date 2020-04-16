package markets

import markets.api.Price
import spock.lang.Specification
import spock.lang.Unroll

class OrderSpec extends Specification {

    @Unroll
    def 'should evaluate whether order stop loss is triggered correctly: #description'() {

        def stoppedOut = order.isStoppedOut(new Price(1.2444, 1.2447))

        expect:
        stoppedOut == expected

        where:
        description                               | order                                             | expected
        'long order stopped out (bid below)'      | new Order("EUR_USD", 10, 1.2544, 1.2445, 1.2655)  | true
        'long order stopped out (bid equal)'      | new Order("EUR_USD", 10, 1.2544, 1.2444, 1.2655)  | true
        'long order not stopped out (bid above)'  | new Order("EUR_USD", 10, 1.2544, 1.2443, 1.2655)  | false
        'short order stopped out (ask above)'     | new Order("EUR_USD", -10, 1.2344, 1.2446, 1.2244) | true
        'short order stopped out (ask equal)'     | new Order("EUR_USD", -10, 1.2344, 1.2447, 1.2244) | true
        'short order not stopped out (ask below)' | new Order("EUR_USD", -10, 1.2344, 1.2448, 1.2244) | false
    }

    @Unroll
    def 'should evaluate whether order take profit is triggered correctly: #description'() {

        def tookProfit = order.isTakeProfit(new Price(1.2444, 1.2447))

        expect:
        tookProfit == expected

        where:
        description                                 | order                                             | expected
        'long order took profit (bid above)'        | new Order("EUR_USD", 10, 1.2344, 1.2244, 1.2443)  | true
        'long order took profit (bid equal)'        | new Order("EUR_USD", 10, 1.2344, 1.2244, 1.2444)  | true
        'long order didnt take profit (bid below)'  | new Order("EUR_USD", 10, 1.2344, 1.2244, 1.2445)  | false
        'short order took profit (ask below)'       | new Order("EUR_USD", -10, 1.2544, 1.2644, 1.2448) | true
        'short order took profit (ask equal)'       | new Order("EUR_USD", -10, 1.2544, 1.2644, 1.2447) | true
        'short order didnt take profit (ask above)' | new Order("EUR_USD", -10, 1.2544, 1.2644, 1.2446) | false
    }

    @Unroll
    def 'should evaluate profit/loss correctly: #description'() {

        def actual = order.profitLoss(new Price(1.2444, 1.2447))

        expect:
        actual == expected

        where:
        description                                 | order                                             | expected
        'long order stopped out (bid below)'        | new Order("EUR_USD", 10, 1.2544, 1.2445, 1.2655)  | 10 * (1.2445 - 1.2544)
        'short order stopped out (ask above)'       | new Order("EUR_USD", -10, 1.2344, 1.2446, 1.2244) | 10 * (1.2344 - 1.2446)
        'long order took profit (bid above)'        | new Order("EUR_USD", 10, 1.2344, 1.2244, 1.2443)  | 10 * (1.2443 - 1.2344)
        'short order took profit (ask below)'       | new Order("EUR_USD", -10, 1.2544, 1.2644, 1.2448) | 10 * (1.2544 - 1.2448)
        'long order didnt take profit (bid below)'  | new Order("EUR_USD", 10, 1.2344, 1.2244, 1.2445)  | null
        'short order didnt take profit (ask above)' | new Order("EUR_USD", -10, 1.2544, 1.2644, 1.2446) | null
    }
}
