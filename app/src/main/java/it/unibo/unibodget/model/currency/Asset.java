package it.unibo.unibodget.model.currency;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable monetary value: a {@link CurrencyUnit} paired with a {@link BigDecimal} amount.
 *
 * <p>Positive amounts represent income or holdings; negative amounts represent expenses or debts.</p>
 *
 * @param currency the type of currency (e.g., USD, EUR, BTC); must not be null
 * @param amount   the numerical amount; positive for income, negative for expense; must not be null
 */
public record Asset(CurrencyUnit currency, BigDecimal amount) {

    /**
     * Constructs an Asset with the given currency and amount.
     *
     * @param currency the currency unit of the asset; must not be null
     * @param amount the numerical amount; must not be null
     * @throws NullPointerException if either currency or amount is null
     */
    public Asset {
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }

    /**
     * Factory method to create an Asset from a currency unit and an amount.
     * 
     * @param currency the currency unit of the asset; must not be null
     * @param amount   the numerical amount; must not be null
     * @return a new Asset instance
     */
    public static Asset of(final CurrencyUnit currency, final BigDecimal amount) {
        return new Asset(currency, amount);
    }

    /**
     * Factory method to create a zero-amount Asset for the given currency.
     *
     * @param currency the currency unit; must not be null
     * @return a new Asset with amount {@code 0}
     */
    public static Asset zero(final CurrencyUnit currency) {
        return new Asset(currency, BigDecimal.ZERO);
    }

    /**
     * Asserts that {@code other} shares the same currency as this asset.
     *
     * @param other the asset to check
     * @throws IllegalArgumentException if the currencies differ
     */
    private void requireSameCurrency(final Asset other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot operate on assets with different currencies: "
                        + this.currency
                        + " vs "
                        + other.currency
            );
        }
    }

    /**
     * Returns a new Asset whose amount is the sum of this and {@code other}.
     *
     * @param other the asset to add; must have the same currency
     * @return a new Asset with the combined amount
     * @throws IllegalArgumentException if currencies differ
     */
    public Asset add(final Asset other) {
        requireSameCurrency(other);
        return new Asset(this.currency, this.amount.add(other.amount));
    }

    /**
     * Returns a new Asset whose amount is this minus {@code other}.
     *
     * @param other the asset to subtract; must have the same currency
     * @return a new Asset with the resulting amount
     * @throws IllegalArgumentException if currencies differ
     */
    public Asset subtract(final Asset other) {
        requireSameCurrency(other);
        return new Asset(this.currency, this.amount.subtract(other.amount));
    }

    /**
     * Returns a new Asset with the sign of the amount flipped.
     *
     * @return a new Asset with negated amount
     */
    public Asset negate() {
        return new Asset(this.currency, this.amount.negate());
    }

    /**
     * Returns a new Asset whose amount is this multiplied by {@code other}'s amount.
     *
     * @param other the asset to multiply by; must have the same currency
     * @return a new Asset with the resulting amount
     * @throws IllegalArgumentException if currencies differ
     */
    public Asset multiply(final Asset other) {
        requireSameCurrency(other);
        return new Asset(this.currency, this.amount.multiply(other.amount));
    }

    /**
     * Returns a new Asset whose amount is this multiplied by a scalar {@code factor}.
     *
     * @param factor the scalar multiplier; must not be null
     * @return a new Asset with the scaled amount
     */
    public Asset multiply(final BigDecimal factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        return new Asset(this.currency, this.amount.multiply(factor));
    }

    /**
     * Returns a new Asset whose amount is this divided by {@code other}'s amount.
     *
     * @param other the asset to divide by; must have the same currency
     * @return a new Asset with the resulting amount
     * @throws IllegalArgumentException if currencies differ
     */
    public Asset divide(final Asset other) {
        requireSameCurrency(other);
        return new Asset(this.currency, this.amount.divide(other.amount));
    }

    /**
     * Returns {@code true} if the amount is strictly greater than zero.
     *
     * @return {@code true} for income/positive balance
     */
    public boolean isPositive() {
        return this.amount.signum() > 0;
    }

    /**
     * Returns {@code true} if the amount is strictly less than zero.
     *
     * @return {@code true} for expense/negative balance
     */
    public boolean isNegative() {
        return this.amount.signum() < 0;
    }

    /**
     * Returns {@code true} if the amount is exactly zero.
     *
     * @return {@code true} if the balance is zero
     */
    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    /**
     * Compares this asset's amount to {@code other}'s amount.
     * Both assets must share the same currency.
     *
     * @param other the asset to compare to; must have the same currency
     * @return a negative integer, zero, or positive integer as this amount
     *         is less than, equal to, or greater than {@code other}'s amount
     * @throws IllegalArgumentException if currencies differ
     */
    public int compareTo(final Asset other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

}
