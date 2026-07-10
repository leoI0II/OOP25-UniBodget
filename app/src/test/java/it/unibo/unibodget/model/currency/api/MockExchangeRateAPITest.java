package it.unibo.unibodget.model.currency.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;

class MockExchangeRateAPITest {

    @Test
    void shouldReturnLatestMockRates() {
        Map<CurrencyUnit, Double> mock = Map.of(FiatCurrency.USD, 1.2);
        MockExchangeRateAPI api = new MockExchangeRateAPI(FiatCurrency.EUR, mock);

        Map<CurrencyUnit, Double> result = api.getLatestRates(FiatCurrency.EUR);

        assertEquals(1.2, result.get(FiatCurrency.USD));
    }

    @Test
    void shouldReturnHistoricalMockRates() {
        Map<CurrencyUnit, Double> mock = Map.of(FiatCurrency.USD, 1.2);
        MockExchangeRateAPI api = new MockExchangeRateAPI(FiatCurrency.EUR, mock);

        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 3);

        Map<LocalDate, Double> history =
                api.getHistoricalRates(FiatCurrency.EUR, FiatCurrency.USD, from, to);

        assertEquals(3, history.size());
        assertTrue(history.values().stream().allMatch(v -> v == 1.2));
    }

}
