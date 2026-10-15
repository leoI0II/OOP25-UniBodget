package it.unibo.unibodget.model.dashboard.specification;

import java.util.Objects;
import java.util.function.Predicate;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Specification pattern interface for filtering {@link CashTransaction} domain objects.
 *
 * <p>
 * Specifications encapsulate domain business predicates and can be chained
 * using logical operations (AND, OR, NOT).
 * </p>
 */
@FunctionalInterface
public interface TransactionSpecification extends Predicate<CashTransaction> {

    /**
     * Evaluates whether the given transaction satisfies this specification.
     *
     * @param transaction the cash transaction to evaluate; must not be {@code null}
     * @return {@code true} if the transaction satisfies the rule, {@code false} otherwise
     */
    boolean isSatisfiedBy(CashTransaction transaction);

    @Override
    default boolean test(final CashTransaction transaction) {
        return isSatisfiedBy(transaction);
    }

    /**
     * Returns a composite specification representing the logical conjunction (AND)
     * of this specification and another.
     *
     * @param other the other specification to combine with
     * @return a composite AND specification
     */
    default TransactionSpecification and(final TransactionSpecification other) {
        Objects.requireNonNull(other, "other specification must not be null");
        return transaction -> isSatisfiedBy(transaction) && other.isSatisfiedBy(transaction);
    }

    /**
     * Returns a composite specification representing the logical disjunction (OR)
     * of this specification and another.
     *
     * @param other the other specification to combine with
     * @return a composite OR specification
     */
    default TransactionSpecification or(final TransactionSpecification other) {
        Objects.requireNonNull(other, "other specification must not be null");
        return transaction -> isSatisfiedBy(transaction) || other.isSatisfiedBy(transaction);
    }

    /**
     * Returns a specification representing the logical negation (NOT) of this specification.
     *
     * @return the negated specification
     */
    default TransactionSpecification not() {
        return transaction -> !isSatisfiedBy(transaction);
    }

    /**
     * Returns a specification that matches all transactions.
     *
     * @return an unconditional matching specification
     */
    static TransactionSpecification all() {
        return transaction -> true;
    }
}
