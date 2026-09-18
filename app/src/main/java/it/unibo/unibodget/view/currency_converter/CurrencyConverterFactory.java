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
 * Factory responsible for creating a fully configured
 * {@link CurrencyConverterController} instance.
 *
 * <p>The resulting controller is ready to be used by the UI layer
 * and provides:</p>
 * <ul>
 *     <li>latest exchange rates</li>
 *     <li>conversion engine</li>
 *     <li>base currency management</li>
 * </ul>
 */
public final class CurrencyConverterFactory {

    private CurrencyConverterFactory() {
        // Prevent instantiation
    }

    /**
     * Creates and configures a {@link CurrencyConverterController}.
     *
     * <p>Steps performed:</p>
     * <ol>
     *     <li>Instantiate the real API implementation.</li>
     *     <li>Fetch latest rates for the base currency (EUR).</li>
     *     <li>If the API response is incomplete, switch to a mock API.</li>
     *     <li>Create a {@link BasicCurrencyConverter} using the chosen API.</li>
     *     <li>Return a controller that wraps both API and converter.</li>
     * </ol>
     *
     * @return a fully initialized {@link CurrencyConverterController}
     */
    public static CurrencyConverterController create() {

        ExchangeRateAPI api = new ExchangeRateAPIImpl();

        final FiatCurrency baseCurrency = FiatCurrency.EUR;

        final Map<CurrencyUnit, Double> latest =
                api.getLatestRates(baseCurrency);

        // Fallback to mock API if the real one does not provide enough data
        if (latest.size() <= 1) {
            api = new MockExchangeRateAPI(
                    baseCurrency,
                    MockExchangeRateAPI.generateMockRatesFromCurrencies()
            );
        }

        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(api, baseCurrency);

        return new CurrencyConverterController(api, converter);
    }
}
