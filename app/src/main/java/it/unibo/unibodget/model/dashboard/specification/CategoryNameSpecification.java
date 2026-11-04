package it.unibo.unibodget.model.dashboard.specification;

import java.util.Locale;
import java.util.Objects;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Specification matching transactions whose category name contains the given string
 * (case-insensitive and trimmed).
 */
public final class CategoryNameSpecification implements TransactionSpecification {

    private final String normalizedFilter;

    /**
     * Creates a new category name specification.
     *
     * @param categoryName the category name substring to match
     */
    public CategoryNameSpecification(final String categoryName) {
        Objects.requireNonNull(categoryName, "categoryName must not be null");
        this.normalizedFilter = categoryName.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isSatisfiedBy(final CashTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        if (transaction.getCategory() == null || transaction.getCategory().getName() == null) {
            return false;
        }
        final String categoryName = transaction.getCategory().getName().trim().toLowerCase(Locale.ROOT);
        return categoryName.contains(normalizedFilter);
    }
}
