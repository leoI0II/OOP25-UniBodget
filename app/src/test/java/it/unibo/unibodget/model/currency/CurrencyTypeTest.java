package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CurrencyTypeTest {

    @Test
    void shouldContainAllTypes() {
        assertNotNull(CurrencyType.FIAT);
        assertNotNull(CurrencyType.CRYPTO);
        assertNotNull(CurrencyType.STOCK);
        assertNotNull(CurrencyType.CUSTOM);
    }

}
