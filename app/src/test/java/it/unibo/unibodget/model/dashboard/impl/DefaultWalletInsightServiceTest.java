package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Tests for {@link DefaultWalletInsightService}.
 */
class DefaultWalletInsightServiceTest {

    private DefaultWalletInsightService service;
    private Category incomeCategory;
    private Category expenseCategory;
    private Category friendLoanCategory;

    /**
     * Creates the shared service and category fixtures used by the tests.
     */
    @BeforeEach
    void setUp() {
        service = new DefaultWalletInsightService();
        incomeCategory = new Category(
                "Salary",
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );
        expenseCategory = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        friendLoanCategory = new Category(
                "Friend loan",
                new ARGBColor(0xFF9C27B0),
                CategoryType.FRIEND_LOAN
        );
    }

    /**
     * Verifies that lower spending in the current month is reported
     * as a positive spending insight.
     */
    @Test
    void shouldDetectLowerSpendingThanPreviousMonth() {
        final LocalDate now = LocalDate.now();
        final LocalDate lastMonth = now.minusMonths(1);

        final List<CashTransaction> transactions = List.of(
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-80.00")),
                        expenseCategory,
                        now,
                        "Groceries",
                        null
                ),
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                        expenseCategory,
                        lastMonth,
                        "Groceries",
                        null
                )
        );

        final WalletInsight spendingInsight = service.computeInsights(transactions).get(0);

        assertEquals("Spending", spendingInsight.title());
        assertEquals(InsightTrend.POSITIVE, spendingInsight.trend());
        assertEquals(0, new BigDecimal("20.00").compareTo(spendingInsight.deltaAmount()));
        assertEquals(0, new BigDecimal("20.00").compareTo(spendingInsight.deltaPercentage()));
    }

    /**
     * Verifies that higher savings in the current month are reported
     * as a positive savings insight.
     */
    @Test
    void shouldDetectHigherSavingsThanPreviousMonth() {
        final LocalDate now = LocalDate.now();
        final LocalDate lastMonth = now.minusMonths(1);

        final List<CashTransaction> transactions = List.of(
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("1000.00")),
                        incomeCategory,
                        now,
                        "Salary",
                        null
                ),
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-300.00")),
                        expenseCategory,
                        now,
                        "Food",
                        null
                ),
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("1000.00")),
                        incomeCategory,
                        lastMonth,
                        "Salary",
                        null
                ),
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-500.00")),
                        expenseCategory,
                        lastMonth,
                        "Food",
                        null
                )
        );

        final WalletInsight savingsInsight = service.computeInsights(transactions).get(1);

        assertEquals("Savings", savingsInsight.title());
        assertEquals(InsightTrend.POSITIVE, savingsInsight.trend());
        assertEquals(0, new BigDecimal("200.00").compareTo(savingsInsight.deltaAmount()));
    }

    /**
     * Verifies that friend-loan transactions contribute to spending insights
     * exactly like other expense-like transactions.
     */
    @Test
    void shouldCountFriendLoanAsExpenseForSpendingInsight() {
        final LocalDate now = LocalDate.now();
        final LocalDate lastMonth = now.minusMonths(1);
        final UUID loanId = UUID.randomUUID();

        final List<CashTransaction> transactions = List.of(
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-50.00")),
                        friendLoanCategory,
                        now,
                        "Loan to Marco",
                        null,
                        loanId,
                        "Marco"
                ),
                new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                        expenseCategory,
                        lastMonth,
                        "Groceries",
                        null
                )
        );

        final WalletInsight spendingInsight = service.computeInsights(transactions).get(0);

        assertEquals("Spending", spendingInsight.title());
        assertEquals(InsightTrend.POSITIVE, spendingInsight.trend());
        assertEquals(0, new BigDecimal("50.00").compareTo(spendingInsight.deltaAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(spendingInsight.deltaPercentage()));
    }

    /**
     * Verifies that when no transactions exist in either month,
     * the service produces neutral informational insights.
     */
    @Test
    void shouldReturnNeutralInsightsWhenNoTransactionsExist() {
        final List<WalletInsight> insights = service.computeInsights(List.of());

        assertEquals(3, insights.size());

        assertEquals("Spending", insights.get(0).title());
        assertEquals(InsightTrend.NEUTRAL, insights.get(0).trend());

        assertEquals("Savings", insights.get(1).title());
        assertEquals(InsightTrend.NEUTRAL, insights.get(1).trend());

        assertEquals("Income", insights.get(2).title());
        assertEquals(InsightTrend.NEUTRAL, insights.get(2).trend());
    }
}