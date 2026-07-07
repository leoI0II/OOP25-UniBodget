package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of {@link ExchangeRateAPI} used for testing or offline mode.
 * This class returns fixed, hard-coded exchange rates and does not perform any
 * network communication.
 */
public class MockExchangeRateAPI implements ExchangeRateAPI {

    private final Map<CurrencyUnit, Double> mockRates = new HashMap<>();

    /**
     * Creates a new mock API with predefined exchange rates.
     *
     * @param base the base currency for which the mock rates are defined
     * @param rates a map of target currencies to their mock exchange-rate values
     */
    public MockExchangeRateAPI(CurrencyUnit base, Map<CurrencyUnit, Double> rates) {
        mockRates.putAll(rates);
    }

    @Override
    public Map<CurrencyUnit, Double> getLatestRates(CurrencyUnit base) {
        return mockRates;
    }

    @Override
    public Map<LocalDate, Double> getHistoricalRates(CurrencyUnit base, CurrencyUnit target,
                                                     LocalDate from, LocalDate to) {
        Map<LocalDate, Double> history = new HashMap<>();

        double value = mockRates.getOrDefault(target, 1.0);

        LocalDate date = from;
        while (!date.isAfter(to)) {
            history.put(date, value);
            date = date.plusDays(1);
        }

        return history;
    }

    public static Map<CurrencyUnit, Double> defaultMockRates() {
        Map<CurrencyUnit, Double> map = new HashMap<>();
        map.put(CurrencyUnit.getByCode("EUR"), 1.0);
        map.put(CurrencyUnit.getByCode("USD"), 1.1);
        map.put(CurrencyUnit.getByCode("CAD"), 1.6);
        map.put(CurrencyUnit.getByCode("JPY"), 170.0);
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
        Map<CurrencyUnit, Double> map = new HashMap<>();
        //parsing from json
        for (var currency : Currency.all()) {
            CurrencyUnit unit = CurrencyUnit.getByCode(currency.getCode());
            if (unit != null) {
                // generate a simple mock value via random
                double mockValue = 0.5 + Math.random();
                map.put(unit, mockValue);
            }
        }
        return map;
    }
}
