package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class TransactionTest {

    @Test
    void shouldImplementEqualsAndHashCode() {
        Asset a = new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN);
        LocalDate d = LocalDate.of(2024, 1, 1);

        Transaction t1 = new CashTransaction(a, Category.FOOD, d, "desc", "notes");
        Transaction t2 = new CashTransaction(a, Category.FOOD, d, "desc", "notes");

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

}
