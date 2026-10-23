package it.unibo.unibodget.model.transactions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class HistoricalTest {

    private CashTransaction tx(final LocalDate date, final String desc) {
        return CashTransaction.builder()
                .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.ONE))
                .category(Category.FOOD)
                .date(date)
                .description(desc)
                .notes("notes")
                .build();
    }

    @Test
    void shouldRemoveTransaction() {
        final Historical<CashTransaction> h = new Historical<>();

        final CashTransaction t = tx(LocalDate.of(2024, 1, 10), "A");

        h.addTransaction(t);
        assertTrue(h.getTransactions().contains(t));

        final boolean removed = h.removeTransaction(t);

        assertTrue(removed);
        assertFalse(h.getTransactions().contains(t));
    }

    @Test
    void shouldReplaceTransaction() {
        final Historical<CashTransaction> h = new Historical<>();

        final CashTransaction oldTx = tx(LocalDate.of(2024, 1, 10), "Old");
        final CashTransaction newTx = tx(LocalDate.of(2024, 1, 10), "New");

        h.addTransaction(oldTx);

        final boolean replaced = h.replaceTransaction(oldTx, newTx);

        assertTrue(replaced);
        assertTrue(h.getTransactions().contains(newTx));
        assertFalse(h.getTransactions().contains(oldTx));
    }

}
