package it.unibo.unibodget.model.currency.history;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CurrencyHistoryPointTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        LocalDate d = LocalDate.of(2024, 1, 1);
        CurrencyHistoryPoint p = new CurrencyHistoryPoint(d, 1.25);

        assertEquals(d, p.getDate());
        assertEquals(1.25, p.getRate());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        CurrencyHistoryPoint p1 = new CurrencyHistoryPoint(LocalDate.of(2024, 1, 1), 1.25);
        CurrencyHistoryPoint p2 = new CurrencyHistoryPoint(LocalDate.of(2024, 1, 1), 1.25);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void shouldPrintReadableToString() {
        CurrencyHistoryPoint p = new CurrencyHistoryPoint(LocalDate.of(2024, 1, 1), 1.25);

        assertTrue(p.toString().contains("2024-01-01"));
        assertTrue(p.toString().contains("1.25"));
    }

}
