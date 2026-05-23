package it.unibo.unibodget.view.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;

/**
 * Immutable input collected by the new-transaction dialog.
 */
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
                        throw new IllegalArgumentException("Friend name is required for a new loan.");
                    }
                    if (existingFriendLoanId != null) {
                        throw new IllegalArgumentException("Existing loan id must be null for a new loan.");
                    }
                }
                case REPAYMENT -> {
                    if (existingFriendLoanId == null) {
                        throw new IllegalArgumentException("Existing loan id is required for a repayment.");
                    }
                }
            }
        } else {
            if (friendLoanOperation != null || existingFriendLoanId != null) {
                throw new IllegalArgumentException(
                        "Friend-loan metadata must be null for non-friend-loan transactions."
                );
            }
        }
    }

    public BigDecimal unsignedAmount() {
        return unsignedAmount;
    }

    public CategoryType transactionType() {
        return transactionType;
    }

    public Category category() {
        return category;
    }

    public LocalDate date() {
        return date;
    }

    public String description() {
        return description;
    }

    public String notes() {
        return notes;
    }

    public String friendName() {
        return friendName;
    }

    public FriendLoanOperation friendLoanOperation() {
        return friendLoanOperation;
    }

    public UUID existingFriendLoanId() {
        return existingFriendLoanId;
    }
}