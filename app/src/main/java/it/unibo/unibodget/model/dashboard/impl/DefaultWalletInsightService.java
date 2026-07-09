package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.dashboard.api.WalletInsightService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of {@link WalletInsightService}.
 *
 * <p>This service computes dashboard insights by comparing the
 * current month against the previous month.</p>
 *
 * <p>The current implementation produces three insights:
 * <ul>
 *   <li>spending change,</li>
 *   <li>savings change,</li>
 *   <li>income change.</li>
 * </ul>
 * </p>
 */
public final class DefaultWalletInsightService implements WalletInsightService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int PERCENT_SCALE = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WalletInsight> computeInsights(final List<CashTransaction> transactions) {
        Objects.requireNonNull(transactions, "transactions must not be null");

        final LocalDate today = LocalDate.now();
        final LocalDate previousMonthDate = today.minusMonths(1);

        final BigDecimal currentExpenses =
                totalExpensesOfMonth(transactions, today.getYear(), today.getMonthValue());
        final BigDecimal previousExpenses =
                totalExpensesOfMonth(transactions, previousMonthDate.getYear(), previousMonthDate.getMonthValue());

        final BigDecimal currentIncome =
                totalIncomeOfMonth(transactions, today.getYear(), today.getMonthValue());
        final BigDecimal previousIncome =
                totalIncomeOfMonth(transactions, previousMonthDate.getYear(), previousMonthDate.getMonthValue());

        final BigDecimal currentSavings = currentIncome.subtract(currentExpenses);
        final BigDecimal previousSavings = previousIncome.subtract(previousExpenses);

        return List.of(
                buildSpendingInsight(currentExpenses, previousExpenses),
                buildSavingsInsight(currentSavings, previousSavings),
                buildIncomeInsight(currentIncome, previousIncome)
        );
    }

    /**
     * Computes the total expenses for the specified month.
     *
     * <p>Transactions of type {@link CategoryType#EXPENSE} and
     * {@link CategoryType#FRIEND_LOAN} are both counted as spending
     * and contribute their absolute amount.</p>
     *
     * @param transactions
     *            the transactions to inspect
     * @param year
     *            the target year
     * @param month
     *            the target month, from 1 to 12
     * @return the total expenses for the given month
     */
    private BigDecimal totalExpensesOfMonth(
            final List<CashTransaction> transactions,
            final int year,
            final int month) {
        return transactions.stream()
                .filter(transaction -> isInMonth(transaction, year, month))
                .filter(transaction -> {
                    final CategoryType type = transaction.getCategory().getType();
                    return type == CategoryType.EXPENSE || type == CategoryType.FRIEND_LOAN;
                })
                .map(transaction -> transaction.getAsset().amount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Computes the total income for the specified month.
     *
     * <p>Only transactions of type {@link CategoryType#INCOME} are counted,
     * and each contributes its absolute amount.</p>
     *
     * @param transactions
     *            the transactions to inspect
     * @param year
     *            the target year
     * @param month
     *            the target month, from 1 to 12
     * @return the total income for the given month
     */
    private BigDecimal totalIncomeOfMonth(
            final List<CashTransaction> transactions,
            final int year,
            final int month) {
        return transactions.stream()
                .filter(transaction -> isInMonth(transaction, year, month))
                .filter(transaction -> transaction.getCategory().getType() == CategoryType.INCOME)
                .map(transaction -> transaction.getAsset().amount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns whether the given transaction belongs to the specified month.
     *
     * @param transaction
     *            the transaction to inspect
     * @param year
     *            the target year
     * @param month
     *            the target month, from 1 to 12
     * @return {@code true} if the transaction date matches the given month
     */
    private boolean isInMonth(final CashTransaction transaction, final int year, final int month) {
        return transaction.getDate().getYear() == year
                && transaction.getDate().getMonthValue() == month;
    }

    /**
     * Builds the spending insight by comparing current and previous expenses.
     *
     * @param current
     *            the current-month spending total
     * @param previous
     *            the previous-month spending total
     * @return the spending insight
     */
    private WalletInsight buildSpendingInsight(final BigDecimal current, final BigDecimal previous) {
        final BigDecimal delta = current.subtract(previous);
        final BigDecimal absoluteDelta = delta.abs();
        final BigDecimal percentage = computePercentageChange(previous, absoluteDelta);

        if (previous.compareTo(BigDecimal.ZERO) == 0 && current.compareTo(BigDecimal.ZERO) == 0) {
            return new WalletInsight(
                    "Spending",
                    "No spending recorded this month or last month.",
                    InsightTrend.NEUTRAL,
                    BigDecimal.ZERO,
                    null
            );
        }

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return new WalletInsight(
                    "Spending",
                    "You spent " + current + " this month; no spending was recorded last month.",
                    InsightTrend.NEUTRAL,
                    current,
                    null
            );
        }

        if (delta.compareTo(BigDecimal.ZERO) < 0) {
            return new WalletInsight(
                    "Spending",
                    "You spent " + percentage + "% less than last month (" + absoluteDelta + " less).",
                    InsightTrend.POSITIVE,
                    absoluteDelta,
                    percentage
            );
        }

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            return new WalletInsight(
                    "Spending",
                    "You spent " + percentage + "% more than last month (" + absoluteDelta + " more).",
                    InsightTrend.NEGATIVE,
                    absoluteDelta,
                    percentage
            );
        }

        return new WalletInsight(
                "Spending",
                "Your spending is in line with last month.",
                InsightTrend.NEUTRAL,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    /**
     * Builds the savings insight by comparing current and previous savings.
     *
     * <p>Savings are computed as income minus expenses for the month.</p>
     *
     * @param current
     *            the current-month savings
     * @param previous
     *            the previous-month savings
     * @return the savings insight
     */
    private WalletInsight buildSavingsInsight(final BigDecimal current, final BigDecimal previous) {
        final BigDecimal delta = current.subtract(previous);
        final BigDecimal absoluteDelta = delta.abs();
        final BigDecimal percentage = computePercentageChange(previous.abs(), absoluteDelta);

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            return new WalletInsight(
                    "Savings",
                    "You saved " + absoluteDelta + " more than last month.",
                    InsightTrend.POSITIVE,
                    absoluteDelta,
                    percentage
            );
        }

        if (delta.compareTo(BigDecimal.ZERO) < 0) {
            return new WalletInsight(
                    "Savings",
                    "You saved " + absoluteDelta + " less than last month.",
                    InsightTrend.NEGATIVE,
                    absoluteDelta,
                    percentage
            );
        }

        return new WalletInsight(
                "Savings",
                "Your savings match last month.",
                InsightTrend.NEUTRAL,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    /**
     * Builds the income insight by comparing current and previous income.
     *
     * @param current
     *            the current-month income total
     * @param previous
     *            the previous-month income total
     * @return the income insight
     */
    private WalletInsight buildIncomeInsight(final BigDecimal current, final BigDecimal previous) {
        final BigDecimal delta = current.subtract(previous);
        final BigDecimal absoluteDelta = delta.abs();
        final BigDecimal percentage = computePercentageChange(previous, absoluteDelta);

        if (previous.compareTo(BigDecimal.ZERO) == 0 && current.compareTo(BigDecimal.ZERO) == 0) {
            return new WalletInsight(
                    "Income",
                    "No income recorded this month or last month.",
                    InsightTrend.NEUTRAL,
                    BigDecimal.ZERO,
                    null
            );
        }

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return new WalletInsight(
                    "Income",
                    "You recorded " + current + " income this month; no income was recorded last month.",
                    InsightTrend.POSITIVE,
                    current,
                    null
            );
        }

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            return new WalletInsight(
                    "Income",
                    "Your income increased by " + percentage + "% compared to last month.",
                    InsightTrend.POSITIVE,
                    absoluteDelta,
                    percentage
            );
        }

        if (delta.compareTo(BigDecimal.ZERO) < 0) {
            return new WalletInsight(
                    "Income",
                    "Your income decreased by " + percentage + "% compared to last month.",
                    InsightTrend.NEGATIVE,
                    absoluteDelta,
                    percentage
            );
        }

        return new WalletInsight(
                "Income",
                "Your income matches last month.",
                InsightTrend.NEUTRAL,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    /**
     * Computes a percentage change using the provided base and delta.
     *
     * <p>The returned value is expressed as a percentage in the range
     * expected for dashboard display, for example {@code 20.00} for 20%.</p>
     *
     * @param base
     *            the reference amount against which the change is measured
     * @param delta
     *            the absolute change amount
     * @return the percentage change, or {@code null} if the base is zero
     */
    private BigDecimal computePercentageChange(final BigDecimal base, final BigDecimal delta) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return delta.multiply(ONE_HUNDRED).divide(base, PERCENT_SCALE, RoundingMode.HALF_UP);
    }
}