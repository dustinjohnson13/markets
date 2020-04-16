package markets.api;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    private final String id;
    private final int openPositions;
    private final BigDecimal balance;


    public Account(String id, int openPositions, BigDecimal balance) {
        this.id = id;
        this.openPositions = openPositions;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public int getOpenPositionsCount() {
        return openPositions;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return openPositions == account.openPositions &&
                Objects.equals(id, account.id) &&
                Objects.equals(balance, account.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, openPositions, balance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("openPositions", openPositions)
                .add("balance", balance)
                .toString();
    }
}
