package it.unibo.unibodget.model.currency;

/**
 * Enum to represent stock market currencies.
 * 
 * Each enum constant defines:
 * - type (fixed to "Stock" for this enum)
 * - symbol
 * - short ISO name
 * - full company name
 * - ISO code
 */
public enum StockMarketCurrency implements CurrencyUnit {

    AAPL("$", "AAPL", "Apple Inc.", "AAPL"),
    MSFT("$", "MSFT", "Microsoft Corp.", "MSFT"),
    NVDA("$", "NVDA", "NVIDIA Corp.", "NVDA"),
    AMZN("$", "AMZN", "Amazon.com Inc.", "AMZN"),
    GOOGL("$", "GOOGL", "Alphabet Inc.", "GOOGL"),
    META("$", "META", "Meta Platforms Inc.", "META"),
    TSLA("$", "TSLA", "Tesla Inc.", "TSLA");

    private final CurrencyType type = CurrencyType.STOCK;
    private final String symbol;
    private final String shortName;
    private final String fullName;
    private final String code;

    /**
     * Constructs a stock market currency definition.
     *
     * @param type      the type of currency (fixed to "Stock" for this enum)
     * @param symbol    the graphical symbol associated with the stock currency
     * @param shortName the short identifier or ticker used in trading contexts
     * @param fullName  the full descriptive name of the company or traded asset
     * @param code      the standardized code used internally or by external data providers
     */
    StockMarketCurrency(String symbol, String shortName, String fullName, String code) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.fullName = fullName;
        this.code = code;
    }

    @Override
    public CurrencyType getType() {
        return this.type;
    }

    @Override
    public String getSymbol() { 
        return this.symbol; 
    }

    @Override
    public String getShortName() { 
        return this.shortName; 
    }

    @Override
    public String getFullName() { 
        return this.fullName; 
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public int getDisplayDecimals() {
        return 2;
    }
}
