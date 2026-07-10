package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CryptoCurrencyTest {

    @Test
    void shouldExposeFieldsCorrectly() {
        CryptoCurrency c = CryptoCurrency.BTC;

        assertEquals(CurrencyType.CRYPTO, c.getType());
        assertEquals("₿", c.getSymbol());
        assertEquals("BTC", c.getShortName());
        assertEquals("Bitcoin", c.getFullName());
        assertEquals("BTC", c.getCode());
        assertEquals("bitcoin", c.getApiId());
        assertFalse(c.isStableCoin());
    }

    @Test
    void shouldIdentifyStableCoins() {
        assertTrue(CryptoCurrency.USDT.isStableCoin());
        assertTrue(CryptoCurrency.USDC.isStableCoin());
    }

}
