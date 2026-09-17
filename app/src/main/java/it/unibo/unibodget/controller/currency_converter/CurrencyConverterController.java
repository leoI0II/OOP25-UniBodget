package it.unibo.unibodget.controller.currency_converter;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.currency.history.CurrencyHistoryPoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for coordinating interactions between the currency model
 * (exchange-rate API + conversion engine) and the UI layer.
 * 
 * <p>
 * This class exposes high-level operations such as performing conversions,
 * retrieving historical exchange-rate data, and preparing chart-ready history points.
 * 
 * <br>
 * No business logic is implemented here: all computations are delegated to the
 * underlying model components. The controller only orchestrates calls and handles
 * errors before forwarding results to the view.
 */
public class CurrencyConverterController {

    private final ExchangeRateAPI api;
    private final CurrencyConverter converter;

    /**
     * Creates a new {@code CurrencyConverterController}.
     *
     * @param api       the exchange-rate provider used to retrieve rate tables;
     *                  must not be {@code null}
     * @param converter the converter used to perform currency conversions;
     *                  must not be {@code null}
     */
    public CurrencyConverterController(final ExchangeRateAPI api, final CurrencyConverter converter) {
        this.api = api;
        this.converter = converter;
    }

    /**
     * Converts an amount from one currency to another using the underlying
     * {@link CurrencyConverter}.
     *
     * @param amount the monetary amount to convert; must not be {@code null}
     * @param from   the source currency unit
     * @param to     the target currency unit
     * @return the converted amount as a {@link BigDecimal}
     *
     * @throws IllegalArgumentException if the conversion cannot be performed,
     *                                  for example due to missing exchange-rate data
     */
    public BigDecimal convert(final BigDecimal amount, final CurrencyUnit from, final CurrencyUnit to) {
        try {
            final CurrencyConversionResult result = converter.convert(amount, from, to);
            return result.getConvertedAmount();
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to perform conversion: " + e.getMessage());
        }
    }

    /**
     * Retrieves historical exchange-rate values for a currency pair over a given date range.
     *
     * @param base   the base currency (denominator of the rate)
     * @param target the target currency (numerator of the rate)
     * @param from   the start date (inclusive)
     * @param to     the end date (inclusive)
     * @return a map where each key is a date and each value is the exchange rate
     *         for the given currency pair on that date
     *
     * @throws IllegalArgumentException if the API cannot retrieve historical data
     */
    public Map<LocalDate, Double> getHistoricalRates(final CurrencyUnit base, final CurrencyUnit target,
                                                     final LocalDate from, final LocalDate to) {
        return api.getHistoricalRates(base, target, from, to);
    }

    /**
     * Retrieves the latest available exchange-rate table for the given base currency.
     *
     * @param base the base currency for which the latest rate table is requested
     * @return a map of currency units to their latest exchange-rate values
     *
     * @throws IllegalArgumentException if the API cannot retrieve the latest rates
     */
    public Map<CurrencyUnit, Double> getLatestRates(final CurrencyUnit base) {
        return api.getLatestRates(base);
    }

    /**
     * Returns the underlying {@link CurrencyConverter} used by this controller.
     * 
     * <p>
     * This may be useful for advanced operations or for accessing converter-specific
     * configuration.
     *
     * @return the converter instance
     */
    public CurrencyConverter getConverter() {
        return this.converter;
    }

    /**
     * Retrieves historical exchange-rate values for a currency pair and transforms them
     * into a chronologically ordered list of {@link CurrencyHistoryPoint} objects.
     * 
     * <p>
     * This method is intended for chart views or any UI component that requires
     * pre-structured historical data.
     *
     * @param base   the base currency
     * @param target the target currency
     * @param from   the start date (inclusive)
     * @param to     the end date (inclusive)
     * @return a list of {@link CurrencyHistoryPoint} objects ordered by date
     *
     * @throws IllegalArgumentException if historical data cannot be retrieved
     */
    public List<CurrencyHistoryPoint> getHistoryPoints(final CurrencyUnit base, final CurrencyUnit target,
                                                       final LocalDate from, final LocalDate to) {
        final Map<LocalDate, Double> rates = getHistoricalRates(base, target, from, to);

        final List<CurrencyHistoryPoint> points = new ArrayList<>();
        for (final Map.Entry<LocalDate, Double> entry : rates.entrySet()) {
            points.add(new CurrencyHistoryPoint(entry.getKey(), entry.getValue()));
        }

        points.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
        return points;
    }
}
