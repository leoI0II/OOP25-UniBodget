package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FiatCurrencyTest {

    private static final String USD = "USD";
    private static final String EUR = "EUR";

    @Test
    void shouldExposeFieldsCorrectly() {
        final FiatCurrency c = FiatCurrency.EUR;

        assertEquals(CurrencyType.FIAT, c.getType());
        assertEquals("€", c.getSymbol());
        assertEquals(EUR, c.getShortName());
        assertEquals("Euro", c.getFullName());
        assertEquals(EUR, c.getCode());
    }

    @Test
    void shouldResolveFromCode() {
        assertEquals(FiatCurrency.USD, FiatCurrency.fromCode(USD));
        assertEquals(FiatCurrency.USD, FiatCurrency.fromCode("usd"));
        assertEquals(FiatCurrency.EUR, FiatCurrency.fromCode(EUR));
        assertEquals(FiatCurrency.GBP, FiatCurrency.fromCode("GBP"));
        assertEquals(FiatCurrency.EUR, FiatCurrency.fromCode("UNKNOWN"));
        assertEquals(FiatCurrency.EUR, FiatCurrency.fromCode(null));
    }
}
