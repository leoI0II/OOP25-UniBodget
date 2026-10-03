package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.provider.CoinGeckoPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.currency.api.provider.AlphaPriceProvider;

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
        final ExchangeRateAPI fiatApi = new ExchangeRateAPIImpl();

        final UniversalPriceService priceService =
                new UniversalPriceService(
                        fiatApi,
                        new CoinGeckoPriceProvider(),
                        new AlphaPriceProvider()
                );

        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(priceService);

        System.out.println("[FACTORY] Fiat: ExchangeRateAPIImpl");
        System.out.println("[FACTORY] Crypto: CoinGeckoPriceProvider");
        System.out.println("[FACTORY] Stock: AlphaPriceProvider");
        System.out.println("[FACTORY] Conversion via USD prices");

        return new CurrencyConverterController(fiatApi, converter);
    }

}
