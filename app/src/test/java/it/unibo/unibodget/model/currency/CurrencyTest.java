package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CurrencyTest {

    private static final String CURR_EUR = "EUR";
    private static final String CURR_TEXT_EUR = "Euro";
    private static final String SYMB_EUR = "€";
    private static final String CURR_USD = "USD";

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
        assertNotNull(Currency.get(CURR_EUR));
        assertNotNull(Currency.get(CURR_USD));
    }

    @Test
    void shouldCreateDynamicCurrency() {
        final Currency c = new Currency(CurrencyType.FIAT, SYMB_EUR, CURR_EUR, CURR_TEXT_EUR, CURR_EUR);

        assertEquals(CURR_EUR, c.getCode());
        assertEquals(CURR_TEXT_EUR, c.getFullName());
        assertEquals(SYMB_EUR, c.getSymbol());
    }

    @Test
    void shouldCompareCurrenciesByCode() {
        final Currency c1 = 
            new Currency(CurrencyType.FIAT, SYMB_EUR, CURR_EUR, CURR_TEXT_EUR, CURR_EUR);
        final Currency c2 = 
            new Currency(CurrencyType.FIAT, SYMB_EUR, CURR_EUR, CURR_TEXT_EUR, CURR_EUR);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

}
