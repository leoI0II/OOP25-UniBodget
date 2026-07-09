package it.unibo.unibodget.model.transactions.base;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;

/**
 * Represents a cash transaction recorded inside a cash wallet.
 *
 * <p>
 * A cash transaction may optionally contain friend-loan metadata. This allows
 * friend-loan movements to be tracked and persisted exactly like any other
 * wallet transaction.</p>
 */
public final class CashTransaction extends Transaction {

    private final UUID friendLoanId;
    private final String friendName;

    /**
     * Creates a new standard cash transaction with no friend-loan metadata.
     *
     * @param asset the monetary value associated with the transaction; must not
     * be null
     * @param category the category describing the nature of the transaction;
     * must not be null
     * @param date the date on which the transaction occurred; must not be null
     * @param description a short human-readable description of the transaction;
     * may be null
     * @param notes optional additional notes or comments; may be null
     */
    public CashTransaction(
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes) {
        this(asset, category, date, description, notes, null, null);
    }

    public static CashTransaction of(
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes
    ) {
        return new CashTransaction(asset, category, date, description, notes);
    }

    /**
     * Creates a new cash transaction, optionally linked to a friend loan.
     *
     * @param asset the monetary value associated with the transaction; must not
     * be null
     * @param category the category describing the nature of the transaction;
     * must not be null
     * @param date the date on which the transaction occurred; must not be null
     * @param description a short human-readable description of the transaction;
     * may be null
     * @param notes optional additional notes or comments; may be null
     * @param friendLoanId the identifier of the linked friend loan; may be null
     * @param friendName the name of the related friend; may be null
     * @throws IllegalArgumentException if friend-loan metadata is provided for
     * a transaction whose category type is not
     * {@link CategoryType#FRIEND_LOAN}, or if only one of the two friend-loan
     * fields is provided
     */
    public CashTransaction(
            final Asset asset,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final UUID friendLoanId,
            final String friendName) {
        super(asset, category, date, description, notes);

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
     * Returns the identifier of the linked friend loan, if present.
     *
     * @return an {@link Optional} containing the friend-loan identifier
     */
    public Optional<UUID> getFriendLoanId() {
        return Optional.ofNullable(friendLoanId);
    }

    /**
     * Returns the name of the related friend, if present.
     *
     * @return an {@link Optional} containing the friend's name
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

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final CashTransaction other = (CashTransaction) obj;
        return Objects.equals(friendLoanId, other.friendLoanId)
                && Objects.equals(friendName, other.friendName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), friendLoanId, friendName);
    }

    @Override
    public String toString() {
        return "CashTransaction{"
                + "asset=" + getAsset()
                + ", category=" + getCategory()
                + ", date=" + getDate()
                + ", description='" + getDescription() + '\''
                + ", notes='" + getNotes() + '\''
                + ", friendLoanId=" + friendLoanId
                + ", friendName='" + friendName + '\''
                + '}';
    }
}
