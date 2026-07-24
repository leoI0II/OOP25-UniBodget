package it.unibo.unibodget.model.currency.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;

class CurrencyAlertServiceTest {

    @Test
    void shouldTriggerMatchingAlerts() {
        final CurrencyAlertService service = new CurrencyAlertService();

        final CurrencyAlert a1 = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true);
        final CurrencyAlert a2 = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.20, false);
        final CurrencyAlert a3 = new CurrencyAlert(FiatCurrency.GBP, FiatCurrency.USD, 1.10, true);

        service.addAlert(a1);
        service.addAlert(a2);
        service.addAlert(a3);

        final List<CurrencyAlert> triggered = service.checkAlerts(1.05, FiatCurrency.EUR, FiatCurrency.USD);

        assertEquals(1, triggered.size());
        assertTrue(triggered.contains(a1));
    }

    @Test
    void shouldReturnEmptyListWhenNoAlertsMatch() {
        final CurrencyAlertService service = new CurrencyAlertService();

        service.addAlert(new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true));

        final List<CurrencyAlert> triggered = service.checkAlerts(1.20, FiatCurrency.GBP, FiatCurrency.USD);

        assertTrue(triggered.isEmpty());
    }

}
