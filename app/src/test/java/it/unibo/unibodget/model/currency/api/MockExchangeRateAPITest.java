package it.unibo.unibodget.model.currency.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;

class MockExchangeRateAPITest {

    private static final double VAL_1_2 = 1.2;
    private static final int YEAR_2024 = 2024;
    private static final int MONTH_1 = 1;
    private static final int DAY_OF_M_1 = 1;
    private static final int DAY_OF_M_3 = 3;

    @Test
    void shouldReturnLatestMockRates() {
        final Map<CurrencyUnit, Double> mock = Map.of(FiatCurrency.USD, VAL_1_2);
        final MockExchangeRateAPI api = new MockExchangeRateAPI(FiatCurrency.EUR, mock);

        final Map<CurrencyUnit, Double> result = api.getLatestRates(FiatCurrency.EUR);

        assertEquals(VAL_1_2, result.get(FiatCurrency.USD));
    }

    @Test
    void shouldReturnHistoricalMockRates() {
        final Map<CurrencyUnit, Double> mock = Map.of(FiatCurrency.USD, VAL_1_2);
        final MockExchangeRateAPI api = new MockExchangeRateAPI(FiatCurrency.EUR, mock);

        final LocalDate from = LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_1);
        final LocalDate to = LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_3);

        final Map<LocalDate, Double> history =
                api.getHistoricalRates(FiatCurrency.EUR, FiatCurrency.USD, from, to);

        assertEquals(3, history.size());
        assertTrue(history.values().stream().allMatch(v -> v == VAL_1_2));
    }

}
