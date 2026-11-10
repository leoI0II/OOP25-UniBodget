package it.unibo.unibodget.model.categories;

/**
 * Represents the high‑level classification of a transaction category.
 *
 * <p>This type allows the application to distinguish between: - income categories (money received)
 * - expense categories (money spent) - friend‑to‑friend loan categories
 */
public enum CategoryType {
    /** Money received. */
    INCOME,
    /** Money spent. */
    EXPENSE,
    /** Internal transfer between accounts. */
    TRANSFER,
    /** Loan or repayment between friends. */
    FRIEND_LOAN
}
