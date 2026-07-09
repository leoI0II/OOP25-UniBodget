package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Service responsible for exposing aggregated totals by category
 * for dashboard use.
 */
public interface CategoryService {

    /**
     * Returns the aggregated amounts by category.
     *
     * @return an unmodifiable map from category name to aggregated amount
     */
    Map<String, BigDecimal> getCategorySummaries();

    /**
     * Recomputes the category summaries from the given cash transactions.
     *
     * @param transactions
     *            the transactions to aggregate
     */
    void recomputeFromTransactions(List<CashTransaction> transactions);
}