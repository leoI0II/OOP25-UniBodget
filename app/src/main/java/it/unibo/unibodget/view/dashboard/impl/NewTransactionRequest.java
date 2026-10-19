package it.unibo.unibodget.view.dashboard.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Immutable input collected by the new-transaction dialog. */
public final class NewTransactionRequest {

    private final BigDecimal unsignedAmount;
    private final CategoryType transactionType;
    private final Category category;
    private final LocalDate date;
    private final String description;
    private final String notes;
    private final String friendName;
    private final FriendLoanOperation friendLoanOperation;
    private final UUID existingFriendLoanId;

    /**
     * Constructs a new transaction request.
     *
     * @param unsignedAmount the positive transaction amount
     * @param transactionType the transaction category type
     * @param category the transaction category
     * @param date the transaction date
     * @param description the non-blank description
     * @param notes optional additional notes
     * @param friendName the optional friend name for a new loan
     * @param friendLoanOperation optional friend-loan operation
     * @param existingFriendLoanId optional identifier of the existing loan to repay
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Payload carries category entity")
    public NewTransactionRequest(
            final BigDecimal unsignedAmount,
            final CategoryType transactionType,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final String friendName,
            final FriendLoanOperation friendLoanOperation,
            final UUID existingFriendLoanId) {

        this.unsignedAmount = Objects.requireNonNull(unsignedAmount);
        this.transactionType = Objects.requireNonNull(transactionType);
        this.category = Objects.requireNonNull(category);
        this.date = Objects.requireNonNull(date);
        this.description = Objects.requireNonNull(description);
        this.notes = notes;
        this.friendName = friendName;
        this.friendLoanOperation = friendLoanOperation;
        this.existingFriendLoanId = existingFriendLoanId;

        validate();
    }

    private void validate() {
        if (unsignedAmount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        if (description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank.");
        }

        if (transactionType == CategoryType.FRIEND_LOAN) {
            if (friendLoanOperation == null) {
                throw new IllegalArgumentException("Friend-loan operation is required.");
            }

            switch (friendLoanOperation) {
                case NEW_LOAN -> {
                    if (friendName == null || friendName.isBlank()) {
                        throw new IllegalArgumentException(
                                "Friend name is required for a new loan.");
                    }
                    if (existingFriendLoanId != null) {
                        throw new IllegalArgumentException(
                                "Existing loan id must be null for a new loan.");
                    }
                }
                case REPAYMENT -> {
                    if (existingFriendLoanId == null) {
                        throw new IllegalArgumentException(
                                "Existing loan id is required for a repayment.");
                    }
                }
            }
        } else {
            if (friendLoanOperation != null || existingFriendLoanId != null) {
                throw new IllegalArgumentException(
                        "Friend-loan metadata must be null for non-friend-loan transactions.");
            }
        }
    }

    /**
     * Returns the unsigned transaction amount.
     *
     * @return the amount
     */
    public BigDecimal unsignedAmount() {
        return unsignedAmount;
    }

    /**
     * Returns the transaction category type.
     *
     * @return the category type
     */
    public CategoryType transactionType() {
        return transactionType;
    }

    /**
     * Returns the transaction category.
     *
     * @return the category
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Payload carries category entity")
    public Category category() {
        return category;
    }

    /**
     * Returns the transaction date.
     *
     * @return the date
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Returns the transaction description.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the transaction notes.
     *
     * @return the notes
     */
    public String notes() {
        return notes;
    }

    /**
     * Returns the friend name.
     *
     * @return the friend name
     */
    public String friendName() {
        return friendName;
    }

    /**
     * Returns the friend-loan operation.
     *
     * @return the friend-loan operation
     */
    public FriendLoanOperation friendLoanOperation() {
        return friendLoanOperation;
    }

    /**
     * Returns the identifier of an existing friend loan.
     *
     * @return the loan identifier
     */
    public UUID existingFriendLoanId() {
        return existingFriendLoanId;
    }
}
