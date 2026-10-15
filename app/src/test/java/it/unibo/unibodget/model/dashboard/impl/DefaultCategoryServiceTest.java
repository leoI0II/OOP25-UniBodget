package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void shouldRecomputeCategorySummariesFromTransactions() {
        final DefaultCategoryService service = new DefaultCategoryService();

        final Category food = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        final Category transport = new Category(
                "Transport",
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

        assertEquals(new BigDecimal("50.00"), result.get("Food"));
        assertEquals(new BigDecimal("15.00"), result.get("Transport"));
        assertEquals(2, result.size());
    }
}
