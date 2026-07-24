package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CurrencyPlaceholderTest {

    @Test
    void shouldStoreCodeCorrectly() {
        final CurrencyPlaceholder p = new CurrencyPlaceholder("XYZ");

        assertEquals("XYZ", p.getCode());
        assertEquals("XYZ", p.getSymbol());
        assertEquals("XYZ", p.getShortName());
        assertEquals("XYZ", p.getFullName());
        assertEquals(CurrencyType.FIAT, p.getType());
    }

}
