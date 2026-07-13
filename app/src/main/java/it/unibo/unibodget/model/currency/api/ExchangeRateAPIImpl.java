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
 * <p>
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
    private Instant lastUpdate = Instant.MIN;

    /** Cached exchange rates keyed by {@link CurrencyUnit}. */
    private Map<CurrencyUnit, Double> cachedRates = new HashMap<>();

    /**
     * Returns the latest exchange rates relative to the given base currency.
     *
     * <p>
     * If cached data is still valid, it is returned immediately. Otherwise,
     * a new request is sent to the external API.
     *
     * @param base the base currency for which rates should be retrieved
     * @return a map of currency units to their exchange rate relative to {@code base}
     */
    @Override
    public Map<CurrencyUnit, Double> getLatestRates(final CurrencyUnit base) {
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
    public Map<LocalDate, Double> getHistoricalRates(final CurrencyUnit base, final CurrencyUnit target,
                                                    final LocalDate from, final LocalDate to) {
        return generateMockHistory(from, to);
    }

    /**
     * Performs an HTTP request to fetch the latest exchange rates from the API.
     *
     * @param base the base currency
     * @return a map of parsed exchange rates
     */
    private Map<CurrencyUnit, Double> fetchRatesFromAPI(final CurrencyUnit base) {
        String body = "";

        try {
            final String url = "https://open.er-api.com/v6/latest/" + base.getCode();

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            final HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            body = response.body();
            System.out.println("API response: " + body);

        } catch (final Exception e) {
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
    private Map<CurrencyUnit, Double> parseRates(final String json, final CurrencyUnit base) {
        final Map<CurrencyUnit, Double> result = new HashMap<>();

        final int ratesStart = json.indexOf("\"rates\":");
        if (ratesStart == -1){
            return result;
        }

        final int braceOpen = json.indexOf("{", ratesStart);
        if (braceOpen == -1){ 
            return result;
        }

        // Find matching closing brace for the "rates" object
        int depth = 0;
        int braceClose = -1;

        for (int i = braceOpen; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{'){
                depth++;
            }
            if (c == '}'){
                depth--;
            }
            if (depth == 0) {
                braceClose = i;
                break;
            }
        }

        if (braceClose == -1){
            return result;
        }

        final String ratesBlock = json.substring(braceOpen + 1, braceClose);
        final String[] entries = ratesBlock.split(",");

        for (final String entry : entries) {
            final String[] parts = entry.split(":");
            if (parts.length != 2){
                continue;
            }

            final String code = parts[0].replace("\"", "").trim();
            final String valueStr = parts[1].trim();

            final CurrencyUnit unit = CurrencyUnit.getByCode(code);
            if (unit != null) {
                try {
                    final double value = Double.parseDouble(valueStr);
                    result.put(unit, value);
                } catch (final Exception ignored) {
                    System.out.println("Parse error in ExchangedRateAPIImpl: " + ignored.getMessage());
                }
            }
        }

        // Base currency always has rate 1.0
        result.put(base, 1.0);
        return result;
    }

    /**
     * Generates mock historical exchange rate data for the given date range.
     *
     * <p>
     * Each date is assigned a pseudo-random value between 0.5 and 1.5.
     * This method is used only because the external API does not provide
     * historical data.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return a map of dates to generated mock values
     */
    private Map<LocalDate, Double> generateMockHistory(final LocalDate from, final LocalDate to) {
        final Map<LocalDate, Double> map = new HashMap<>();
        LocalDate date = from;

        while (!date.isAfter(to)) {
            map.put(date, 0.5 + Math.random());
            date = date.plusDays(1);
        }

        return map;
    }
}
