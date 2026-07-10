package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class BankConversionResultTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        BankConversionResult r = new BankConversionResult(
                new BigDecimal("120"),
                new BigDecimal("5"),
                new BigDecimal("105"),
                "EUR",
                "USD"
        );

        assertEquals(new BigDecimal("120"), r.getConvertedAmount());
        assertEquals(new BigDecimal("5"), r.getCommission());
        assertEquals(new BigDecimal("105"), r.getTotalCost());
        assertEquals("EUR", r.getSourceCurrencyCode());
        assertEquals("USD", r.getTargetCurrencyCode());
    }

}
