package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class CashTransactionTest {

    private static final int YEAR_2024 = 2024;
    private static final String DESC = "desc";
    private static final String SHORT_DESCR = "d";
    private static final String NOTES = "notes";
    private static final String SHORT_NOTES = "n";

    @Test
    void shouldCreateStandardTransaction() {
        final CashTransaction t = CashTransaction.builder()
                .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN))
                .category(Category.FOOD)
                .date(LocalDate.of(YEAR_2024, 1, 1))
                .description(DESC)
                .notes(NOTES)
                .build();

        assertFalse(t.isFriendLoanTransaction());
        assertTrue(t.getFriendLoanId().isEmpty());
        assertTrue(t.getFriendName().isEmpty());
    }

    @Test
    void shouldCreateFriendLoanTransaction() {
        final UUID id = UUID.randomUUID();

        final CashTransaction t = CashTransaction.builder()
                .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN))
                .category(Category.FRIEND_LOAN)
                .date(LocalDate.of(YEAR_2024, 1, 1))
                .description(DESC)
                .notes(NOTES)
                .loanId(id)
                .friendName("Alice")
                .build();

        assertTrue(t.isFriendLoanTransaction());
        assertEquals(id, t.getFriendLoanId().get());
        assertEquals("Alice", t.getFriendName().get());
    }

    @Test
    void shouldFailIfOnlyOneFriendLoanFieldProvided() {
        assertThrows(IllegalArgumentException.class, () ->
                CashTransaction.builder()
                        .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN))
                        .category(Category.FRIEND_LOAN)
                        .date(LocalDate.now())
                        .description(SHORT_DESCR)
                        .notes(SHORT_NOTES)
                        .loanId(UUID.randomUUID())
                        .friendName(null)
                        .build()
        );
    }

    @Test
    void shouldFailIfFriendLoanCategoryWithoutMetadata() {
        assertThrows(IllegalArgumentException.class, () ->
                CashTransaction.builder()
                        .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN))
                        .category(Category.FRIEND_LOAN)
                        .date(LocalDate.now())
                        .description(SHORT_DESCR)
                        .notes(SHORT_NOTES)
                        .loanId(null)
                        .friendName(null)
                        .build()
        );
    }

    @Test
    void shouldFailIfNonFriendLoanCategoryHasMetadata() {
        assertThrows(IllegalArgumentException.class, () ->
                CashTransaction.builder()
                        .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN))
                        .category(Category.FOOD)
                        .date(LocalDate.now())
                        .description(SHORT_DESCR)
                        .notes(SHORT_NOTES)
                        .loanId(UUID.randomUUID())
                        .friendName("Bob")
                        .build()
        );
    }

}
