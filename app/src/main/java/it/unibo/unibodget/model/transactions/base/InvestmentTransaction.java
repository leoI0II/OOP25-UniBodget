package it.unibo.unibodget.model.transactions.base;

import java.time.LocalDate;
import java.util.Objects;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;

/**
 * Represents a specialized financial transaction related to an investment.
 *
 * An InvestmentTransaction extends the basic {@link Transaction} model by
 * including additional information specific to investment operations, such as:
 *
 * - the historical unit price of the asset at the moment of the transaction
 * - an optional fee (e.g., broker fee, exchange fee)
 *
 * This class is typically used for:
 * - buying or selling stocks
 * - crypto trades
 * - investment fund operations
 *
 * It is immutable: all fields are final and set at construction time.
 */
public final class InvestmentTransaction extends Transaction {

    private final Asset unitPrice;
    private final Asset fee;

    /**
     * Creates a new InvestmentTransaction with the given base transaction data
     * and investment‑specific fields.
     *
     * @param asset        the main asset involved in the transaction
     *                     (e.g., total amount invested or received)
     * @param category     the category describing the nature of the transaction
     * @param date         the date on which the transaction occurred
     * @param description  a short human‑readable description of the transaction
     * @param notes        optional additional notes or comments
     * @param unitPrice    the historical unit price of the asset at the time
     *                     of the transaction; may be null if not applicable
     * t@param fee          an optional fee associated with the transaction
     *                     (e.g., broker fee); may be null
     */
    public InvestmentTransaction(
            Asset asset,
            Category category,
            LocalDate date,
            String description,
            String notes,
            Asset unitPrice,
            Asset fee
    ) {
        super(asset, category, date, description, notes);
        this.unitPrice = unitPrice;
        this.fee = fee;
    }

    /**
     * Returns the historical unit price of the asset at the moment of the transaction.
     *
     * @return the unit price as an {@link Asset}, or null if not provided
     */
    public Asset getUnitPrice() { 
        return unitPrice; 
    }

    /**
     * Returns the fee associated with this investment transaction.
     *
     * @return the fee as an {@link Asset}, or null if no fee was applied
     */
    public Asset getFee() {
        return fee;
    }

    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o)) {
            return false;
        }
        final InvestmentTransaction other = (InvestmentTransaction) o;
        return Objects.equals(unitPrice, other.unitPrice)
            && Objects.equals(fee, other.fee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), unitPrice, fee);
    }

    @Override
    public String toString() {
        return "InvestmentTransaction{" + super.toString()
            + ", unitPrice=" + unitPrice
            + ", fee=" + fee
            + '}';
    }

}
