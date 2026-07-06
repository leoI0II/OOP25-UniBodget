package it.unibo.unibodget.model.currency;

/**
 * Enum to represent fiat currencies.
 * 
 * Each enum constant defines:
 * - type (fixed to "Fiat")
 * - symbol
 * - short ISO name
 * - full name
 * - ISO code
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
     * @param type      the type of currency (fixed to "Fiat" for this enum)
     * @param symbol    the graphical symbol of the currency
     * @param shortName the short identifier (ISO-like code)
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code
     */
    FiatCurrency(String symbol, String shortName, String fullName, String code) {
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
    public String toString() {
        return String.format(
            "%s { symbol='%s', shortName='%s', fullName='%s', code='%s' }",
            this.name(), symbol, shortName, fullName, code
        );
    }

}
