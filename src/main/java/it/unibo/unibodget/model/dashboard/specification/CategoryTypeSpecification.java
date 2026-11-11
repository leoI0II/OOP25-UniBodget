package it.unibo.unibodget.model.dashboard.specification;

import java.util.Objects;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Specification matching transactions belonging to a specific {@link CategoryType}.
 */
public final class CategoryTypeSpecification implements TransactionSpecification {

    private final CategoryType expectedType;

    /**
     * Creates a new category type specification.
     *
     * @param expectedType the expected category type
     */
    public CategoryTypeSpecification(final CategoryType expectedType) {
        this.expectedType = Objects.requireNonNull(expectedType, "expectedType must not be null");
    }

    @Override
    public boolean isSatisfiedBy(final CashTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        return transaction.getCategory() != null && transaction.getCategory().getType() == expectedType;
    }
}
