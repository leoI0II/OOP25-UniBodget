package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.provider.AlphaPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.CoinGeckoPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory responsible for creating a fully configured {@link CurrencyConverterController} instance.
 *
 * <p>The resulting controller is ready to be used by the UI layer and provides:
 *
 * <ul>
 *   <li>latest exchange rates
 *   <li>conversion engine
 *   <li>base currency management
 * </ul>
 */
public final class CurrencyConverterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyConverterFactory.class);

    private CurrencyConverterFactory() {
        // Prevent instantiation
    }

    /**
     * Creates and configures a {@link CurrencyConverterController}.
     *
     * <p>Steps performed:
     *
     * <ol>
     *   <li>Instantiate the real API implementation.
     *   <li>Fetch latest rates for the base currency (EUR).
     *   <li>If the API response is incomplete, switch to a mock API.
     *   <li>Create a {@link BasicCurrencyConverter} using the chosen API.
     *   <li>Return a controller that wraps both API and converter.
     * </ol>
     *
     * @return a fully initialized {@link CurrencyConverterController}
     */
    public static CurrencyConverterController create() {
        final ExchangeRateAPI fiatApi = new ExchangeRateAPIImpl();

        final UniversalPriceService priceService =
                new UniversalPriceService(
                        fiatApi, new CoinGeckoPriceProvider(), new AlphaPriceProvider());

        final BasicCurrencyConverter converter = new BasicCurrencyConverter(priceService);

        LOGGER.debug("[FACTORY] Fiat: ExchangeRateAPIImpl");
        LOGGER.debug("[FACTORY] Crypto: CoinGeckoPriceProvider");
        LOGGER.debug("[FACTORY] Stock: AlphaPriceProvider");
        LOGGER.debug("[FACTORY] Conversion via USD prices");

        return new CurrencyConverterController(fiatApi, converter);
    }
}
