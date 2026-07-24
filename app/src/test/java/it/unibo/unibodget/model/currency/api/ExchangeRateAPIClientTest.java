package it.unibo.unibodget.model.currency.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ExchangeRateAPIClientTest {

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

        assertEquals(2, parsed.size());
        assertEquals(1.05, parsed.get(LocalDate.of(2024, 1, 1)));
        assertEquals(1.10, parsed.get(LocalDate.of(2024, 1, 2)));
    }

}
