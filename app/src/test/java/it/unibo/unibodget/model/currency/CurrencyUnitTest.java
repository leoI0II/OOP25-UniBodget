package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CurrencyUnitTest {

    @Test
    void shouldFindCurrencyByCode() {
        assertEquals(FiatCurrency.EUR, CurrencyUnit.getByCode("EUR"));
        assertEquals(CryptoCurrency.BTC, CurrencyUnit.getByCode("BTC"));
        assertEquals(StockMarketCurrency.AAPL, CurrencyUnit.getByCode("AAPL"));
    }

    @Test
    void shouldReturnAllCurrencies() {
        assertFalse(CurrencyUnit.allCurrencies().isEmpty());
    }

    @Test
    void shouldFilterBasicCurrencies() {
        var list = CurrencyUnit.basicCurrencies();

        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(c ->
                c.getType() == CurrencyType.FIAT
        ));
    }

}
