package it.unibo.unibodget.model.currency.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;

class CurrencyAlertTest {

    private static final double THRESHOLD_1_10 = 1.10;
    private static final double CURRENT_RATE_1_05 = 1.05;
    private static final double CURRENT_RATE_1_15 = 1.15;
    private static final double CURRENT_RATE_1_20 = 1.20;

    @Test
    void shouldTriggerWhenRateBelowThreshold() {
        final CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, THRESHOLD_1_10, true);

        assertTrue(alert.check(CURRENT_RATE_1_05, FiatCurrency.EUR, FiatCurrency.USD));
        assertFalse(alert.check(CURRENT_RATE_1_15, FiatCurrency.EUR, FiatCurrency.USD));
    }

    @Test
    void shouldTriggerWhenRateAboveThreshold() {
        final CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, THRESHOLD_1_10, false);

        assertTrue(alert.check(CURRENT_RATE_1_20, FiatCurrency.EUR, FiatCurrency.USD));
        assertFalse(alert.check(CURRENT_RATE_1_05, FiatCurrency.EUR, FiatCurrency.USD));
    }

    @Test
    void shouldNotTriggerOnDifferentCurrencyPair() {
        final CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, THRESHOLD_1_10, true);

        assertFalse(alert.check(CURRENT_RATE_1_05, FiatCurrency.USD, FiatCurrency.EUR));
        assertFalse(alert.check(CURRENT_RATE_1_05, FiatCurrency.GBP, FiatCurrency.USD));
    }

    @Test
    void shouldExposeSourceAndTargetCurrencies() {
        final CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, THRESHOLD_1_10, true);

        assertEquals(FiatCurrency.EUR, alert.getSourceCurrency());
        assertEquals(FiatCurrency.USD, alert.getTargetCurrency());
    }

}
