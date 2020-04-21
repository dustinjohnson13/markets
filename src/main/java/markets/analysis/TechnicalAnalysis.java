package markets.analysis;

import com.google.common.base.Preconditions;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.tictactec.ta.lib.MAType.Sma;

public final class TechnicalAnalysis {

    private TechnicalAnalysis() {
    }

    public static List<BollingerBand> bollingerBands(int period, int standardDeviations, List<BigDecimal> closePrices) {
        Preconditions.checkArgument(closePrices.size() > 19, "Need at least 20 values to generate a 20-day SMA!");

        List<BollingerBand> result = new ArrayList<>();

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        double[] outRealUpperBand = new double[closePrices.size()];
        double[] outRealMiddleBand = new double[closePrices.size()];
        double[] outRealLowerBand = new double[closePrices.size()];

        double[] asDoubles = new double[closePrices.size()];
        for (int i = 0; i < closePrices.size(); i++) {
            asDoubles[i] = closePrices.get(i).doubleValue();
        }

        Core c = new Core();
        RetCode retCode = c.bbands(0, asDoubles.length - 1, asDoubles, period,
                standardDeviations, standardDeviations, Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
        if (retCode != RetCode.Success) {
            throw new IllegalStateException("Encountered error: " + retCode);
        }

        for (int i = 0; i < outRealUpperBand.length - period + 1; i++) {
            result.add(new BollingerBand(
                    BigDecimal.valueOf(outRealUpperBand[i]),
                    BigDecimal.valueOf(outRealMiddleBand[i]),
                    BigDecimal.valueOf(outRealLowerBand[i])
            ));
        }

        return result;
    }

}
