package it.unibo.unibodget.model.currency;

/**
 * Supported stock market instruments.
 *
 * <p>Each constant represents a publicly traded company, identified by its ticker symbol.
 * All stocks share the {@code "$"} display symbol and use 2 decimal places for display.</p>
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
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
     * @param symbol    the graphical symbol associated with the stock currency
     * @param shortName the short identifier or ticker used in trading contexts
     * @param fullName  the full descriptive name of the company or traded asset
     * @param code      the standardized code used internally or by external data providers
     */
    StockMarketCurrency(
            final String symbol,
            final String shortName,
            final String fullName,
            final String code
    ) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.fullName = fullName;
        this.code = code;
    }

    /** {@inheritDoc} */
    @Override
    public CurrencyType getType() {
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public String getSymbol() {
        return this.symbol;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName() {
        return this.shortName;
    }

    /** {@inheritDoc} */
    @Override
    public String getFullName() {
        return this.fullName;
    }

    /** {@inheritDoc} */
    @Override
    public String getCode() {
        return this.code;
    }

    /** {@inheritDoc} */
    @Override
    public int getDisplayDecimals() {
        return 2;
    }
}
