package it.unibo.unibodget.model.transactions.base;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;

/**
 * Represents a cash transaction recorded inside a cash wallet.
 *
 * <p>
 * A cash transaction may optionally contain friend-loan metadata. This allows
 * friend-loan movements to be tracked and persisted exactly like any other
 * wallet transaction.
 * </p>
 */
public final class CashTransaction extends AbstractTransaction {

    private final UUID friendLoanId;
    private final String friendName;



    /**
     * Creates a new cash transaction, optionally linked to a friend loan.
     *
     * @param asset the monetary value associated with the transaction; must not be null
     * @param category the category describing the nature of the transaction; must not be null
     * @param date the date on which the transaction occurred; must not be null
     * @param description a short human-readable description of the transaction; may be null
     * @param notes optional additional notes or comments; may be null
     * @param friendLoanId the identifier of the linked friend loan; may be null
     * @param friendName the name of the related friend; may be null
     * @throws IllegalArgumentException if friend-loan metadata is invalid
     */
    @JsonCreator
    public CashTransaction(
            @JsonProperty("asset") final Asset asset,
            @JsonProperty("category") final Category category,
            @JsonProperty("date") final LocalDate date,
            @JsonProperty("description") final String description,
            @JsonProperty("notes") final String notes,
            @JsonProperty("friendLoanId") final UUID friendLoanId,
            @JsonProperty("friendName") final String friendName) {
        this(UUID.randomUUID(), asset, category, date, description, notes, friendLoanId, friendName);
    }

    /**
     * Creates a new cash transaction with an explicit identifier, optionally linked to a friend loan.
     *
     * @param id the stable transaction identifier; must not be null
     * @param asset the monetary value associated with the transaction; must not be null
     * @param category the category describing the nature of the transaction; must not be null
     * @param date the date on which the transaction occurred; must not be null
     * @param description a short human-readable description of the transaction; may be null
     * @param notes optional additional notes or comments; may be null
     * @param friendLoanId the identifier of the linked friend loan; may be null
     * @param friendName the name of the related friend; may be null
     * @throws IllegalArgumentException if friend-loan metadata is invalid
     */
    public CashTransaction(
            final UUID id,
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final UUID friendLoanId,
            final String friendName) {
        super(id, asset, category, date, description, notes);

        final boolean hasLoanId = friendLoanId != null;
        final boolean hasFriendName = friendName != null && !friendName.isBlank();
        final boolean isFriendLoanCategory = category.getType() == CategoryType.FRIEND_LOAN;

        if (hasLoanId != hasFriendName) {
            throw new IllegalArgumentException(
                    "friendLoanId and friendName must either both be provided or both be absent."
            );
        }

        if (isFriendLoanCategory && !hasLoanId) {
            throw new IllegalArgumentException(
                    "FRIEND_LOAN transactions must provide both friendLoanId and friendName."
            );
        }

        if (!isFriendLoanCategory && hasLoanId) {
            throw new IllegalArgumentException(
                    "Friend-loan metadata can only be set for FRIEND_LOAN transactions."
            );
        }

        this.friendLoanId = friendLoanId;
        this.friendName = friendName;
    }

    /**
     * Static factory method for a standard cash transaction with no friend-loan metadata.
     *
     * @param asset the monetary value associated with the transaction; must not be null
     * @param category the category describing the nature of the transaction; must not be null
     * @param date the date on which the transaction occurred; must not be null
     * @param description a short human-readable description of the transaction; may be null
     * @param notes optional additional notes or comments; may be null
     * @return a new {@code CashTransaction}
     * @throws NullPointerException if asset, category, or date is null
     */
    public static CashTransaction of(
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes
    ) {
        return builder()
                .asset(asset)
                .category(category)
                .date(date)
                .description(description)
                .notes(notes)
                .build();
    }

    /**
     * Returns the identifier of the linked friend loan, if present.
     *
     * @return an {@link Optional} containing the friend-loan identifier, or empty
     */
    public Optional<UUID> getFriendLoanId() {
        return Optional.ofNullable(friendLoanId);
    }

    /**
     * Returns the name of the related friend, if present.
     *
     * @return an {@link Optional} containing the friend's name, or empty
     */
    public Optional<String> getFriendName() {
        return Optional.ofNullable(friendName);
    }

    /**
     * Returns whether this transaction belongs to a friend loan.
     *
     * @return true if this transaction contains friend-loan metadata
     */
    public boolean isFriendLoanTransaction() {
        return friendLoanId != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final CashTransaction other = (CashTransaction) obj;
        return Objects.equals(friendLoanId, other.friendLoanId)
                && Objects.equals(friendName, other.friendName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), friendLoanId, friendName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CashTransaction{"
                + "id=" + getId()
                + ", asset=" + getAsset()
                + ", category=" + getCategory()
                + ", date=" + getDate()
                + ", description='" + getDescription() + '\''
                + ", notes='" + getNotes() + '\''
                + ", friendLoanId=" + friendLoanId
                + ", friendName='" + friendName + '\''
                + '}';
    }

    /**
     * Creates a new builder for constructing a {@link CashTransaction}.
     *
     * @return a new empty builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder initialized with values from an existing {@link CashTransaction}.
     *
     * @param prototype
     *            the existing transaction to copy values from, must not be null
     * @return a builder pre-filled with the prototype's values
     */
    public static Builder builder(final CashTransaction prototype) {
        return new Builder(prototype);
    }

    /**
     * Builder for constructing immutable {@link CashTransaction} instances fluently.
     */
    public static final class Builder {

        private UUID id;
        private Asset asset;
        private Category category;
        private LocalDate date;
        private String description;
        private String notes;
        private UUID friendLoanId;
        private String friendName;

        /**
         * Creates an empty builder.
         */
        public Builder() {
        }

        /**
         * Creates a builder pre-populated with values from a prototype transaction.
         *
         * @param prototype
         *            the prototype transaction, must not be null
         */
        public Builder(final CashTransaction prototype) {
            Objects.requireNonNull(prototype, "prototype must not be null");
            this.id = prototype.getId();
            this.asset = prototype.getAsset();
            this.category = prototype.getCategory();
            this.date = prototype.getDate();
            this.description = prototype.getDescription();
            this.notes = prototype.getNotes();
            this.friendLoanId = prototype.getFriendLoanId().orElse(null);
            this.friendName = prototype.getFriendName().orElse(null);
        }

        /**
         * Sets the stable transaction identifier.
         *
         * @param transactionId
         *            the transaction id
         * @return this builder
         */
        public Builder id(final UUID transactionId) {
            this.id = transactionId;
            return this;
        }

        /**
         * Sets the monetary asset.
         *
         * @param monetaryAsset
         *            the monetary asset
         * @return this builder
         */
        public Builder asset(final Asset monetaryAsset) {
            this.asset = monetaryAsset;
            return this;
        }

        /**
         * Sets the transaction category.
         *
         * @param transactionCategory
         *            the category
         * @return this builder
         */
        public Builder category(final Category transactionCategory) {
            this.category = transactionCategory;
            return this;
        }

        /**
         * Sets the transaction date.
         *
         * @param transactionDate
         *            the date
         * @return this builder
         */
        public Builder date(final LocalDate transactionDate) {
            this.date = transactionDate;
            return this;
        }

        /**
         * Sets the description.
         *
         * @param transactionDescription
         *            the description
         * @return this builder
         */
        public Builder description(final String transactionDescription) {
            this.description = transactionDescription;
            return this;
        }

        /**
         * Sets the notes.
         *
         * @param transactionNotes
         *            the notes
         * @return this builder
         */
        public Builder notes(final String transactionNotes) {
            this.notes = transactionNotes;
            return this;
        }

        /**
         * Sets friend loan metadata.
         *
         * @param loanIdVal
         *            the friend loan id
         * @param friendNameVal
         *            the friend's name
         * @return this builder
         */
        public Builder friendLoan(final UUID loanIdVal, final String friendNameVal) {
            this.friendLoanId = loanIdVal;
            this.friendName = friendNameVal;
            return this;
        }

        /**
         * Sets the friend loan id.
         *
         * @param loanIdVal
         *            the friend loan id
         * @return this builder
         */
        public Builder friendLoanId(final UUID loanIdVal) {
            this.friendLoanId = loanIdVal;
            return this;
        }

        /**
         * Sets the friend loan id (convenience alias for {@link #friendLoanId(UUID)}).
         *
         * @param loanIdVal
         *            the friend loan id
         * @return this builder
         */
        public Builder loanId(final UUID loanIdVal) {
            return friendLoanId(loanIdVal);
        }

        /**
         * Sets the friend name.
         *
         * @param friendNameVal
         *            the friend's name
         * @return this builder
         */
        public Builder friendName(final String friendNameVal) {
            this.friendName = friendNameVal;
            return this;
        }

        /**
         * Builds the {@link CashTransaction} instance.
         *
         * @return a new immutable {@link CashTransaction}
         * @throws NullPointerException
         *             if asset, category, or date is null
         * @throws IllegalArgumentException
         *             if friend loan metadata is inconsistent
         */
        public CashTransaction build() {
            Objects.requireNonNull(this.asset, "asset must not be null");
            Objects.requireNonNull(this.category, "category must not be null");
            Objects.requireNonNull(this.date, "date must not be null");

            if (this.id != null) {
                return new CashTransaction(
                        this.id,
                        this.asset,
                        this.category,
                        this.date,
                        this.description,
                        this.notes,
                        this.friendLoanId,
                        this.friendName
                );
            }
            return new CashTransaction(
                    this.asset,
                    this.category,
                    this.date,
                    this.description,
                    this.notes,
                    this.friendLoanId,
                    this.friendName
            );
        }
    }
}
