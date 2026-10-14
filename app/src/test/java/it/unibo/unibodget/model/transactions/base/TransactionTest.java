package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class TransactionTest {

    private static final String DESC = "desc";
    private static final String NOTES = "notes";

    @Test
    void shouldImplementEqualsAndHashCode() {
        final Asset a = new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN);
        final LocalDate d = LocalDate.of(2024, 1, 1);
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();

        final AbstractTransaction t1 = new CashTransaction(id1, a, Category.FOOD, d, DESC, NOTES);
        final AbstractTransaction t2 = new CashTransaction(id1, a, Category.FOOD, d, DESC, NOTES);
        final AbstractTransaction t3 = new CashTransaction(id2, a, Category.FOOD, d, DESC, NOTES);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, t3);
    }

}
