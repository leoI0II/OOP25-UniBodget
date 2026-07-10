package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FiatCurrencyTest {

    @Test
    void shouldExposeFieldsCorrectly() {
        FiatCurrency c = FiatCurrency.EUR;

        assertEquals(CurrencyType.FIAT, c.getType());
        assertEquals("€", c.getSymbol());
        assertEquals("EUR", c.getShortName());
        assertEquals("Euro", c.getFullName());
        assertEquals("EUR", c.getCode());
    }

}
