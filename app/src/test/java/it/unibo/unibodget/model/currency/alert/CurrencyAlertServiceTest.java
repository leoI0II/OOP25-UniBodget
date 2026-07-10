package it.unibo.unibodget.model.currency.alert;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;

class CurrencyAlertServiceTest {

    @Test
    void shouldTriggerMatchingAlerts() {
        CurrencyAlertService service = new CurrencyAlertService();

        CurrencyAlert a1 = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true);
        CurrencyAlert a2 = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.20, false);
        CurrencyAlert a3 = new CurrencyAlert(FiatCurrency.GBP, FiatCurrency.USD, 1.10, true);

        service.addAlert(a1);
        service.addAlert(a2);
        service.addAlert(a3);

        List<CurrencyAlert> triggered = service.checkAlerts(1.05, FiatCurrency.EUR, FiatCurrency.USD);

        assertEquals(1, triggered.size());
        assertTrue(triggered.contains(a1));
    }

    @Test
    void shouldReturnEmptyListWhenNoAlertsMatch() {
        CurrencyAlertService service = new CurrencyAlertService();

        service.addAlert(new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true));

        List<CurrencyAlert> triggered = service.checkAlerts(1.20, FiatCurrency.GBP, FiatCurrency.USD);

        assertTrue(triggered.isEmpty());
    }

}
