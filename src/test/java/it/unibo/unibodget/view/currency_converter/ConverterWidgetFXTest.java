package it.unibo.unibodget.view.currency_converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.MainInfoNotificationEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConverterWidgetFX}.
 */
class ConverterWidgetFXTest {

    private static final double RATE_EUR = 1.0;
    private static final double RATE_USD = 1.10;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> { });
        } catch (final IllegalStateException ignored) {
            // Already initialized
        }
    }

    @Test
    void shouldSendNotificationWhenAlertIsSet() {
        final ExchangeRateAPI mockApi = new ExchangeRateAPI() {
            @Override
            public Map<CurrencyUnit, Double> getLatestRates(final CurrencyUnit base) {
                return Map.of(FiatCurrency.EUR, RATE_EUR, FiatCurrency.USD, RATE_USD);
            }

            @Override
            public Map<LocalDate, Double> getHistoricalRates(
                    final CurrencyUnit base,
                    final CurrencyUnit target,
                    final LocalDate from,
                    final LocalDate to) {
                return Map.of();
            }
        };

        final UniversalPriceService priceService = new UniversalPriceService(
                mockApi,
                unit -> BigDecimal.ONE,
                unit -> BigDecimal.ONE
        );
        final BasicCurrencyConverter converter = new BasicCurrencyConverter(priceService);
        final CurrencyConverterController controller =
                new CurrencyConverterController(mockApi, converter);
        final CurrencyAlertService alertService = new CurrencyAlertService();

        final ConverterWidgetFX widget = new ConverterWidgetFX(controller, alertService);
        assertNotNull(widget.getView());

        final List<String> receivedMessages = new ArrayList<>();
        final Consumer<MainInfoNotificationEvent> listener =
                event -> receivedMessages.add(event.msg());
        MessageBus.subscribe(MainInfoNotificationEvent.class, listener);

        try {
            widget.getThresholdField().setText("1.25");
            widget.getAddAlertButton().fire();

            assertEquals(1, receivedMessages.size());
            assertEquals("Alert threshold set correctly!", receivedMessages.get(0));
            assertTrue(widget.getThresholdField().getText().isEmpty());
        } finally {
            MessageBus.unsubscribe(MainInfoNotificationEvent.class, listener);
        }
    }
}
