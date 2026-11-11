package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Tests for {@link DefaultCategoryService}.
 */
class DefaultCategoryServiceTest {

    private static final String FOOD = "Food";
    private static final String SALARY = "Salary";
    private static final String TRANSPORT = "Transport";

    @Test
    void shouldRecomputeCategorySummariesFromTransactions() {
        final DefaultCategoryService service = new DefaultCategoryService();

        final Category food = new Category(
                FOOD,
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        final Category transport = new Category(
                TRANSPORT,
                new ARGBColor(0xFF2196F3),
                CategoryType.EXPENSE
        );

        final CashTransaction firstFoodTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-20.00")))
                .category(food)
                .date(LocalDate.now())
                .description("Lunch")
                .notes("Lunch with friends")
                .build();
        final CashTransaction secondFoodTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-30.00")))
                .category(food)
                .date(LocalDate.now())
                .description("Dinner")
                .notes("Dinner at restaurant")
                .build();
        final CashTransaction transportTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-15.00")))
                .category(transport)
                .date(LocalDate.now())
                .description("Bus")
                .notes("Monthly pass")
                .build();

        service.recomputeFromTransactions(
                java.util.List.of(firstFoodTransaction, secondFoodTransaction, transportTransaction)
        );

        final Map<String, BigDecimal> result = service.getCategorySummaries();

        assertEquals(new BigDecimal("50.00"), result.get(FOOD));
        assertEquals(new BigDecimal("15.00"), result.get(TRANSPORT));
        assertEquals(2, result.size());
    }

    @Test
    void shouldDistinguishExpenseCategorySummariesFromIncome() {
        final DefaultCategoryService service = new DefaultCategoryService();

        final Category food = new Category(FOOD, new ARGBColor(0xFFFF9800), CategoryType.EXPENSE);
        final Category salary = new Category(SALARY, new ARGBColor(0xFF4CAF50), CategoryType.INCOME);

        final CashTransaction expense = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-45.00")))
                .category(food)
                .date(LocalDate.now())
                .description("Groceries")
                .notes("Supermarket")
                .build();

        final CashTransaction income = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("2500.00")))
                .category(salary)
                .date(LocalDate.now())
                .description("Paycheck")
                .notes("Monthly salary")
                .build();

        service.recomputeFromTransactions(java.util.List.of(expense, income));

        final Map<String, BigDecimal> allSummaries = service.getCategorySummaries();
        final Map<String, BigDecimal> expenseSummaries = service.getExpenseCategorySummaries();

        assertEquals(2, allSummaries.size());
        assertEquals(new BigDecimal("45.00"), allSummaries.get(FOOD));
        assertEquals(new BigDecimal("2500.00"), allSummaries.get(SALARY));

        assertEquals(1, expenseSummaries.size());
        assertEquals(new BigDecimal("45.00"), expenseSummaries.get(FOOD));
        assertNull(expenseSummaries.get(SALARY));
    }
}
