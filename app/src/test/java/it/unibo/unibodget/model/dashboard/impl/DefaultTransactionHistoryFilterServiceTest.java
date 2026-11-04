package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Tests for {@link DefaultTransactionHistoryFilterService}.
 */
class DefaultTransactionHistoryFilterServiceTest {

    private static final int Y_2026 = 2026;
    private static final int M_5 = 5;
    private static final int D_15 = 15;

    private DefaultTransactionHistoryFilterService service;

    private CashTransaction salaryTransaction;
    private CashTransaction groceriesTransaction;
    private CashTransaction rentTransaction;
    private CashTransaction transferTransaction;

    @BeforeEach
    void setUp() {
        service = new DefaultTransactionHistoryFilterService();

        final Category salaryCategory = new Category(
                "Salary",
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );
        final Category foodCategory = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        final Category rentCategory = new Category(
                "Rent",
                new ARGBColor(0xFF9C27B0),
                CategoryType.EXPENSE
        );
        final Category transferCategory = new Category(
                "Transfer",
                new ARGBColor(0xFF2196F3),
                CategoryType.TRANSFER
        );

        salaryTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("2500.00")))
                .category(salaryCategory)
                .date(LocalDate.of(Y_2026, M_5, 1))
                .description("Monthly salary")
                .notes("Company payment")
                .build();

        groceriesTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-80.00")))
                .category(foodCategory)
                .date(LocalDate.of(Y_2026, M_5, 10))
                .description("Groceries")
                .notes("Supermarket shopping")
                .build();

        rentTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-700.00")))
                .category(rentCategory)
                .date(LocalDate.of(Y_2026, M_5, 3))
                .description("Rent payment")
                .notes("May rent")
                .build();

        transferTransaction = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-200.00")))
                .category(transferCategory)
                .date(LocalDate.of(Y_2026, M_5, D_15))
                .description("Transfer to savings")
                .notes("Monthly transfer")
                .build();
    }

    @Test
    void shouldReturnAllTransactionsSortedByNewestFirstByDefault() {
        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                new TransactionFilterCriteria()
        );

        assertEquals(4, result.size());
        assertEquals(transferTransaction, result.get(0));
        assertEquals(groceriesTransaction, result.get(1));
        assertEquals(rentTransaction, result.get(2));
        assertEquals(salaryTransaction, result.get(3));
    }

    @Test
    void shouldFilterByCategoryName() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                "Food",
                null,
                null,
                null,
                null,
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(1, result.size());
        assertEquals(groceriesTransaction, result.get(0));
    }

    @Test
    void shouldFilterByCategoryType() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                CategoryType.EXPENSE,
                null,
                null,
                null,
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(2, result.size());
        assertEquals(groceriesTransaction, result.get(0));
        assertEquals(rentTransaction, result.get(1));
    }

    @Test
    void shouldFilterByDateRange() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                null,
                LocalDate.of(Y_2026, M_5, 2),
                LocalDate.of(Y_2026, M_5, 10),
                null,
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(2, result.size());
        assertEquals(groceriesTransaction, result.get(0));
        assertEquals(rentTransaction, result.get(1));
    }

    @Test
    void shouldFilterByKeywordInDescription() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                null,
                null,
                null,
                "salary",
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(1, result.size());
        assertEquals(salaryTransaction, result.get(0));
    }

    @Test
    void shouldFilterByKeywordInNotes() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                null,
                null,
                null,
                "supermarket",
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(1, result.size());
        assertEquals(groceriesTransaction, result.get(0));
    }

    @Test
    void shouldSortByOldestFirst() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                null,
                null,
                null,
                null,
                TransactionSortOrder.OLDEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(groceriesTransaction, transferTransaction, rentTransaction, salaryTransaction),
                criteria
        );

        assertEquals(4, result.size());
        assertEquals(salaryTransaction, result.get(0));
        assertEquals(rentTransaction, result.get(1));
        assertEquals(groceriesTransaction, result.get(2));
        assertEquals(transferTransaction, result.get(3));
    }

    @Test
    void shouldSortByHighestAmountFirst() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                null,
                null,
                null,
                null,
                TransactionSortOrder.HIGHEST_AMOUNT_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(4, result.size());
        assertEquals(salaryTransaction, result.get(0));
        assertEquals(rentTransaction, result.get(1));
        assertEquals(transferTransaction, result.get(2));
        assertEquals(groceriesTransaction, result.get(3));
    }

    @Test
    void shouldApplyMultipleFiltersTogether() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                null,
                CategoryType.EXPENSE,
                LocalDate.of(Y_2026, M_5, 1),
                LocalDate.of(Y_2026, M_5, 31),
                "rent",
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertEquals(1, result.size());
        assertEquals(rentTransaction, result.get(0));
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionMatches() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                "Travel",
                null,
                null,
                null,
                null,
                TransactionSortOrder.NEWEST_FIRST
        );

        final List<CashTransaction> result = service.filter(
                List.of(salaryTransaction, groceriesTransaction, rentTransaction, transferTransaction),
                criteria
        );

        assertTrue(result.isEmpty());
    }
}
