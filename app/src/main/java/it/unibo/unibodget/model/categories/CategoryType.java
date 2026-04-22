package it.unibo.unibodget.model.categories;

/**
 * Represents the high‑level classification of a transaction category.
 *
 * This type allows the application to distinguish between:
 * - income categories (money received)
 * - expense categories (money spent)
 * - friend‑to‑friend loan categories
 * - bank‑related loan categories
 */
public enum CategoryType {
    INCOME,
    EXPENSE,
    FRIEND_LOAN,
    BANK_LOAN;
}
