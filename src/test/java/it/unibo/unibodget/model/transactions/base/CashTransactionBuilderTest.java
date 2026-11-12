package it.unibo.unibodget.model.transactions.base;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Unit tests for {@link CashTransaction.Builder}.
 */
class CashTransactionBuilderTest {

    private static final int GREEN = 0xFF4CAF50;
    private static final int PURPLE = 0xFF9C27B0;

    private Category foodCategory;
    private Category friendLoanCategory;
    private Asset asset;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        foodCategory = new Category("Food", new ARGBColor(GREEN), CategoryType.EXPENSE);
        friendLoanCategory = new Category("Friend Loan", new ARGBColor(PURPLE), CategoryType.FRIEND_LOAN);
        asset = Asset.of(FiatCurrency.EUR, new BigDecimal("-25.50"));
        today = LocalDate.now();
    }

    @Test
    void shouldBuildStandardTransactionWithDefaults() {
        final CashTransaction transaction = CashTransaction.builder()
                .asset(asset)
                .category(foodCategory)
                .date(today)
                .description("Lunch")
                .notes("Pizza with team")
                .build();

        assertNotNull(transaction.getId());
        assertEquals(asset, transaction.getAsset());
        assertEquals(foodCategory, transaction.getCategory());
        assertEquals(today, transaction.getDate());
        assertEquals("Lunch", transaction.getDescription());
        assertEquals("Pizza with team", transaction.getNotes());
        assertFalse(transaction.isFriendLoanTransaction());
        assertTrue(transaction.getFriendLoanId().isEmpty());
        assertTrue(transaction.getFriendName().isEmpty());
    }

    @Test
    void shouldBuildTransactionWithExplicitId() {
        final UUID explicitId = UUID.randomUUID();

        final CashTransaction transaction = CashTransaction.builder()
                .id(explicitId)
                .asset(asset)
                .category(foodCategory)
                .date(today)
                .build();

        assertEquals(explicitId, transaction.getId());
    }

    @Test
    void shouldBuildFriendLoanTransaction() {
        final UUID loanId = UUID.randomUUID();
        final Asset loanAsset = Asset.of(FiatCurrency.EUR, new BigDecimal("-50.00"));

        final CashTransaction transaction = CashTransaction.builder()
                .asset(loanAsset)
                .category(friendLoanCategory)
                .date(today)
                .description("Lent to Alice")
                .friendLoan(loanId, "Alice")
                .build();

        assertTrue(transaction.isFriendLoanTransaction());
        assertEquals(loanId, transaction.getFriendLoanId().orElse(null));
        assertEquals("Alice", transaction.getFriendName().orElse(null));
    }

    @Test
    void shouldThrowWhenMandatoryFieldsAreMissing() {
        assertThrows(NullPointerException.class, () ->
                CashTransaction.builder()
                        .category(foodCategory)
                        .date(today)
                        .build()
        );

        assertThrows(NullPointerException.class, () ->
                CashTransaction.builder()
                        .asset(asset)
                        .date(today)
                        .build()
        );

        assertThrows(NullPointerException.class, () ->
                CashTransaction.builder()
                        .asset(asset)
                        .category(foodCategory)
                        .build()
        );
    }

    @Test
    void shouldThrowWhenFriendLoanMetadataIsInconsistent() {
        final UUID loanId = UUID.randomUUID();

        // Friend loan metadata set on non-FRIEND_LOAN category
        assertThrows(IllegalArgumentException.class, () ->
                CashTransaction.builder()
                        .asset(asset)
                        .category(foodCategory)
                        .date(today)
                        .friendLoan(loanId, "Bob")
                        .build()
        );

        // FRIEND_LOAN category missing loan metadata
        assertThrows(IllegalArgumentException.class, () ->
                CashTransaction.builder()
                        .asset(asset)
                        .category(friendLoanCategory)
                        .date(today)
                        .build()
        );
    }

    @Test
    void shouldSupportPrototypeCloningAndMutation() {
        final UUID originalId = UUID.randomUUID();
        final CashTransaction original = CashTransaction.builder()
                .id(originalId)
                .asset(asset)
                .category(foodCategory)
                .date(today)
                .description("Initial description")
                .notes("Initial notes")
                .build();

        final UUID newId = UUID.randomUUID();
        final CashTransaction clonedWithNewId = CashTransaction.builder(original)
                .id(newId)
                .description("Updated description")
                .build();

        assertEquals(newId, clonedWithNewId.getId());
        assertEquals("Updated description", clonedWithNewId.getDescription());
        assertEquals(original.getAsset(), clonedWithNewId.getAsset());
        assertEquals(original.getCategory(), clonedWithNewId.getCategory());
        assertEquals(original.getDate(), clonedWithNewId.getDate());
        assertEquals(original.getNotes(), clonedWithNewId.getNotes());
    }
}
