package it.unibo.unibodget.view.currency_converter;

import java.util.Map;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.MockExchangeRateAPI;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;

/**
 * Temporary launcher class used to start the Currency Converter dashboard
 * without the full UniBodget application.
 * 
 * <p>
 * It initializes:
 * <ul>
 *     <li>a basic exchange‑rate API implementation</li>
 *     <li>a {@link BasicCurrencyConverter} with a chosen base currency</li>
 *     <li>a {@link CurrencyConverterController} that orchestrates conversions</li>
 * </ul>
 * and then launches {@link CurrencyConverterViewFX}.
 */
public class TestCurrencyConverterApp {

    /**
     * Entry point for launching the standalone Currency Converter dashboard.
     * <p>
     * This method:
     * <ol>
     *     <li>creates an API client for exchange rates</li>
     *     <li>sets EUR as the base currency</li>
     *     <li>creates a basic converter engine</li>
     *     <li>creates the main controller</li>
     *     <li>launches the JavaFX dashboard</li>
     * </ol>
     *
     * @param args ignored
     */
    public static void main(final String[] args) {

        // 1. API for exchange rates (historical + latest)
        ExchangeRateAPI api = new ExchangeRateAPIImpl();
        
        // 2. Base currency for conversion engine
        final var baseCurrency = FiatCurrency.EUR;

        // 3. Check online / offline or error mode
        final Map<CurrencyUnit, Double> latest = api.getLatestRates(baseCurrency);
        if (latest.size() <= 1) {
            System.out.println("Offline mode: using mock API");
            api = new MockExchangeRateAPI(
                    baseCurrency,
                    MockExchangeRateAPI.generateMockRatesFromCurrencies()
            );
        } else {
            System.out.println("Online mode: using real API");
        }

        // 3. Conversion engine using the API
        final var converter = new BasicCurrencyConverter(api, baseCurrency);

        // 4. Main controller orchestrating conversions
        final var controller = new CurrencyConverterController(api, converter);

        // 5. Launch the FX dashboard
        CurrencyConverterViewFX.launchWith(controller);
    }
}
