package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ExchangeRateTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        Instant now = Instant.now();
        ExchangeRate r = new ExchangeRate(FiatCurrency.EUR, FiatCurrency.USD, 1.2, now);

        assertEquals(FiatCurrency.EUR, r.getBase());
        assertEquals(FiatCurrency.USD, r.getTarget());
        assertEquals(1.2, r.getRate());
        assertEquals(now, r.getTimestamp());
    }

}
