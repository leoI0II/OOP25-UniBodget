package it.unibo.unibodget.model.currency.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CurrencyHistoryPointTest {

    private static final int YEAR_2024 = 2024;
    private static final int MONTH_1 = 1;
    private static final int DAY_OF_M_1 = 1;
    private static final double RATE_1_25 = 1.25;

    @Test
    void shouldStoreFieldsCorrectly() {
        final LocalDate d = LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_1);
        final CurrencyHistoryPoint p = new CurrencyHistoryPoint(d, RATE_1_25);

        assertEquals(d, p.getDate());
        assertEquals(RATE_1_25, p.getRate());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        final CurrencyHistoryPoint p1 = 
            new CurrencyHistoryPoint(LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_1), RATE_1_25);
        final CurrencyHistoryPoint p2 = 
            new CurrencyHistoryPoint(LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_1), RATE_1_25);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void shouldPrintReadableToString() {
        final CurrencyHistoryPoint p = 
            new CurrencyHistoryPoint(LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_1), RATE_1_25);

        assertTrue(p.toString().contains("2024-01-01"));
        assertTrue(p.toString().contains("1.25"));
    }

}
