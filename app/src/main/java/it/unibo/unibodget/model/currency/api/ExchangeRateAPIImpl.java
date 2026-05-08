package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link ExchangeRateAPI} that retrieves currency
 * exchange rates from the public <a href="https://open.er-api.com/">open.er-api.com</a>
 * service.
 *
 * This implementation performs live HTTP requests using Java's
 * {@link HttpClient}, applies a simple in-memory caching strategy, and parses
 * the JSON response using lightweight string operations (no external JSON
 * libraries).
 */
public class ExchangeRateAPIImpl implements ExchangeRateAPI {

    /** Duration for which fetched exchange rates remain valid in cache. */
    private static final Duration CACHE_DURATION = Duration.ofHours(1);

    /** HTTP client used for performing API requests. */
    private final HttpClient client = HttpClient.newHttpClient();

    /** Timestamp of the last successful API update. */
    private Instant lastUpdate = null;

    /** Cached exchange rates keyed by {@link CurrencyUnit}. */
    private Map<CurrencyUnit, Double> cachedRates = new HashMap<>();

    /**
     * Returns the latest exchange rates relative to the given base currency.
     *
     * If cached data is still valid, it is returned immediately. Otherwise,
     * a new request is sent to the external API.
     *
     * @param base the base currency for which rates should be retrieved
     * @return a map of currency units to their exchange rate relative to {@code base}
     */
    @Override
    public Map<CurrencyUnit, Double> getLatestRates(CurrencyUnit base) {
        if (lastUpdate == null || Instant.now().isAfter(lastUpdate.plus(CACHE_DURATION))) {
            cachedRates = fetchRatesFromAPI(base);
            lastUpdate = Instant.now();
        }
        return cachedRates;
    }

    /**
     * Returns historical exchange rates for the given currency pair.
     *
     * Since the external API does not support historical data, this method
     * generates mock values for each date in the requested range.
     *
     * @param base   the base currency
     * @param target the target currency
     * @param from   start date (inclusive)
     * @param to     end date (inclusive)
     * @return a map of dates to mock exchange rate values
     */
    @Override
    public Map<LocalDate, Double> getHistoricalRates(CurrencyUnit base, CurrencyUnit target,
                                                     LocalDate from, LocalDate to) {
        return generateMockHistory(from, to);
    }

    /**
     * Performs an HTTP request to fetch the latest exchange rates from the API.
     *
     * @param base the base currency
     * @return a map of parsed exchange rates
     */
    private Map<CurrencyUnit, Double> fetchRatesFromAPI(CurrencyUnit base) {
        String body = "";

        try {
            String url = "https://open.er-api.com/v6/latest/" + base.getCode();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            body = response.body();
            System.out.println("API response: " + body);

        } catch (Exception e) {
            System.out.println("HTTP error: " + e.getMessage());
        }

        return parseRates(body, base);
    }

    /**
     * Extracts the {@code "rates"} object from the API JSON response using
     * simple string operations.
     *
     * @param json the raw JSON response
     * @param base the base currency (added manually with value {@code 1.0})
     * @return a map of currency units to exchange rates
     */
    private Map<CurrencyUnit, Double> parseRates(String json, CurrencyUnit base) {
        Map<CurrencyUnit, Double> result = new HashMap<>();

        int ratesStart = json.indexOf("\"rates\":");
        if (ratesStart == -1) return result;

        int braceOpen = json.indexOf("{", ratesStart);
        if (braceOpen == -1) return result;

        // Find matching closing brace for the "rates" object
        int depth = 0;
        int braceClose = -1;

        for (int i = braceOpen; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            if (c == '}') depth--;
            if (depth == 0) {
                braceClose = i;
                break;
            }
        }

        if (braceClose == -1) return result;

        String ratesBlock = json.substring(braceOpen + 1, braceClose);
        String[] entries = ratesBlock.split(",");

        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            String code = parts[0].replace("\"", "").trim();
            String valueStr = parts[1].trim();

            CurrencyUnit unit = CurrencyUnit.getByCode(code);
            if (unit != null) {
                try {
                    double value = Double.parseDouble(valueStr);
                    result.put(unit, value);
                } catch (Exception ignored) {}
            }
        }

        // Base currency always has rate 1.0
        result.put(base, 1.0);
        return result;
    }

    /**
     * Generates mock historical exchange rate data for the given date range.
     *
     * Each date is assigned a pseudo-random value between 0.5 and 1.5.
     * This method is used only because the external API does not provide
     * historical data.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return a map of dates to generated mock values
     */
    private Map<LocalDate, Double> generateMockHistory(LocalDate from, LocalDate to) {
        Map<LocalDate, Double> map = new HashMap<>();
        LocalDate date = from;

        while (!date.isAfter(to)) {
            map.put(date, 0.5 + Math.random());
            date = date.plusDays(1);
        }

        return map;
    }
}
