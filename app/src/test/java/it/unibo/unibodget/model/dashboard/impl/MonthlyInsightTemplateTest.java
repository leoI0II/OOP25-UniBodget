package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MonthlyInsightTemplate} specializations:
 * {@link SpendingInsightTemplate}, {@link SavingsInsightTemplate}, and {@link IncomeInsightTemplate}.
 */
class MonthlyInsightTemplateTest {

    private SpendingInsightTemplate spendingTemplate;
    private SavingsInsightTemplate savingsTemplate;
    private IncomeInsightTemplate incomeTemplate;

    @BeforeEach
    void setUp() {
        spendingTemplate = new SpendingInsightTemplate();
        savingsTemplate = new SavingsInsightTemplate();
        incomeTemplate = new IncomeInsightTemplate();
    }

    @Test
    void testSpendingTemplateBehaviors() {
        final WalletInsight bothZero = spendingTemplate.generate(BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(InsightTrend.NEUTRAL, bothZero.trend());
        assertEquals("Spending", bothZero.title());
        assertNull(bothZero.deltaPercentage());

        final WalletInsight previousZero = spendingTemplate.generate(new BigDecimal("50.00"), BigDecimal.ZERO);
        assertEquals(InsightTrend.NEUTRAL, previousZero.trend());
        assertEquals(new BigDecimal("50.00"), previousZero.deltaAmount());

        final WalletInsight increasedSpending = spendingTemplate.generate(
                new BigDecimal("120.00"),
                new BigDecimal("100.00")
        );
        assertEquals(InsightTrend.NEGATIVE, increasedSpending.trend());
        assertEquals(0, new BigDecimal("20.00").compareTo(increasedSpending.deltaPercentage()));

        final WalletInsight decreasedSpending = spendingTemplate.generate(
                new BigDecimal("80.00"),
                new BigDecimal("100.00")
        );
        assertEquals(InsightTrend.POSITIVE, decreasedSpending.trend());
        assertEquals(0, new BigDecimal("20.00").compareTo(decreasedSpending.deltaPercentage()));

        final WalletInsight unchanged = spendingTemplate.generate(
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        );
        assertEquals(InsightTrend.NEUTRAL, unchanged.trend());
    }

    @Test
    void testSavingsTemplateBehaviors() {
        final WalletInsight increasedSavings = savingsTemplate.generate(
                new BigDecimal("400.00"),
                new BigDecimal("200.00")
        );
        assertEquals("Savings", increasedSavings.title());
        assertEquals(InsightTrend.POSITIVE, increasedSavings.trend());
        assertEquals(new BigDecimal("200.00"), increasedSavings.deltaAmount());

        final WalletInsight decreasedSavings = savingsTemplate.generate(
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );
        assertEquals(InsightTrend.NEGATIVE, decreasedSavings.trend());
        assertEquals(new BigDecimal("100.00"), decreasedSavings.deltaAmount());

        final WalletInsight unchangedSavings = savingsTemplate.generate(
                new BigDecimal("200.00"),
                new BigDecimal("200.00")
        );
        assertEquals(InsightTrend.NEUTRAL, unchangedSavings.trend());
    }

    @Test
    void testIncomeTemplateBehaviors() {
        final WalletInsight bothZero = incomeTemplate.generate(BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals("Income", bothZero.title());
        assertEquals(InsightTrend.NEUTRAL, bothZero.trend());

        final WalletInsight previousZero = incomeTemplate.generate(new BigDecimal("1500.00"), BigDecimal.ZERO);
        assertEquals(InsightTrend.POSITIVE, previousZero.trend());
        assertEquals(new BigDecimal("1500.00"), previousZero.deltaAmount());

        final WalletInsight increasedIncome = incomeTemplate.generate(
                new BigDecimal("2500.00"),
                new BigDecimal("2000.00")
        );
        assertEquals(InsightTrend.POSITIVE, increasedIncome.trend());
        assertEquals(0, new BigDecimal("25.00").compareTo(increasedIncome.deltaPercentage()));

        final WalletInsight decreasedIncome = incomeTemplate.generate(
                new BigDecimal("1500.00"),
                new BigDecimal("2000.00")
        );
        assertEquals(InsightTrend.NEGATIVE, decreasedIncome.trend());
        assertEquals(0, new BigDecimal("25.00").compareTo(decreasedIncome.deltaPercentage()));

        final WalletInsight unchangedIncome = incomeTemplate.generate(
                new BigDecimal("2000.00"),
                new BigDecimal("2000.00")
        );
        assertEquals(InsightTrend.NEUTRAL, unchangedIncome.trend());
    }
}
