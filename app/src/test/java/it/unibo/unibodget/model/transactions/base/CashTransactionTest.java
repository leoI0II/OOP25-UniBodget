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

        private static final String DESC = "desc";
        private static final String SHORT_DESCR = "d";
        private static final String NOTES = "notes";
        private static final String SHORT_NOTES = "n";

    @Test
    void shouldCreateStandardTransaction() {
        final CashTransaction t = new CashTransaction(
                new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                Category.FOOD,                      // categoria reale
                LocalDate.of(2024, 1, 1),
                DESC,
                NOTES
        );

        assertFalse(t.isFriendLoanTransaction());
        assertTrue(t.getFriendLoanId().isEmpty());
        assertTrue(t.getFriendName().isEmpty());
    }

    @Test
    void shouldCreateFriendLoanTransaction() {
        final UUID id = UUID.randomUUID();

        final CashTransaction t = new CashTransaction(
                new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                Category.FRIEND_LOAN,               // categoria reale
                LocalDate.of(2024, 1, 1),
                DESC,
                NOTES,
                id,
                "Alice"
        );

        assertTrue(t.isFriendLoanTransaction());
        assertEquals(id, t.getFriendLoanId().get());
        assertEquals("Alice", t.getFriendName().get());
    }

    @Test
    void shouldFailIfOnlyOneFriendLoanFieldProvided() {
        assertThrows(IllegalArgumentException.class, () ->
                new CashTransaction(
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                        Category.FRIEND_LOAN,
                        LocalDate.now(),
                        SHORT_DESCR,
                        SHORT_NOTES,
                        UUID.randomUUID(),
                        null
                )
        );
    }

    @Test
    void shouldFailIfFriendLoanCategoryWithoutMetadata() {
        assertThrows(IllegalArgumentException.class, () ->
                new CashTransaction(
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                        Category.FRIEND_LOAN,
                        LocalDate.now(),
                        SHORT_DESCR,
                        SHORT_NOTES,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldFailIfNonFriendLoanCategoryHasMetadata() {
        assertThrows(IllegalArgumentException.class, () ->
                new CashTransaction(
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                        Category.FOOD,                 // NON FRIEND_LOAN
                        LocalDate.now(),
                        SHORT_DESCR,
                        SHORT_NOTES,
                        UUID.randomUUID(),
                        "Bob"
                )
        );
    }

}
