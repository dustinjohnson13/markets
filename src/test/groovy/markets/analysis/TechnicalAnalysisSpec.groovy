package markets.analysis

import spock.lang.Specification
import spock.lang.Unroll

import static java.math.RoundingMode.HALF_UP

class TechnicalAnalysisSpec extends Specification {

    @Unroll
    def "should generate correct bollinger band values: #description"() {

        def result = TechnicalAnalysis.bollingerBands(20, 2, closePrices)
        def actual = []
        // Round to two decimal places like the test data did
        for (BollingerBand bb : result) {
            actual.add(new BollingerBand(
                    bb.upperBand.setScale(2, HALF_UP),
                    bb.middleBand.setScale(2, HALF_UP),
                    bb.lowerBand.setScale(2, HALF_UP)
            ))
        }

        expect:
        actual == expected

        where:
        description | expected                          | closePrices
        '20,2'      | [
                new BollingerBand(91.29, 88.71, 86.13),
                new BollingerBand(91.95, 89.05, 86.14),
                new BollingerBand(92.61, 89.24, 85.87),
                new BollingerBand(92.93, 89.39, 85.85),
                new BollingerBand(93.31, 89.51, 85.70),
                new BollingerBand(93.73, 89.69, 85.65),
                new BollingerBand(93.90, 89.75, 85.59),
                new BollingerBand(94.26, 89.91, 85.56),
                new BollingerBand(94.56, 90.08, 85.60),
                new BollingerBand(94.79, 90.38, 85.98),
                new BollingerBand(95.04, 90.66, 86.27),
                new BollingerBand(94.91, 90.86, 86.82),
                new BollingerBand(94.90, 90.88, 86.86),
                new BollingerBand(94.89, 90.90, 86.91),
                new BollingerBand(94.86, 90.99, 87.12),
                new BollingerBand(94.67, 91.15, 87.63),
                new BollingerBand(94.55, 91.19, 87.83),
                new BollingerBand(94.68, 91.12, 87.56),
                new BollingerBand(94.57, 91.17, 87.76),
                new BollingerBand(94.53, 91.25, 87.97),
                new BollingerBand(94.53, 91.24, 87.95),
                new BollingerBand(94.37, 91.17, 87.96),
                new BollingerBand(94.15, 91.05, 87.95)] | [

                // Values for 20-day SMA before first value
                86.16, 89.09, 88.78, 90.32, 89.07, 91.15, 89.44, 89.18, 86.93, 87.68, 86.96,
                89.43, 89.32, 88.72, 87.45, 87.26, 89.50, 87.90, 89.13,
                // These get band values generated
                90.7, 92.9, 92.98, 91.8, 92.66, 92.68, 92.3, 92.77, 92.54, 92.95, 93.2, 91.07,
                89.83, 89.74, 90.4, 90.74, 88.02, 88.09, 88.84, 90.78, 90.54, 91.39, 90.65
        ]
    }

}
