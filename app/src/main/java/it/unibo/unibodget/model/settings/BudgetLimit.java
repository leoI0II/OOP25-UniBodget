package it.unibo.unibodget.model.settings;

import java.util.Objects;

import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Represents a single global budget limit chosen by the user.
 * It contains only an amount and a currency.
 */
public class BudgetLimit {
    
    private double amount;
    private CurrencyUnit currency;

    /**
     * Creates a new BudgetLimit with the given amount and currency.
     *
     * @param amount   the numeric limit (must be >= 0)
     * @param currency the currency of the limit (not null)
     */
    public BudgetLimit(double amount, CurrencyUnit currency) {
        if (amount < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative");
        }
        this.amount = amount;
        this.currency = Objects.requireNonNull(currency);
    }

    /**
     * Returns the numeric value of the budget limit.
     *
     * @return the limit amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Updates the numeric value of the budget limit.
     *
     * @param amount the new limit amount (must be >= 0)
     */
    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative");
        }
        this.amount = amount;
    }

    /**
     * Returns the currency associated with the budget limit.
     *
     * @return the CurrencyUnit of the limit
     */
    public CurrencyUnit getCurrency() {
        return currency;
    }

    /**
     * Updates the currency associated with the budget limit.
     *
     * @param currency the new CurrencyUnit (not null)
     */
    public void setCurrency(CurrencyUnit currency) {
        this.currency = Objects.requireNonNull(currency);
    }

    @Override
    public String toString() {
        return "BudgetLimit{amount=" + amount + ", currency=" + currency + "}";
    }  

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BudgetLimit)) return false;
        BudgetLimit b = (BudgetLimit) o;
        return Double.compare(b.amount, amount) == 0
                && Objects.equals(currency, b.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
