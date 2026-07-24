package it.unibo.unibodget.model.currency.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;

class MarketAlertTest {

    @Test
    void shouldDelegateToAlertService() {
        final CurrencyAlertService service = new CurrencyAlertService();

        final CurrencyAlert alert = new CurrencyAlert(FiatCurrency.EUR, FiatCurrency.USD, 1.10, true);
        service.addAlert(alert);

        final List<CurrencyAlert> triggered = service.checkAlerts(1.05, FiatCurrency.EUR, FiatCurrency.USD);

        assertEquals(1, triggered.size());
        assertTrue(triggered.contains(alert));
    }

}
