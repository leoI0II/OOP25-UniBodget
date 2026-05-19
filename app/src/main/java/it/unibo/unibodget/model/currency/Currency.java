package it.unibo.unibodget.model.currency;

import java.util.Objects;

/**
 * Represents a currency loaded from external JSON configuration.
 * 
 * A Currency instance defines:
 * - type: the category of the currency
 * - symbol: graphical representation
 * - shortName: short identifier
 * - fullName: full descriptive name
 * - code: standardized currency code
 */
public final class Currency implements CurrencyUnit {

    private CurrencyType type;
    private String symbol;
    private String shortName;
    private String fullName;
    private String code;

    /** Empty constructor required for the generic JSON parser. */
    public Currency() {}

    /**
     * Creates a new dynamic currency.
     *
     * @param type      the type of currency
     * @param symbol    the graphical symbol of the currency
     * @param shortName the short identifier
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code
     */
    public Currency(CurrencyType type, String symbol, String shortName, String fullName, String code) {
        this.type = Objects.requireNonNull(type);
        this.symbol = Objects.requireNonNull(symbol);
        this.shortName = Objects.requireNonNull(shortName);
        this.fullName = Objects.requireNonNull(fullName);
        this.code = Objects.requireNonNull(code);
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

    @Override
    public String toString() {
        return String.format("%s [%s] - %s", this.shortName, this.symbol, this.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.code);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Currency)) return false;
        Currency other = (Currency) obj;
        return Objects.equals(this.code, other.code);
    }
}
