package it.unibo.unibodget.model.dashboard.specification;

import java.util.Locale;
import java.util.Objects;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Specification matching transactions whose description or notes contain a given keyword (case-insensitive).
 */
public final class KeywordSpecification implements TransactionSpecification {

    private final String normalizedKeyword;

    /**
     * Creates a keyword specification.
     *
     * @param keyword the search keyword
     */
    public KeywordSpecification(final String keyword) {
        Objects.requireNonNull(keyword, "keyword must not be null");
        this.normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isSatisfiedBy(final CashTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        final String description = transaction.getDescription() == null
                ? ""
                : transaction.getDescription().toLowerCase(Locale.ROOT);
        final String notes = transaction.getNotes() == null
                ? ""
                : transaction.getNotes().toLowerCase(Locale.ROOT);
        return description.contains(normalizedKeyword) || notes.contains(normalizedKeyword);
    }
}
