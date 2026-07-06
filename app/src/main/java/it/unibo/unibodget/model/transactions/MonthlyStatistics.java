package it.unibo.unibodget.model.transactions;

import it.unibo.unibodget.model.categories.CategoryType;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents aggregated financial statistics for a single calendar month.
 * <p>
 * Instances of {@code MonthlyStatistics} are produced by the
 * {@code MonthlyStatisticsController} from a historical ledger. This ensures
 * that the view layer never needs to filter, group, or sum transactions itself.
 * <br>
 * The class provides:
 * <ul>
 *     <li>total income for the month</li>
 *     <li>total expenses for the month</li>
 *     <li>net balance (income minus expenses)</li>
 *     <li>category-level totals, already grouped and ready for chart rendering</li>
 * </ul>
 * <p>
 * All fields are immutable, making the object safe to pass directly to UI components.
 */
public final class MonthlyStatistics {

    private final YearMonth month;
    private final BigDecimal totalIncome;
    private final BigDecimal totalExpense;
    private final BigDecimal netBalance;
    private final List<CategoryTotal> categoryTotals;

    /**
     * Creates a new {@code MonthlyStatistics} instance containing pre-aggregated
     * financial data for a specific month.
     *
     * @param month          the calendar month represented by this statistics object;
     *                       must not be {@code null}
     * @param totalIncome    the total income for the month; must not be {@code null}
     * @param totalExpense   the total expenses for the month; must not be {@code null}
     * @param netBalance     the net balance (income minus expenses); must not be {@code null}
     * @param categoryTotals a list of category-level totals already aggregated;
     *                       must not be {@code null}. A defensive copy is created.
     */
    public MonthlyStatistics(YearMonth month, BigDecimal totalIncome, BigDecimal totalExpense,
                             BigDecimal netBalance, List<CategoryTotal> categoryTotals) {
        this.month = Objects.requireNonNull(month);
        this.totalIncome = totalIncome;
        this.totalExpense = Objects.requireNonNull(totalExpense);
        this.netBalance = Objects.requireNonNull(netBalance);
        this.categoryTotals = List.copyOf(categoryTotals);
    }

    /**
     * Returns the calendar month represented by this statistics object.
     *
     * @return the {@link YearMonth} of the aggregated data
     */
    public YearMonth getMonth() {
        return month;
    }

    /**
     * Returns the total income for the month.
     *
     * @return the aggregated income amount
     */
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    /**
     * Returns the total expenses for the month.
     *
     * @return the aggregated expense amount
     */
    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    /**
     * Returns the net balance for the month (income minus expenses).
     *
     * @return the net balance
     */
    public BigDecimal getNetBalance() {
        return netBalance;
    }

    /**
     * Returns an immutable list of category-level totals.
     * <p>
     * Each element contains:
     * <ul>
     *     <li>the category name</li>
     *     <li>the category type (income or expense)</li>
     *     <li>the aggregated total for that category</li>
     * </ul>
     * <p>
     * The list is already sorted/grouped by the controller, so the view layer
     * can directly use it for chart rendering.
     *
     * @return an unmodifiable list of {@link CategoryTotal} objects
     */
    public List<CategoryTotal> getCategoryTotals() {
        return Collections.unmodifiableList(categoryTotals);
    }

    /**
     * Represents the aggregated total for a single category within a month.
     * <p>
     * This record is used by chart views to display category-level breakdowns
     * without needing to re-derive category type or perform additional grouping.
     *
     * @param categoryName the human-readable name of the category
     * @param type         the {@link CategoryType} (income or expense), used by charts
     *                     to apply different colors or styles
     * @param total        the aggregated monetary total for the category
     */
    public record CategoryTotal(String categoryName, CategoryType type, BigDecimal total) {
    }
}
