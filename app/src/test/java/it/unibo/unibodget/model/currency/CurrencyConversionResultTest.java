package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CurrencyConversionResultTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        final CurrencyConversionResult r = new CurrencyConversionResult(
                new BigDecimal("10"),
                FiatCurrency.EUR,
                FiatCurrency.USD,
                new BigDecimal("1.2"),
                new BigDecimal("12")
        );

        assertEquals(new BigDecimal("10"), r.getAmount());
        assertEquals(new BigDecimal("12"), r.getConvertedAmount());
        assertEquals(new BigDecimal("1.2"), r.getAppliedRate());
        assertEquals(FiatCurrency.EUR, r.getFrom());
        assertEquals(FiatCurrency.USD, r.getTo());
    }

}
