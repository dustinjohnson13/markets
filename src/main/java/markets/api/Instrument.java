package markets.api;

public enum Instrument {
    EUR_USD("EUR_USD"),
    GBP_USD("GBP_USD");

    private final String symbol;

    Instrument(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
