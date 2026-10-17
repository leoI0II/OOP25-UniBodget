package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;

class TransactionTest {

    private static final String DESC = "desc";
    private static final String NOTES = "notes";

    @Test
    void shouldDistinguishTransactionsWithDifferentIds() {
        final Asset asset = new Asset(FiatCurrency.EUR, BigDecimal.TEN);
        final LocalDate date = LocalDate.of(2026, 1, 1);

        final AbstractTransaction first =
                new CashTransaction(asset, Category.FOOD, date, DESC, NOTES);
        final AbstractTransaction second =
                new CashTransaction(asset, Category.FOOD, date, DESC, NOTES);

        assertNotEquals(first.getId(), second.getId());
        assertNotEquals(first, second);
    }

    @Test
    void shouldImplementEqualsAndHashCode() throws Exception {
        final ObjectMapper mapper = PersistenceJacksonConfig.mapper().copy();
        final CashTransaction original = new CashTransaction(
                new Asset(FiatCurrency.EUR, BigDecimal.TEN),
                Category.FOOD,
                LocalDate.of(2026, 1, 1),
                DESC,
                NOTES
        );

        final String json = mapper.writeValueAsString(original);
        final CashTransaction restored =
                mapper.readValue(json, CashTransaction.class);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original, restored);
        assertEquals(original.hashCode(), restored.hashCode());
    }
}
