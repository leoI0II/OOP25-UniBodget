package it.unibo.unibodget.model.currency;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a financial asset composed of an amount and a currency unit
 * (fiat, crypto, or stock). A negative amount represents an expense; positive an income.
 *
 * <p>This class is immutable: both fields are final and non-null.
 * The currency unit can be any implementation of {@link CurrencyUnit}.
 */
public final class Asset {

    private final CurrencyUnit currency;
    private final BigDecimal amount;

    /**
     * Creates a new Asset with the given currency and amount.
     *
     * @param currency the currency unit of the asset; must not be null
     * @param amount   the numerical amount; must not be null
     */
    public Asset(final CurrencyUnit currency, final BigDecimal amount) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
    }

    /**
     * Factory method to create an Asset from a currency unit and an amount.
     * 
     * @param currency the currency unit of the asset; must not be null
     * @param amount   the numerical amount; must not be null
     * @return a new Asset instance
     */
    public static Asset of(CurrencyUnit currency, BigDecimal amount) {
        return new Asset(currency, amount);
    }

    /**
     * Returns the numerical amount of this asset.
     *
     * @return the amount as a {@link BigDecimal}
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * Returns the currency unit associated with this asset.
     *
     * @return the currency unit implementing {@link CurrencyUnit}
     */
    public CurrencyUnit getCurrencyUnit() {
        return this.currency;
    }

    @Override
    public String toString() {
        return "Asset{amount=" + this.amount + 
                ", currency=" + this.currency.getShortName() + "'}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount, this.currency);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Asset)) return false;
        Asset other = (Asset) obj;
        return Objects.equals(this.amount, other.amount)
                && Objects.equals(this.currency, other.currency);
    }

}
