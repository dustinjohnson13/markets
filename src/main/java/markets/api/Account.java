package markets.api;

import java.math.BigDecimal;

public class Account {
    private final int openPositions;
    private final BigDecimal balance;


    public Account(int openPositions, BigDecimal balance) {
        this.openPositions = openPositions;
        this.balance = balance;
    }

    public int getOpenPositionsCount() {
        return openPositions;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
