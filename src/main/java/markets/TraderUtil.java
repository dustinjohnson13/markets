package markets;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_DOWN;

public final class TraderUtil {

    private TraderUtil() {
    }

    public static int calculatePositionSize(BigDecimal balanceDollars,
                                     BigDecimal percentageLossAllowed,
                                     BigDecimal currentPrice,
                                     BigDecimal stopLoss) {
        return balanceDollars
                .multiply(percentageLossAllowed)
                .divide(currentPrice.subtract(stopLoss).abs(), 4, HALF_DOWN)
                .intValue();
    }
}
