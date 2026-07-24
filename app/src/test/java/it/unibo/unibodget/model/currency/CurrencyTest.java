package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CurrencyTest {

    @BeforeEach
    void resetCurrencyManager() throws Exception {
        // 1. Ottieni il campo static final "loaded"
        final Field loadedField = Currency.class.getDeclaredField("LOADED");
        loadedField.setAccessible(true);

        // 2. Ottieni la mappa esistente (non puoi sostituirla)
        @SuppressWarnings("unchecked")
        final Map<String, Currency> loadedMap = (Map<String, Currency>) loadedField.get(null);

        // 3. Svuota la mappa
        loadedMap.clear();

        // 4. Reset "initialized"
        final Field initField = Currency.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, false);
    }

    @Test
    void shouldLoadMockCurrenciesIfJsonMissing() {
        final var all = Currency.all();

        assertFalse(all.isEmpty());
        assertNotNull(Currency.get("EUR"));
        assertNotNull(Currency.get("USD"));
    }

    @Test
    void shouldCreateDynamicCurrency() {
        final Currency c = new Currency(CurrencyType.FIAT, "€", "EUR", "Euro", "EUR");

        assertEquals("EUR", c.getCode());
        assertEquals("Euro", c.getFullName());
        assertEquals("€", c.getSymbol());
    }

    @Test
    void shouldCompareCurrenciesByCode() {
        final Currency c1 = 
            new Currency(CurrencyType.FIAT, "€", "EUR", "Euro", "EUR");
        final Currency c2 = 
            new Currency(CurrencyType.FIAT, "€", "EUR", "Euro", "EUR");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

}
