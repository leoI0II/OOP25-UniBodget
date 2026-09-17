package it.unibo.unibodget.model.transactions.base;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;

/**
 * Represents a basic financial transaction recorded by the system.
 *
 * <p>
 * Each transaction contains:
 * - an {@link Asset} describing the monetary value and currency
 * - a {@link Category} classifying the type of movement
 * - a {@link LocalDate} indicating when the transaction occurred
 * - an optional textual description
 * - optional notes for additional context
 * </p>
 *
 * <p>
 * This class is immutable: all fields are final and set at construction time.
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "transactionType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CashTransaction.class, name = "cash"),
    @JsonSubTypes.Type(value = InvestmentTransaction.class, name = "investment")
})
public abstract class Transaction {

    private final UUID id;
    private final Asset asset;
    private final Category category;
    private final LocalDate date;
    private final String description;
    private final String notes;

    /**
     * Creates a new transaction with an automatically generated identifier.
     *
     * @param asset
     *            the monetary value associated with the transaction; must not be
     *            {@code null}
     * @param category
     *            the category describing the nature of the transaction; must not
     *            be {@code null}
     * @param date
     *            the date on which the transaction occurred; must not be
     *            {@code null}
     * @param description
     *            a short human-readable description of the transaction; may be
     *            {@code null}
     * @param notes
     *            optional additional notes or comments; may be {@code null}
     * @throws NullPointerException
     *             if {@code asset}, {@code category}, or {@code date} is
     *             {@code null}
     */
    // public Transaction(
    //         final Asset asset,
    //         final Category category,
    //         final LocalDate date,
    //         final String description,
    //         final String notes) {
    //     this(UUID.randomUUID(), asset, category, date, description, notes);
    // }

    /**
     * Creates a new transaction with the given explicit identifier.
     *
     * <p>
     * This overload is useful when reconstructing transactions from persistence,
     * importing existing data, or copying transactions while preserving identity.
     * </p>
     *
     * @param id
     *            the stable transaction identifier; must not be {@code null}
     * @param asset
     *            the monetary value associated with the transaction; must not be
     *            {@code null}
     * @param category
     *            the category describing the nature of the transaction; must not
     *            be {@code null}
     * @param date
     *            the date on which the transaction occurred; must not be
     *            {@code null}
     * @param description
     *            a short human-readable description of the transaction; may be
     *            {@code null}
     * @param notes
     *            optional additional notes or comments; may be {@code null}
     * @throws NullPointerException
     *             if {@code id}, {@code asset}, {@code category}, or
     *             {@code date} is {@code null}
     */
    public Transaction(
            final UUID id,
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes) {
        this.id = Objects.requireNonNull(id);
        this.asset = Objects.requireNonNull(asset);
        this.category = Objects.requireNonNull(category);
        this.date = Objects.requireNonNull(date);
        this.description = description;
        this.notes = notes;
    }

    /**
     * Creates a transaction while allowing Jackson to reconstruct it from JSON.
     *
     * @param asset the monetary asset associated with the transaction
     * @param category the transaction category
     * @param date the transaction date
     * @param description an optional description
     * @param notes optional notes
     */
    @JsonCreator
    public Transaction(
            @JsonProperty("asset") final Asset asset,
            @JsonProperty("category") final Category category,
            @JsonProperty("date") final LocalDate date,
            @JsonProperty("description") final String description,
            @JsonProperty("notes") final String notes) {
        this(UUID.randomUUID(), asset, category, date, description, notes);
    }

    /**
     * Returns the unique identifier of this transaction.
     *
     * @return the transaction identifier, never {@code null}
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the monetary asset associated with this transaction.
     *
     * @return the {@link Asset} representing amount, currency, and sign
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * Returns the category assigned to this transaction.
     *
     * @return the {@link Category} describing the transaction type
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns the date on which this transaction occurred.
     *
     * @return the transaction date as a {@link LocalDate}
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns a short textual description of the transaction.
     *
     * @return the description, or {@code null} if not provided
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns additional notes or comments associated with the transaction.
     *
     * @return the notes, or {@code null} if not provided
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Compares this transaction with another object for equality.
     *
     * <p>
     * Subclasses overriding this method must invoke {@code super.equals(Object)}
     * and include all additional state introduced by the subclass in the comparison,
     * preserving the general contract of {@link Object#equals(Object)}.
     *
     * @param o the object to compare with
     * @return {@code true} if the two objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Transaction other = (Transaction) o;
        return Objects.equals(id, other.id)
                && Objects.equals(asset, other.asset)
                && Objects.equals(category, other.category)
                && Objects.equals(date, other.date)
                && Objects.equals(description, other.description)
                && Objects.equals(notes, other.notes);
    }

    /**
     * Returns the hash code for this transaction.
     *
     * <p>
     * Subclasses overriding this method must invoke {@code super.hashCode()}
     * and include all additional state introduced by the subclass, preserving the
     * contract between {@link #equals(Object)} and {@link #hashCode()}.
     *
     * @return the hash code of this transaction
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, asset, category, date, description, notes);
    }

    /**
     * Returns a string representation of this transaction.
     *
     * <p>
     * The returned string includes the asset, category, date, description, and notes.
     * Subclasses overriding this method should include their additional state in the
     * string representation.
     * </p>
     *
     * @return a string describing this transaction
     */
    @Override
    public String toString() {
        return "Transaction{"
                + "id=" + id
                + ", asset=" + asset
                + ", category=" + category
                + ", date=" + date
                + ", description='" + description + '\''
                + ", notes='" + notes + '\''
                + '}';
    }
}
