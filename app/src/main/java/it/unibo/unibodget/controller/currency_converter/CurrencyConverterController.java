package it.unibo.unibodget.controller.currency_converter;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Controller responsible for coordinating interactions between the currency model
 * (API + converter) and the UI layer. This class exposes high-level operations
 * such as performing conversions and retrieving historical exchange-rate data.
 *
 * <p>No business logic is implemented here: all computations are delegated to
 * the model layer. The controller only orchestrates calls and handles errors
 * before forwarding results to the view.</p>
 */
public class CurrencyConverterController {

    private final ExchangeRateAPI api;
    private final CurrencyConverter converter;

    /**
     * Creates a new {@code CurrencyConverterController}.
     *
     * @param api       the exchange-rate provider used to retrieve rate tables
     * @param converter the converter used to perform currency conversions
     */
    public CurrencyConverterController(ExchangeRateAPI api, CurrencyConverter converter) {
        this.api = api;
        this.converter = converter;
    }

    /**
     * Converts an amount from one currency to another.
     *
     * @param amount the amount to convert
     * @param from   the source currency
     * @param to     the target currency
     * @return a {@link CurrencyConversionResult} describing the conversion outcome
     * @throws IllegalArgumentException if the conversion cannot be performed
     */
    public BigDecimal convert(BigDecimal amount, CurrencyUnit from, CurrencyUnit to) {
        try {
            CurrencyConversionResult result = converter.convert(amount, from, to);
            return result.getConvertedAmount();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to perform conversion: " + e.getMessage());
        }
    }

    /**
     * Retrieves historical exchange-rate values for a currency pair.
     *
     * @param base   the base currency
     * @param target the target currency
     * @param from   the start date (inclusive)
     * @param to     the end date (inclusive)
     * @return a map of dates to exchange-rate values
     */
    public Map<LocalDate, Double> getHistoricalRates(CurrencyUnit base, CurrencyUnit target,
                                                     LocalDate from, LocalDate to) {
        return api.getHistoricalRates(base, target, from, to);
    }

    /**
     * Retrieves the latest exchange-rate table for the given base currency.
     *
     * @param base the base currency
     * @return a map of currencies to their latest exchange-rate values
     */
    public Map<CurrencyUnit, Double> getLatestRates(CurrencyUnit base) {
        return api.getLatestRates(base);
    }
}
