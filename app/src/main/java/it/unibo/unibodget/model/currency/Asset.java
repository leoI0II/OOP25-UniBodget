package it.unibo.unibodget.model.currency;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a financial asset composed of:
 * - an amount
 * - a currency unit (fiat, crypto, stock)
 * - a sign indicating whether it's an income ('+') or an expense ('-')
 *
 * This class is immutable and ensures that both fields are non-null.
 * It provides getters to access the amount and the associated currency unit.
 * The currency unit can be any implementation of the currency unit 
 * {@link CurrencyUnit} interface.
 */
public class Asset {

    private CurrencyUnit currency;
    private BigDecimal amount;
    private final char sign;

    /**
     * Creates a new Asset with the given currency and amount.
     *
     * @param currency the currency unit of the asset; must not be null
     * @param amount   the numerical amount; must not be null
     * @param sign     the sign of the amount ('+' or '-')
     */
    public Asset(CurrencyUnit currency, BigDecimal amount, char sign) {
        this.currency = currency;
        this.amount = amount;
        this.sign = sign;
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

    /**
     * Returns the sign of this asset,
     * indicating whether it's an income ('+') or an expense ('-').
     *
     * @return the sign character
     */
    public char getSign() {
        return this.sign;
    }

    @Override
    public String toString() {
        return "Asset{amount=" + this.amount + 
                ", currency=" + this.currency.getShortName() + 
                ", sign='" + this.sign + "'}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount, this.currency.getCode(), this.sign);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Asset)) return false;
        Asset other = (Asset) obj;
        return Objects.equals(this.amount, other.amount)
                && Objects.equals(this.currency.getCode(), other.currency.getCode())
                && this.sign == other.sign;
    }

}
