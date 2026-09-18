package it.unibo.unibodget.model.transactions.base;

import java.time.LocalDate;
import java.util.Objects;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;

/**
 * Represents a specialized financial transaction related to an investment.
 *
 * <p>An {@code InvestmentTransaction} extends the basic {@link Transaction} model by
 * including additional information specific to investment operations, such as:
 * <ul>
 *   <li>the historical unit price of the asset at the moment of the transaction</li>
 *   <li>an optional fee (e.g., broker fee, exchange fee)</li>
 * </ul>
 *
 * <p>This class is typically used for:
 * <ul>
 *   <li>buying or selling stocks</li>
 *   <li>crypto trades</li>
 *   <li>investment fund operations</li>
 * </ul>
 *
 * <p>It is immutable: all fields are {@code final} and set at construction time.
 */
public final class InvestmentTransaction extends AbstractTransaction {

    private final Asset unitPrice;
    private final Asset fee;

    /**
     * Creates a new {@code InvestmentTransaction} with the given base transaction data
     * and investment-specific fields.
     *
     * @param asset        the main asset involved in the transaction
     *                     (e.g., total amount invested or received)
     * @param category     the category describing the nature of the transaction
     * @param date         the date on which the transaction occurred
     * @param description  a short human-readable description of the transaction
     * @param notes        optional additional notes or comments
     * @param unitPrice    the historical unit price of the asset at the time
     *                     of the transaction; must not be {@code null}
     * @param fee          an optional fee associated with the transaction
     *                     (e.g., broker fee); may be {@code null} if no fee applies
     * @throws NullPointerException     if {@code unitPrice} is {@code null}
     * @throws IllegalArgumentException if {@code unitPrice} or {@code fee} is negative
     */
    public InvestmentTransaction(
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final Asset unitPrice,
            final Asset fee
    ) {
        super(asset, category, date, description, notes);
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (unitPrice.isNegative()) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
        this.unitPrice = unitPrice;
        this.fee = fee;
        if (fee != null && fee.isNegative()) {
            throw new IllegalArgumentException("Fee cannot be negative.");
        }
    }

    /**
     * Static factory method; delegates to the constructor.
     *
     * @param asset        the main asset involved in the transaction
     * @param category     the category describing the nature of the transaction
     * @param date         the date on which the transaction occurred
     * @param description  a short human-readable description of the transaction
     * @param notes        optional additional notes or comments
     * @param unitPrice    the historical unit price of the asset; must not be {@code null}
     * @param fee          an optional fee; may be {@code null}
     * @return a new {@code InvestmentTransaction}
     * @throws NullPointerException     if {@code unitPrice} is {@code null}
     * @throws IllegalArgumentException if {@code unitPrice} or {@code fee} is negative
     */
    public static InvestmentTransaction of(
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final Asset unitPrice,
            final Asset fee
    ) {
        return new InvestmentTransaction(asset, category, date, description, notes, unitPrice, fee);
    }

    /**
     * Returns the historical unit price of the asset at the moment of the transaction.
     *
     * @return the unit price as an {@link Asset}; never {@code null}
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

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o)) {
            return false;
        }
        final InvestmentTransaction other = (InvestmentTransaction) o;
        return Objects.equals(unitPrice, other.unitPrice)
            && Objects.equals(fee, other.fee);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), unitPrice, fee);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InvestmentTransaction{" + super.toString()
            + ", unitPrice=" + unitPrice
            + ", fee=" + fee
            + '}';
    }
}
