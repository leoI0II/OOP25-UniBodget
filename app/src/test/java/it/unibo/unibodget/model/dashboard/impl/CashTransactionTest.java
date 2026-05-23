package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Tests for {@link CashTransaction}.
 */
class CashTransactionTest {

    @Test
    void shouldCreateStandardTransactionWithoutFriendLoanMetadata() {
        final Category food = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        final CashTransaction transaction = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-20.00")),
                food,
                LocalDate.of(2026, 5, 12),
                "Lunch",
                "Lunch with friends"
        );

        assertFalse(transaction.isFriendLoanTransaction());
        assertEquals(Optional.empty(), transaction.getFriendLoanId());
        assertEquals(Optional.empty(), transaction.getFriendName());
    }

    @Test
    void shouldCreateFriendLoanTransactionWithMetadata() {
        final Category friendLoan = new Category(
                "Friend loan",
                new ARGBColor(0xFF9C27B0),
                CategoryType.FRIEND_LOAN
        );
        final UUID loanId = UUID.randomUUID();

        final CashTransaction transaction = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                friendLoan,
                LocalDate.of(2026, 5, 12),
                "Loan to Marco",
                "Temporary loan",
                loanId,
                "Marco"
        );

        assertTrue(transaction.isFriendLoanTransaction());
        assertEquals(Optional.of(loanId), transaction.getFriendLoanId());
        assertEquals(Optional.of("Marco"), transaction.getFriendName());
    }

    @Test
    void shouldRejectFriendLoanMetadataForNonFriendLoanCategory() {
        final Category food = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-20.00")),
                        food,
                        LocalDate.of(2026, 5, 12),
                        "Lunch",
                        "Lunch with friends",
                        UUID.randomUUID(),
                        "Marco"
                )
        );
    }

    @Test
    void shouldRejectPartialFriendLoanMetadata() {
        final Category friendLoan = new Category(
                "Friend loan",
                new ARGBColor(0xFF9C27B0),
                CategoryType.FRIEND_LOAN
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CashTransaction(
                        Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                        friendLoan,
                        LocalDate.of(2026, 5, 12),
                        "Loan to Marco",
                        "Temporary loan",
                        UUID.randomUUID(),
                        null
                )
        );
    }
}