package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of {@link CategoryService}.
 *
 * <p>This implementation supports both manually supplied category summaries
 * and summaries derived from a list of {@link CashTransaction}s.</p>
 */
public final class DefaultCategoryService implements CategoryService {

    private final Map<String, BigDecimal> categorySummaries;

    /**
     * Creates an empty category service.
     */
    public DefaultCategoryService() {
        this(Map.of());
    }

    /**
     * Creates a category service initialized with the provided summaries.
     *
     * @param categorySummaries
     *            the initial category summary values
     */
    public DefaultCategoryService(final Map<String, BigDecimal> categorySummaries) {
        this.categorySummaries = new LinkedHashMap<>(Objects.requireNonNull(categorySummaries));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, BigDecimal> getCategorySummaries() {
        return Collections.unmodifiableMap(categorySummaries);
    }

    /**
     * Adds or replaces a category summary value.
     *
     * @param categoryName
     *            the category name
     * @param amount
     *            the aggregated amount associated with the category
     */
    public void putCategorySummary(final String categoryName, final BigDecimal amount) {
        categorySummaries.put(
                Objects.requireNonNull(categoryName),
                Objects.requireNonNull(amount)
        );
    }

    /**
     * Recomputes the category summaries from the given cash transactions.
     *
     * <p>Each transaction contributes its absolute monetary amount to the
     * corresponding category total.</p>
     *
     * @param transactions
     *            the transactions to aggregate
     */
    @Override
    public void recomputeFromTransactions(final List<CashTransaction> transactions) {
        Objects.requireNonNull(transactions);
        categorySummaries.clear();

        for (final CashTransaction transaction : transactions) {
            final String categoryName = transaction.getCategory().getName();
            final BigDecimal amount = transaction.getAsset().amount().abs();
            categorySummaries.merge(categoryName, amount, BigDecimal::add);
        }
    }
}