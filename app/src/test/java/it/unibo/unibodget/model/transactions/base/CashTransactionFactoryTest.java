package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.FiatCurrency;

/**
 * Unit tests for {@link CashTransactionFactory}.
 */
class CashTransactionFactoryTest {

    private static final int YEAR_2024 = 2024;
    private static final int MONTH_MAY = 5;
    private static final int DAY_TEN = 10;
    private static final LocalDate SAMPLE_DATE = LocalDate.of(YEAR_2024, MONTH_MAY, DAY_TEN);

    private static final BigDecimal AMOUNT_FIFTY = new BigDecimal("50.00");
    private static final BigDecimal SIGNED_AMOUNT_FIFTY = new BigDecimal("-50.00");
    private static final BigDecimal AMOUNT_HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-10.00");

    private static final String FRIEND_ALICE = "Alice";
    private static final String DESC = "Sample description";
    private static final String NOTES = "Sample notes";

    private CashTransactionFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CashTransactionFactory();
    }

    @Test
    void shouldCreateExpenseTransactionWithNegativeSignedAmount() {
        final CashTransaction tx = factory.create(
                AMOUNT_FIFTY,
                FiatCurrency.EUR,
                CategoryType.EXPENSE,
                Category.FOOD,
                SAMPLE_DATE,
                DESC,
                NOTES
        );

        assertNotNull(tx);
        assertEquals(SIGNED_AMOUNT_FIFTY, tx.getAsset().amount());
        assertEquals(Category.FOOD, tx.getCategory());
        assertEquals(SAMPLE_DATE, tx.getDate());
        assertEquals(DESC, tx.getDescription());
        assertEquals(NOTES, tx.getNotes());
        assertFalse(tx.isFriendLoanTransaction());
        assertTrue(tx.getFriendLoanId().isEmpty());
    }

    @Test
    void shouldCreateIncomeTransactionWithPositiveSignedAmount() {
        final CashTransaction tx = factory.create(
                AMOUNT_HUNDRED,
                FiatCurrency.EUR,
                CategoryType.INCOME,
                Category.SAVINGS,
                SAMPLE_DATE,
                DESC,
                null
        );

        assertNotNull(tx);
        assertEquals(AMOUNT_HUNDRED, tx.getAsset().amount());
        assertEquals(Category.SAVINGS, tx.getCategory());
        assertFalse(tx.isFriendLoanTransaction());
    }

    @Test
    void shouldCreateTransferTransactionWithNegativeSignedAmount() {
        final CashTransaction tx = factory.create(
                AMOUNT_FIFTY,
                FiatCurrency.EUR,
                CategoryType.TRANSFER,
                Category.TRANSFER,
                SAMPLE_DATE,
                DESC,
                null
        );

        assertNotNull(tx);
        assertEquals(SIGNED_AMOUNT_FIFTY, tx.getAsset().amount());
        assertEquals(Category.TRANSFER, tx.getCategory());
    }

    @Test
    void shouldRejectFriendLoanTypeInStandardCreate() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.create(
                        AMOUNT_FIFTY,
                        FiatCurrency.EUR,
                        CategoryType.FRIEND_LOAN,
                        Category.FRIEND_LOAN,
                        SAMPLE_DATE,
                        DESC,
                        null
                )
        );
    }

    @Test
    void shouldRejectMismatchedCategoryType() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.create(
                        AMOUNT_FIFTY,
                        FiatCurrency.EUR,
                        CategoryType.INCOME,
                        Category.FOOD, // Category.FOOD is EXPENSE
                        SAMPLE_DATE,
                        DESC,
                        null
                )
        );
    }

    @Test
    void shouldRejectZeroOrNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.create(
                        BigDecimal.ZERO,
                        FiatCurrency.EUR,
                        CategoryType.EXPENSE,
                        Category.FOOD,
                        SAMPLE_DATE,
                        DESC,
                        null
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                factory.create(
                        NEGATIVE_AMOUNT,
                        FiatCurrency.EUR,
                        CategoryType.EXPENSE,
                        Category.FOOD,
                        SAMPLE_DATE,
                        DESC,
                        null
                )
        );
    }

    @Test
    void shouldCreateFriendLoanDisbursementWithNegativeAmountAndGeneratedId() {
        final CashTransaction tx = factory.createFriendLoan(
                AMOUNT_FIFTY,
                FiatCurrency.EUR,
                CategoryType.FRIEND_LOAN,
                Category.FRIEND_LOAN,
                SAMPLE_DATE,
                "Lending to friend",
                null,
                FRIEND_ALICE
        );

        assertNotNull(tx);
        assertEquals(SIGNED_AMOUNT_FIFTY, tx.getAsset().amount());
        assertTrue(tx.isFriendLoanTransaction());
        assertTrue(tx.getFriendLoanId().isPresent());
        assertEquals(FRIEND_ALICE, tx.getFriendName().orElseThrow());
    }

    @Test
    void shouldRejectFriendLoanWithBlankFriendNameOrWrongType() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.createFriendLoan(
                        AMOUNT_FIFTY,
                        FiatCurrency.EUR,
                        CategoryType.FRIEND_LOAN,
                        Category.FRIEND_LOAN,
                        SAMPLE_DATE,
                        DESC,
                        null,
                        "   "
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                factory.createFriendLoan(
                        AMOUNT_FIFTY,
                        FiatCurrency.EUR,
                        CategoryType.EXPENSE,
                        Category.FOOD,
                        SAMPLE_DATE,
                        DESC,
                        null,
                        FRIEND_ALICE
                )
        );
    }

    @Test
    void shouldCreateFriendLoanRepaymentWithPositiveAmountAndProvidedId() {
        final UUID loanId = UUID.randomUUID();
        final CashTransaction tx = factory.createFriendLoanRepayment(
                AMOUNT_FIFTY,
                FiatCurrency.EUR,
                CategoryType.FRIEND_LOAN,
                Category.FRIEND_LOAN,
                SAMPLE_DATE,
                "Repayment received",
                null,
                FRIEND_ALICE,
                loanId
        );

        assertNotNull(tx);
        assertEquals(AMOUNT_FIFTY, tx.getAsset().amount());
        assertTrue(tx.isFriendLoanTransaction());
        assertEquals(loanId, tx.getFriendLoanId().orElseThrow());
        assertEquals(FRIEND_ALICE, tx.getFriendName().orElseThrow());
    }

    @Test
    void shouldThrowWhenRequiredArgumentsAreNull() {
        assertThrows(NullPointerException.class, () ->
                factory.create(null, FiatCurrency.EUR, CategoryType.EXPENSE, Category.FOOD, SAMPLE_DATE, DESC, null)
        );
        assertThrows(NullPointerException.class, () ->
                factory.create(AMOUNT_FIFTY, null, CategoryType.EXPENSE, Category.FOOD, SAMPLE_DATE, DESC, null)
        );
        assertThrows(NullPointerException.class, () ->
                factory.create(AMOUNT_FIFTY, FiatCurrency.EUR, null, Category.FOOD, SAMPLE_DATE, DESC, null)
        );
        assertThrows(NullPointerException.class, () ->
                factory.create(AMOUNT_FIFTY, FiatCurrency.EUR, CategoryType.EXPENSE, null, SAMPLE_DATE, DESC, null)
        );
        assertThrows(NullPointerException.class, () ->
                factory.create(AMOUNT_FIFTY, FiatCurrency.EUR, CategoryType.EXPENSE, Category.FOOD, null, DESC, null)
        );
        assertThrows(NullPointerException.class, () ->
                factory.createFriendLoan(
                        AMOUNT_FIFTY,
                        FiatCurrency.EUR,
                        CategoryType.FRIEND_LOAN,
                        Category.FRIEND_LOAN,
                        SAMPLE_DATE,
                        DESC,
                        null,
                        null
                )
        );
        assertThrows(NullPointerException.class, () ->
                factory.createFriendLoanRepayment(
                        AMOUNT_FIFTY,
                        FiatCurrency.EUR,
                        CategoryType.FRIEND_LOAN,
                        Category.FRIEND_LOAN,
                        SAMPLE_DATE,
                        DESC,
                        null,
                        FRIEND_ALICE,
                        null
                )
        );
    }
}
