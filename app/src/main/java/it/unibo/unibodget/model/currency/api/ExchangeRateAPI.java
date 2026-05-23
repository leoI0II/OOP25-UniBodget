package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.time.LocalDate;
import java.util.Map;

/**
 * Defines the contract for retrieving exchange-rate information from an external source.
 * Implementations may rely on HTTP requests, cached data, or mock values.
 * The API provides both real-time rates and historical time-series data.
 */
public interface ExchangeRateAPI {

    /**
     * Retrieves the most recent exchange rates relative to a given base currency.
     * The returned map associates each currency with its rate expressed as
     * "target per base".
     *
     * @param base the base currency for which the rate table is requested
     * @return a map of currencies to their latest exchange-rate values
     */
    Map<CurrencyUnit, Double> getLatestRates(CurrencyUnit base);

    /**
     * Retrieves historical exchange-rate values for a specific currency pair
     * over a given date range. The returned map associates each date with
     * the corresponding exchange rate.
     *
     * @param base the base currency
     * @param target the target currency
     * @param from the start date of the historical interval (inclusive)
     * @param to the end date of the historical interval (inclusive)
     * @return a map of dates to exchange-rate values
     */
    Map<LocalDate, Double> getHistoricalRates(CurrencyUnit base, CurrencyUnit target,
                                              LocalDate from, LocalDate to);
}
