package it.unibo.unibodget.model.currency;

/**
 * Supported fiat currencies.
 *
 * <p>Each constant carries a display symbol, short ISO name, full name,
 * and currency code. All fiat currencies use 2 decimal places for display.</p>
 */
public enum FiatCurrency implements CurrencyUnit {

    EUR("€", "EUR", "Euro", "EUR"),
    USD("$", "USD", "United States Dollar", "USD"),
    GBP("£", "GBP", "Pound Sterling", "GBP"),
    RUB("₽", "RUB", "Russian Ruble", "RUB"),
    JPY("¥", "JPY", "Japanese Yen", "JPY"),
    AUD("$", "AUD", "Australian Dollar", "AUD"),
    CAD("$", "CAD", "Canadian Dollar", "CAD"),
    CHF("₣", "CHF", "Swiss Franc", "CHF"),
    CNY("¥", "CNY", "Chinese Yuan", "CNY"),
    HKD("$", "HKD", "Hong Kong Dollar", "HKD"),
    INR("₹", "INR", "Indian Rupee", "INR");

    private final CurrencyType type = CurrencyType.FIAT;
    private final String symbol;
    private final String shortName;
    private final String fullName;
    private final String code;

    /**
     * Constructs a fiat currency definition.
     *
     * @param symbol    the graphical symbol of the currency
     * @param shortName the short identifier (ISO-like code)
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code
     */
    FiatCurrency(
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(
            "%s { symbol='%s', shortName='%s', fullName='%s', code='%s' }",
            this.name(), symbol, shortName, fullName, code
        );
    }
}
