package it.unibo.unibodget.view.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;

/**
 * Immutable input collected by the new-transaction dialog.
 *
 * @param unsignedAmount the positive amount entered by the user
 * @param transactionType the chosen category type
 * @param category the chosen category
 * @param date the transaction date
 * @param description the transaction description
 * @param notes optional additional notes
 * @param friendName optional friend name for a new loan
 * @param friendLoanOperation optional friend loan operation type
 * @param existingFriendLoanId optional friend loan identifier for repayments
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Request record carries category reference from UI dialog")
public record NewTransactionRequest(
        BigDecimal unsignedAmount,
        CategoryType transactionType,
        Category category,
        LocalDate date,
        String description,
        String notes,
        String friendName,
        FriendLoanOperation friendLoanOperation,
        UUID existingFriendLoanId) {

    /**
     * Compact canonical constructor with domain validation.
     */
    public NewTransactionRequest {
        Objects.requireNonNull(unsignedAmount, "unsignedAmount must not be null");
        Objects.requireNonNull(transactionType, "transactionType must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(description, "description must not be null");

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
}
