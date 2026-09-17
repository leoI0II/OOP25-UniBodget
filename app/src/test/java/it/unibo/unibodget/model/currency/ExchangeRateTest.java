package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ExchangeRateTest {

    private static final double EXPECTED = 1.2;

    @Test
    void shouldStoreFieldsCorrectly() {
        final Instant now = Instant.now();
        final ExchangeRate r = new ExchangeRate(FiatCurrency.EUR, FiatCurrency.USD, EXPECTED, now);

        assertEquals(FiatCurrency.EUR, r.getBase());
        assertEquals(FiatCurrency.USD, r.getTarget());
        assertEquals(EXPECTED, r.getRate());
        assertEquals(now, r.getTimestamp());
    }

}
