package it.unibo.unibodget.model.dashboard.specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.impl.DefaultTransactionHistoryFilterService;
import it.unibo.unibodget.model.dashboard.impl.TransactionFilterCriteria;
import it.unibo.unibodget.model.dashboard.impl.TransactionSortOrder;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Unit tests for the Specification Pattern implementations under {@link it.unibo.unibodget.model.dashboard.specification}.
 */
class TransactionSpecificationTest {

    private static final int YEAR_2026 = 2026;
    private static final int MONTH_5 = 5;
    private static final int DAY_1 = 1;
    private static final int DAY_5 = 5;
    private static final int DAY_10 = 10;
    private static final int DAY_15 = 15;
    private static final int DAY_20 = 20;
    private static final int DAY_25 = 25;
    private static final String ORGANIC = "organic";

    private CashTransaction groceryExpense;
    private CashTransaction salaryIncome;
    private CashTransaction rentExpense;

    @BeforeEach
    void setUp() {
        final Category food = new Category("Food & Groceries", new ARGBColor(0xFFFF9800), CategoryType.EXPENSE);
        final Category salary = new Category("Salary", new ARGBColor(0xFF4CAF50), CategoryType.INCOME);
        final Category housing = new Category("Housing", new ARGBColor(0xFF9C27B0), CategoryType.EXPENSE);

        groceryExpense = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-45.50")))
                .category(food)
                .date(LocalDate.of(YEAR_2026, MONTH_5, DAY_10))
                .description("Supermarket trip")
                .notes("Bought organic fruits")
                .build();

        salaryIncome = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("2500.00")))
                .category(salary)
                .date(LocalDate.of(YEAR_2026, MONTH_5, DAY_1))
                .description("Monthly salary")
                .notes("May payout")
                .build();

        rentExpense = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-650.00")))
                .category(housing)
                .date(LocalDate.of(YEAR_2026, MONTH_5, DAY_20))
                .description("Apartment rent")
                .notes("Monthly wire")
                .build();
    }

    @Test
    void shouldMatchCategoryNameCaseInsensitively() {
        final TransactionSpecification spec = new CategoryNameSpecification("groceries");

        assertTrue(spec.isSatisfiedBy(groceryExpense));
        assertFalse(spec.isSatisfiedBy(salaryIncome));
        assertFalse(spec.isSatisfiedBy(rentExpense));
    }

    @Test
    void shouldMatchCategoryType() {
        final TransactionSpecification expenseSpec = new CategoryTypeSpecification(CategoryType.EXPENSE);
        final TransactionSpecification incomeSpec = new CategoryTypeSpecification(CategoryType.INCOME);

        assertTrue(expenseSpec.isSatisfiedBy(groceryExpense));
        assertTrue(expenseSpec.isSatisfiedBy(rentExpense));
        assertFalse(expenseSpec.isSatisfiedBy(salaryIncome));

        assertTrue(incomeSpec.isSatisfiedBy(salaryIncome));
        assertFalse(incomeSpec.isSatisfiedBy(groceryExpense));
    }

    @Test
    void shouldMatchDateRange() {
        final TransactionSpecification rangeSpec = new DateRangeSpecification(
                LocalDate.of(YEAR_2026, MONTH_5, DAY_5),
                LocalDate.of(YEAR_2026, MONTH_5, DAY_15)
        );

        assertFalse(rangeSpec.isSatisfiedBy(salaryIncome)); // May 1 is before
        assertTrue(rangeSpec.isSatisfiedBy(groceryExpense)); // May 10 is in range
        assertFalse(rangeSpec.isSatisfiedBy(rentExpense));   // May 20 is after
    }

    @Test
    void shouldMatchKeywordInDescriptionOrNotes() {
        final TransactionSpecification descSpec = new KeywordSpecification("supermarket");
        final TransactionSpecification notesSpec = new KeywordSpecification(ORGANIC);
        final TransactionSpecification missingSpec = new KeywordSpecification("cryptocurrency");

        assertTrue(descSpec.isSatisfiedBy(groceryExpense));
        assertTrue(notesSpec.isSatisfiedBy(groceryExpense));
        assertFalse(missingSpec.isSatisfiedBy(groceryExpense));
    }

    @Test
    void shouldSupportLogicalCompositionAndOrNot() {
        final TransactionSpecification isExpense = new CategoryTypeSpecification(CategoryType.EXPENSE);
        final TransactionSpecification hasOrganic = new KeywordSpecification(ORGANIC);
        final TransactionSpecification hasRent = new KeywordSpecification("rent");

        final TransactionSpecification compositeAnd = isExpense.and(hasOrganic);
        assertTrue(compositeAnd.isSatisfiedBy(groceryExpense));
        assertFalse(compositeAnd.isSatisfiedBy(rentExpense));

        final TransactionSpecification compositeOr = hasOrganic.or(hasRent);
        assertTrue(compositeOr.isSatisfiedBy(groceryExpense));
        assertTrue(compositeOr.isSatisfiedBy(rentExpense));
        assertFalse(compositeOr.isSatisfiedBy(salaryIncome));

        final TransactionSpecification notExpense = isExpense.not();
        assertTrue(notExpense.isSatisfiedBy(salaryIncome));
        assertFalse(notExpense.isSatisfiedBy(groceryExpense));
    }

    @Test
    void shouldMatchAllTransactionsWithAllSpecification() {
        final TransactionSpecification allSpec = TransactionSpecification.all();

        assertTrue(allSpec.isSatisfiedBy(groceryExpense));
        assertTrue(allSpec.isSatisfiedBy(salaryIncome));
        assertTrue(allSpec.isSatisfiedBy(rentExpense));
    }

    @Test
    void shouldCreateCompositeSpecificationFromCriteria() {
        final TransactionFilterCriteria criteria = new TransactionFilterCriteria(
                "Food",
                CategoryType.EXPENSE,
                LocalDate.of(YEAR_2026, MONTH_5, DAY_1),
                LocalDate.of(YEAR_2026, MONTH_5, DAY_15),
                ORGANIC,
                TransactionSortOrder.NEWEST_FIRST
        );

        final TransactionSpecification composite = TransactionSpecifications.fromCriteria(criteria);

        assertTrue(composite.isSatisfiedBy(groceryExpense));
        assertFalse(composite.isSatisfiedBy(salaryIncome));
        assertFalse(composite.isSatisfiedBy(rentExpense));
    }

    @Test
    void shouldFilterUsingDirectSpecificationInFilterService() {
        final DefaultTransactionHistoryFilterService service = new DefaultTransactionHistoryFilterService();

        final TransactionSpecification spec = TransactionSpecifications.categoryType(CategoryType.EXPENSE)
                .and(TransactionSpecifications.dateBetween(
                        LocalDate.of(YEAR_2026, MONTH_5, DAY_15),
                        LocalDate.of(YEAR_2026, MONTH_5, DAY_25)
                ));

        final List<CashTransaction> filtered = service.filter(
                List.of(groceryExpense, salaryIncome, rentExpense),
                spec,
                TransactionSortOrder.NEWEST_FIRST
        );

        assertEquals(1, filtered.size());
        assertEquals(rentExpense, filtered.get(0));
    }
}
