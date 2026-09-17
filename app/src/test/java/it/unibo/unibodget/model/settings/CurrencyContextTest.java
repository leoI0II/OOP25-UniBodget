package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CurrencyContextTest {

    @Test
    void shouldSetAndGetBaseCurrency() {
        CurrencyContext.setBase("USD");
        assertEquals("USD", CurrencyContext.getBase());
    }

}
