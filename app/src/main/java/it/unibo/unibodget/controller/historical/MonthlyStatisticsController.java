package it.unibo.unibodget.controller.historical;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.transactions.MonthlyStatistics;
import it.unibo.unibodget.model.transactions.MonthlyStatistics.CategoryTotal;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.Transaction;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for computing monthly financial statistics from a
 * historical transaction ledger.
 * 
 * <p>
 * All filtering, grouping, and aggregation logic is performed here. The view
 * layer receives only fully prepared {@link MonthlyStatistics} objects, ready
 * for chart rendering or summary display.
 * 
 * <p>
 * Assumptions:
 * 
 * <ul>
 *     <li>All transactions in the ledger use the same currency (e.g., the user's base currency).</li>
 *     <li>Category totals are computed directly from transaction amounts.</li>
 *     <li>Income and expense totals are computed using absolute values.</li>
 * </ul>
 */
public final class MonthlyStatisticsController {

    private final Historical<? extends Transaction> historical;

    /**
     * Creates a new {@code MonthlyStatisticsController}.
     *
     * @param historical the transaction ledger used to compute monthly statistics;
     *                   must not be {@code null}
     */
    public MonthlyStatisticsController(final Historical<? extends Transaction> historical) {
        this.historical = historical;
    }

    /**
     * Computes aggregated financial statistics for the given month.
     * 
     * <p>
     * The computation includes:
     * 
     * <ul>
     *     <li>Filtering transactions belonging to the specified {@link YearMonth}</li>
     *     <li>Summing amounts per category</li>
     *     <li>Tracking category types (income/expense)</li>
     *     <li>Computing total income, total expenses, and net balance</li>
     *     <li>Producing a sorted list of {@link CategoryTotal} objects</li>
     * </ul>
     * 
     * <p>
     * Category totals are sorted in descending order by absolute amount, so charts
     * can display the most relevant categories first.
     *
     * @param month the month for which statistics should be computed; must not be {@code null}
     * @return a {@link MonthlyStatistics} instance containing all aggregated values
     */
    public MonthlyStatistics computeStatistics(final YearMonth month) {
        final List<? extends Transaction> monthTransactions = historical.filterByMonth(month);

        final Map<String, BigDecimal> sumsByCategory = new LinkedHashMap<>();
        final Map<String, CategoryType> typeByCategory = new LinkedHashMap<>();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        // Aggregate per-category totals and compute income/expense sums
        for (final Transaction transaction : monthTransactions) {
            final Category category = transaction.getCategory();
            final BigDecimal amount = transaction.getAsset().amount();
            final String categoryName = category.getName();

            // Sum category totals
            sumsByCategory.merge(categoryName, amount, BigDecimal::add);
            typeByCategory.put(categoryName, category.getType());

            // Compute income/expense totals using absolute values
            if (category.getType() == CategoryType.INCOME) {
                totalIncome = totalIncome.add(amount.abs());
            } else if (category.getType() == CategoryType.EXPENSE) {
                totalExpense = totalExpense.add(amount.abs());
            }
        }

        // Build category totals list
        final List<CategoryTotal> categoryTotals = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : sumsByCategory.entrySet()) {
            final String name = entry.getKey();
            categoryTotals.add(new CategoryTotal(name, typeByCategory.get(name), entry.getValue()));
        }

        // Sort categories by descending absolute total
        categoryTotals.sort((a, b) -> b.total().abs().compareTo(a.total().abs()));

        final BigDecimal netBalance = totalIncome.subtract(totalExpense);

        return new MonthlyStatistics(month, totalIncome, totalExpense, netBalance, categoryTotals);
    }
}
