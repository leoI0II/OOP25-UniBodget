package it.unibo.unibodget.model.currency.alert;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;

class CurrencyAlertTest {

    @Test
    void shouldTriggerWhenRateBelowThreshold() {
        CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true);

        assertTrue(alert.check(1.05, FiatCurrency.EUR, FiatCurrency.USD));
        assertFalse(alert.check(1.15, FiatCurrency.EUR, FiatCurrency.USD));
    }

    @Test
    void shouldTriggerWhenRateAboveThreshold() {
        CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, false);

        assertTrue(alert.check(1.20, FiatCurrency.EUR, FiatCurrency.USD));
        assertFalse(alert.check(1.05, FiatCurrency.EUR, FiatCurrency.USD));
    }

    @Test
    void shouldNotTriggerOnDifferentCurrencyPair() {
        CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true);

        assertFalse(alert.check(1.05, FiatCurrency.USD, FiatCurrency.EUR));
        assertFalse(alert.check(1.05, FiatCurrency.GBP, FiatCurrency.USD));
    }

    @Test
    void shouldExposeSourceAndTargetCurrencies() {
        CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true);

        assertEquals(FiatCurrency.EUR, alert.getSourceCurrency());
        assertEquals(FiatCurrency.USD, alert.getTargetCurrency());
    }

}
