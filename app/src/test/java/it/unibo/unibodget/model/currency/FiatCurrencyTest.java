package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FiatCurrencyTest {

    @Test
    void shouldExposeFieldsCorrectly() {
        final FiatCurrency c = FiatCurrency.EUR;

        assertEquals(CurrencyType.FIAT, c.getType());
        assertEquals("€", c.getSymbol());
        assertEquals("EUR", c.getShortName());
        assertEquals("Euro", c.getFullName());
        assertEquals("EUR", c.getCode());
    }

}
