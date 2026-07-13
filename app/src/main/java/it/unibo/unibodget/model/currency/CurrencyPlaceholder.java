package it.unibo.unibodget.model.currency;

/**
 * Lightweight placeholder used during JSON deserialization.
 * It only stores the currency code. After initialization,
 * Currency.init() replaces these placeholders with real Currency objects.
 */
public final class CurrencyPlaceholder implements CurrencyUnit {

    private final String code;

    /**
     * Creates a new placeholder currency used during JSON deserialization.
     *
     * <p>
     * The placeholder stores only the currency code. After all currency
     * definitions are loaded, {@link Currency#init()} replaces instances of
     * {@code CurrencyPlaceholder} with fully initialized {@link Currency} objects.
     * </p>
     *
     * @param code the ISO-like currency code extracted from JSON
     */
    public CurrencyPlaceholder(final String code) {
        this.code = code;
    }

    @Override
    public CurrencyType getType() {
        return CurrencyType.FIAT; // temporary default
    }

    @Override
    public String getSymbol() {
        return code;
    }

    @Override
    public String getShortName() {
        return code;
    }

    @Override
    public String getFullName() {
        return code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
