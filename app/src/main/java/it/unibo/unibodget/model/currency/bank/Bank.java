package it.unibo.unibodget.model.currency.bank;

import java.util.Objects;

/**
 * Represents a banking institution and its fee structure for currency conversion
 * operations. A {@code Bank} defines both a fixed commission and a percentage-based
 * commission, allowing conversion services to compute the total cost of exchanging
 * money through this institution.
 * <p>
 * Instances of this class are typically loaded from JSON configuration files
 * (e.g., user-defined bank lists), which is why a no-argument constructor is
 * provided for deserialization frameworks such as Jackson.
 */
public final class Bank {

    private String name;
    private double fixedFee;
    private double percentageFee;

    /**
     * Empty constructor required for JSON deserialization (e.g., Jackson).
     * <p>
     * Fields are expected to be populated via reflection after instantiation.
     */
    public Bank() {}

    /**
     * Creates a new {@code Bank} with the specified fee structure.
     *
     * @param name          the name of the bank; must not be {@code null}
     * @param fixedFee      the fixed commission fee applied to each conversion
     * @param percentageFee the percentage commission fee applied to the converted amount
     */
    public Bank(String name, double fixedFee, double percentageFee) {
        this.name = Objects.requireNonNull(name);
        this.fixedFee = fixedFee;
        this.percentageFee = percentageFee;
    }

    /**
     * Returns the bank's name.
     *
     * @return the name of the bank
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the fixed commission fee applied by the bank.
     *
     * @return the fixed fee value
     */
    public double getFixedFee() {
        return fixedFee;
    }

    /**
     * Returns the percentage-based commission fee applied by the bank.
     *
     * @return the percentage fee value
     */
    public double getPercentageFee() {
        return percentageFee;
    }

    /**
     * Returns a human-readable representation of the bank and its fee structure.
     *
     * @return a formatted string containing the bank name and its fees
     */
    @Override
    public String toString() {
        return String.format("%s (Fixed: %.2f - Calculated: %.2f %%)",
                name, fixedFee, percentageFee);
    }

    /**
     * Compares this bank with another object for equality.
     * <p>
     * Two banks are considered equal if they share the same name. Fee values
     * are not considered in equality checks, allowing banks to be uniquely
     * identified by name.
     *
     * @param o the object to compare with
     * @return {@code true} if the other object represents a bank with the same name
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bank bank = (Bank) o;
        return Objects.equals(name, bank.name);
    }

    /**
     * Computes the hash code for this bank.
     * <p>
     * The hash code is based solely on the bank name, consistent with
     * {@link #equals(Object)}.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
