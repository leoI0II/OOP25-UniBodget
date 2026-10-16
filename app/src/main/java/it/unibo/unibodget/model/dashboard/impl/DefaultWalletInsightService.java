package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.dashboard.api.WalletInsightService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of {@link WalletInsightService}.
 *
 * <p>
 * This service computes dashboard insights by comparing the
 * current month against the previous month.
 * </p>
 *
 * <p>
 * The current implementation produces three insights:
 * <ul>
 *   <li>spending change,</li>
 *   <li>savings change,</li>
 *   <li>income change.</li>
 * </ul>
 * </p>
 */
public final class DefaultWalletInsightService implements WalletInsightService {

    private final AbstractMonthlyInsightTemplate spendingTemplate = new SpendingInsightTemplate();
    private final AbstractMonthlyInsightTemplate savingsTemplate = new SavingsInsightTemplate();
    private final AbstractMonthlyInsightTemplate incomeTemplate = new IncomeInsightTemplate();

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
                spendingTemplate.generate(currentExpenses, previousExpenses),
                savingsTemplate.generate(currentSavings, previousSavings),
                incomeTemplate.generate(currentIncome, previousIncome)
        );
    }

    /**
     * Computes the total expenses for the specified month.
     *
     * <p>
     * Transactions of type {@link CategoryType#EXPENSE} and
     * {@link CategoryType#FRIEND_LOAN} are both counted as spending
     * and contribute their absolute amount.
     * </p>
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
     * <p>
     * Only transactions of type {@link CategoryType#INCOME} are counted,
     * and each contributes its absolute amount.
     * </p>
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
}

