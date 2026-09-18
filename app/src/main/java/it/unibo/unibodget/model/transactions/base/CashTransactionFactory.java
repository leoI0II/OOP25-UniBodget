package it.unibo.unibodget.model.transactions.base;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Factory responsible for creating valid {@link CashTransaction} instances
 * from user-oriented unsigned input values.
 *
 * <p>
 * This factory centralizes the business rules that determine the signed amount
 * of a transaction according to its semantic type. The UI can therefore collect
 * always-positive amounts, while the model layer guarantees consistent sign
 * handling for both default and user-defined categories.
 * </p>
 */
public final class CashTransactionFactory {

    /**
     * Creates a standard cash transaction.
     *
     * <p>
     * The provided amount is expected to be unsigned. The sign is derived from
     * the selected transaction type after verifying that the category belongs
     * to the same {@link CategoryType}.
     * </p>
     *
     * @param unsignedAmount
     *            the positive amount entered by the user
     * @param currency
     *            the currency of the transaction
     * @param selectedType
     *            the semantic type chosen by the user
     * @param category
     *            the selected category
     * @param date
     *            the transaction date
     * @param description
     *            the transaction description
     * @param notes
     *            optional notes, may be null
     * @return a valid cash transaction
     */
    public CashTransaction create(
            final BigDecimal unsignedAmount,
            final CurrencyUnit currency,
            final CategoryType selectedType,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes) {

        Objects.requireNonNull(unsignedAmount, "unsignedAmount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(selectedType, "selectedType must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(date, "date must not be null");

        validatePositiveAmount(unsignedAmount);
        validateTypeConsistency(selectedType, category);

        if (selectedType == CategoryType.FRIEND_LOAN) {
            throw new IllegalArgumentException(
                    "FRIEND_LOAN transactions require friend-loan metadata."
            );
        }

        final BigDecimal signedAmount = normalizeStandardAmount(unsignedAmount, selectedType);
        final Asset asset = Asset.of(currency, signedAmount);

        return new CashTransaction(asset, category, date, description, notes);
    }

    /**
     * Creates a new friend-loan disbursement transaction.
     *
     * <p>
     * This represents money given to a friend, therefore the stored amount is
     * negative and a brand-new friend-loan identifier is generated.
     * </p>
     *
     * @param unsignedAmount
     *            the positive amount entered by the user
     * @param currency
     *            the currency of the transaction
     * @param selectedType
     *            the semantic type chosen by the user
     * @param category
     *            the selected category
     * @param date
     *            the transaction date
     * @param description
     *            the transaction description
     * @param notes
     *            optional notes, may be null
     * @param friendName
     *            the related friend's name
     * @return a valid friend-loan cash transaction representing a new loan
     */
    public CashTransaction createFriendLoan(
            final BigDecimal unsignedAmount,
            final CurrencyUnit currency,
            final CategoryType selectedType,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final String friendName) {

        Objects.requireNonNull(unsignedAmount, "unsignedAmount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(selectedType, "selectedType must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(friendName, "friendName must not be null");

        validatePositiveAmount(unsignedAmount);
        validateTypeConsistency(selectedType, category);
        validateFriendLoanType(selectedType);

        if (friendName.isBlank()) {
            throw new IllegalArgumentException("friendName must not be blank");
        }

        final Asset asset = Asset.of(currency, unsignedAmount.abs().negate());

        return new CashTransaction(
                asset,
                category,
                date,
                description,
                notes,
                UUID.randomUUID(),
                friendName
        );
    }

    /**
     * Creates a friend-loan repayment transaction linked to an existing loan.
     *
     * <p>
     * This represents money received back from a friend, therefore the stored
     * amount is positive and the transaction must reuse the existing
     * friend-loan identifier.
     * </p>
     *
     * @param unsignedAmount
     *            the positive repayment amount entered by the user
     * @param currency
     *            the currency of the transaction
     * @param selectedType
     *            the semantic type chosen by the user
     * @param category
     *            the selected category
     * @param date
     *            the transaction date
     * @param description
     *            the transaction description
     * @param notes
     *            optional notes, may be null
     * @param friendName
     *            the related friend's name
     * @param friendLoanId
     *            the identifier of the existing friend loan
     * @return a valid friend-loan repayment transaction
     */
    public CashTransaction createFriendLoanRepayment(
            final BigDecimal unsignedAmount,
            final CurrencyUnit currency,
            final CategoryType selectedType,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes,
            final String friendName,
            final UUID friendLoanId) {

        Objects.requireNonNull(unsignedAmount, "unsignedAmount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(selectedType, "selectedType must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(friendName, "friendName must not be null");
        Objects.requireNonNull(friendLoanId, "friendLoanId must not be null");

        validatePositiveAmount(unsignedAmount);
        validateTypeConsistency(selectedType, category);
        validateFriendLoanType(selectedType);

        if (friendName.isBlank()) {
            throw new IllegalArgumentException("friendName must not be blank");
        }

        final Asset asset = Asset.of(currency, unsignedAmount.abs());

        return new CashTransaction(
                asset,
                category,
                date,
                description,
                notes,
                friendLoanId,
                friendName
        );
    }

    /**
     * Validates that the amount entered by the user is strictly positive.
     *
     * @param unsignedAmount
     *            the amount to validate
     */
    private void validatePositiveAmount(final BigDecimal unsignedAmount) {
        if (unsignedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
    }

    /**
     * Validates that the selected transaction type matches the selected category type.
     *
     * @param selectedType
     *            the selected transaction type
     * @param category
     *            the selected category
     */
    private void validateTypeConsistency(
            final CategoryType selectedType,
            final Category category) {
        if (category.getType() != selectedType) {
            throw new IllegalArgumentException(
                    "Selected category type does not match the selected transaction type."
            );
        }
    }

    /**
     * Validates that the provided type is the friend-loan type.
     *
     * @param selectedType
     *            the selected transaction type
     */
    private void validateFriendLoanType(final CategoryType selectedType) {
        if (selectedType != CategoryType.FRIEND_LOAN) {
            throw new IllegalArgumentException(
                    "Friend-loan creation requires FRIEND_LOAN type."
            );
        }
    }

    /**
     * Converts an unsigned amount into the stored signed amount according to the
     * selected standard transaction type.
     *
     * @param unsignedAmount
     *            the positive amount entered by the user
     * @param transactionType
     *            the semantic transaction type
     * @return the normalized signed amount
     */
    private BigDecimal normalizeStandardAmount(
            final BigDecimal unsignedAmount,
            final CategoryType transactionType) {

        final BigDecimal normalized = unsignedAmount.abs();

        return switch (transactionType) {
            case INCOME -> normalized;
            case EXPENSE -> normalized.negate();
            case TRANSFER -> normalized.negate();
            case FRIEND_LOAN -> throw new IllegalArgumentException(
                    "FRIEND_LOAN must be created through dedicated methods."
            );
        };
    }
}