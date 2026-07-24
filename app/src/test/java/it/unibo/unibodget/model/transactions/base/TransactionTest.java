package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class TransactionTest {

    @Test
    void shouldImplementEqualsAndHashCode() {
        final Asset a = new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN);
        final LocalDate d = LocalDate.of(2024, 1, 1);

        final Transaction t1 = new CashTransaction(a, Category.FOOD, d, "desc", "notes");
        final Transaction t2 = new CashTransaction(a, Category.FOOD, d, "desc", "notes");

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

}
