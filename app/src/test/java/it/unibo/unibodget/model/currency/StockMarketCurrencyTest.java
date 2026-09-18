package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StockMarketCurrencyTest {

    @Test
    void shouldExposeFieldsCorrectly() {
        final StockMarketCurrency c = StockMarketCurrency.AAPL;

        assertEquals(CurrencyType.STOCK, c.getType());
        assertEquals("$", c.getSymbol());
        assertEquals("AAPL", c.getShortName());
        assertEquals("Apple Inc.", c.getFullName());
        assertEquals("AAPL", c.getCode());
    }

}
