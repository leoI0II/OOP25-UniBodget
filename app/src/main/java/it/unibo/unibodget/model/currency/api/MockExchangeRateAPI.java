package it.unibo.unibodget.model.currency.api;

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
}
