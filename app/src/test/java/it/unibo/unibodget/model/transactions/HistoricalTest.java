package it.unibo.unibodget.model.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class HistoricalTest {

    private CashTransaction tx(final LocalDate date, final String desc) {
        return new CashTransaction(
                new Asset(FiatCurrency.EUR, java.math.BigDecimal.ONE),
                Category.FOOD,
                date,
                desc,
                "notes"
        );
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

    @Test
    void shouldFilterByMonth() {
        final Historical<CashTransaction> h = new Historical<>();

        final CashTransaction janTx = tx(LocalDate.of(2024, 1, 10), "Jan");
        final CashTransaction febTx = tx(LocalDate.of(2024, 2, 10), "Feb");

        h.addTransaction(janTx);
        h.addTransaction(febTx);

        final var januaryList = h.filterByMonth(YearMonth.of(2024, 1));

        assertEquals(1, januaryList.size());
        assertTrue(januaryList.contains(janTx));
        assertFalse(januaryList.contains(febTx));
    }

}
