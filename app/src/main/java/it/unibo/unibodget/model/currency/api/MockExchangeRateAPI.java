package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Mock implementation of {@link ExchangeRateAPI} used for testing or offline mode.
 * This class returns fixed, hard-coded exchange rates and does not perform any
 * network communication.
 */
public class MockExchangeRateAPI implements ExchangeRateAPI {

    private final Map<CurrencyUnit, Double> mockRates = new HashMap<>();

    private final static double MEDIUM_VALUE = 1.14;
    private static final double DAILY_VAR = 0.2;

    /**
     * Creates a new mock API with predefined exchange rates.
     *
     * @param base the base currency for which the mock rates are defined
     * @param rates a map of target currencies to their mock exchange-rate values
     */
    public MockExchangeRateAPI(final CurrencyUnit base, final Map<CurrencyUnit, Double> rates) {
        mockRates.putAll(rates);
    }

    @Override
    public Map<CurrencyUnit, Double> getLatestRates(final CurrencyUnit base) {
        return mockRates;
    }

    @Override
    public Map<LocalDate, Double> getHistoricalRates(final CurrencyUnit base, final CurrencyUnit target,
                                                    final LocalDate from, final LocalDate to) {
        final Map<LocalDate, Double> history = new HashMap<>();

        final double value = mockRates.getOrDefault(target, 1.0);

        LocalDate date = from;
        while (!date.isAfter(to)) {
            history.put(date, value);
            date = date.plusDays(1);
        }

        return history;
    }

    /**
     * Generates a mock historical exchange-rate time-series for testing purposes.
     *
     * @param from the start date of the historical interval (inclusive)
     * @param to the end date of the historical interval (inclusive)
     * @return a map of dates to mock exchange-rate values
     */
    public static Map<LocalDate, Double> generateMockHistory(final LocalDate from, final LocalDate to) {
        final Map<LocalDate, Double> history = new TreeMap<>();
        LocalDate date = from;
        double baseValue = MEDIUM_VALUE; // medium value
        while (!date.isAfter(to)) {
            // little daily variation
            final double delta = (Math.random() - 0.5) * DAILY_VAR;
            baseValue += delta;
            history.put(date, baseValue);
            date = date.plusDays(1);
        }
        return history;
    }

    /**
     * Generates a mock map of exchange rates for a given base currency.
     * Each currency is assigned a random exchange rate between 0.5 and 1.5
     * relative to the specified base currency.
     *
     * @param base the base currency for which the mock rates are generated
     * @return a map of {@link CurrencyUnit} to mock exchange rates
     */
    public static Map<CurrencyUnit, Double> generateMockLatestRates(final CurrencyUnit base) {
        final Map<CurrencyUnit, Double> map = generateMockRatesFromCurrencies();
        map.put(base, 1.0);
        return map;
    }

    /**
     * Generates a mock map of exchange rates for all available currencies.
     * Each currency is assigned a random exchange rate between 0.5 and 1.5
     * relative to a base currency (e.g., EUR).
     *
     * @return a map of {@link CurrencyUnit} to mock exchange rates
     */
    public static Map<CurrencyUnit, Double> generateMockRatesFromCurrencies() {
        final Map<CurrencyUnit, Double> map = new HashMap<>();
        //parsing from json
        for (final var currency : Currency.all()) {
            final CurrencyUnit unit = CurrencyUnit.getByCode(currency.getCode());
            if (unit != null) {
                // generate a simple mock value via random
                final double mockValue = 0.5 + Math.random();
                map.put(unit, mockValue);
            }
        }
        return map;
    }
}
