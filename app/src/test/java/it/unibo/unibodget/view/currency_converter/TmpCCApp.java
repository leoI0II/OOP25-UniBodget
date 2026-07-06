package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;

/**
 * Temporary launcher class used to start the Currency Converter dashboard
 * without the full UniBodget application.
 * It initializes:
 * <ul>
 *     <li>a basic exchange‑rate API implementation</li>
 *     <li>a {@link BasicCurrencyConverter} with a chosen base currency</li>
 *     <li>a {@link CurrencyConverterController} that orchestrates conversions</li>
 * </ul>
 * and then launches {@link CurrencyConverterViewFX}.
 */
public class TmpCCApp2 {

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
    public static void main(String[] args) {

        // 1. API for exchange rates (historical + latest)
        var api = new ExchangeRateAPIImpl();

        // 2. Base currency for conversion engine
        var baseCurrency = FiatCurrency.EUR;

        // 3. Conversion engine using the API
        var converter = new BasicCurrencyConverter(api, baseCurrency);

        // 4. Main controller orchestrating conversions
        var controller = new CurrencyConverterController(api, converter);

        // 5. Launch the FX dashboard
        CurrencyConverterViewFX.launchWith(controller);
    }
}
