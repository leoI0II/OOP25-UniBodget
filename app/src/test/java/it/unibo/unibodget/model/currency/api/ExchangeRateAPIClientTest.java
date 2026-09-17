package it.unibo.unibodget.model.currency.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ExchangeRateAPIClientTest {

    private static final double EXPECTED_2 = 2;
    private static final double EXPECTED_1_05 = 1.05;
    private static final double EXPECTED_1_10 = 1.10;
    private static final int YEAR_2024 = 2024;
    private static final int MONTH_1 = 1;
    private static final int DAY_OF_M_1 = 1;
    private static final int DAY_OF_M_2 = 2;

    @Test
    void shouldParseFrankfurterJsonCorrectly() {
        final ExchangeRateAPIClient client = new ExchangeRateAPIClient();

        final String json = """
            {"rates":{
                "2024-01-01":{"USD":1.05},
                "2024-01-02":{"USD":1.10}
            }}
            """;

        final Map<LocalDate, Double> parsed =
                client.parseFrankfurterJson(json, "USD");

        assertEquals(EXPECTED_2, parsed.size());
        assertEquals(EXPECTED_1_05, parsed.get(LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_1)));
        assertEquals(EXPECTED_1_10, parsed.get(LocalDate.of(YEAR_2024, MONTH_1, DAY_OF_M_2)));
    }

}
