package it.unibo.unibodget.view.currency_converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import org.junit.jupiter.api.Test;

class MarketAlertViewTest {

    @Test
    void shouldInstantiateWithService() {
        final CurrencyAlertService service = new CurrencyAlertService();
        final MarketAlertView view = new MarketAlertView(service);
        assertNotNull(view);
    }
}
