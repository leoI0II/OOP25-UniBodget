package it.unibo.unibodget.model.currency;

import java.util.Objects;

/**
 * A currency loaded from external JSON configuration.
 *
 * <p>Unlike the enum-based currencies ({@link FiatCurrency}, {@link CryptoCurrency}),
 * this class supports dynamic currencies defined at runtime.</p>
 */
public final class Currency implements CurrencyUnit {

    private CurrencyType type;
    private String symbol;
    private String shortName;
    private String fullName;
    private String code;

    /** Empty constructor required for the generic JSON parser. */
    public Currency() { }

    /**
     * Creates a new dynamic currency.
     *
     * @param type      the type of currency
     * @param symbol    the graphical symbol of the currency
     * @param shortName the short identifier
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code
     */
    public Currency(
            final CurrencyType type,
            final String symbol,
            final String shortName,
            final String fullName,
            final String code
    ) {
        this.type = Objects.requireNonNull(type);
        this.symbol = Objects.requireNonNull(symbol);
        this.shortName = Objects.requireNonNull(shortName);
        this.fullName = Objects.requireNonNull(fullName);
        this.code = Objects.requireNonNull(code);
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
        return String.format("%s [%s] - %s", this.shortName, this.symbol, this.fullName);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(this.code);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Currency)) {
            return false;
        }
        final Currency other = (Currency) obj;
        return Objects.equals(this.code, other.code);
    }
}
