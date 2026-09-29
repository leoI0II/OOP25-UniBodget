package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.io.IOException;
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
 * Default implementation of {@link ExchangeRateAPI} that retrieves
 * cryptocurrency exchange rates from the Fawaz Ahmed currency API.
 *
 * <p>
 * This implementation performs live HTTP requests using Java's
 * {@link HttpClient}, applies a simple in-memory caching strategy,
 * and parses the JSON response using lightweight string operations
 * (no external JSON libraries).
 */
public class CryptoExchangeRateAPIImpl implements ExchangeRateAPI {

    private static final int SECONDS_5 = 5;

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
     * Performs an HTTP request to fetch the latest exchange rates from the API.
     *
     * @param base the base currency
     * @return a map of parsed exchange rates
     */
    private Map<CurrencyUnit, Double> fetchRatesFromAPI(final CurrencyUnit base) {
        String body = "";

        try {
            final String url =
                    "https://cdn.jsdelivr.net/npm/"
                    + "@fawazahmed0/currency-api@latest/v1/currencies/"
                    + base.getCode().toLowerCase()
                    + ".json";
            System.out.println("Crypto API URL: " + url);

            final HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(SECONDS_5))
                            .GET()
                            .build();

            final HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            
            System.out.println("HTTP status: " + response.statusCode());
            body = response.body();

            System.out.println("Crypto API response: " + body);

        } catch (final InterruptedException | IOException e) {
            System.out.println(
                    "HTTP error: " + e.getMessage()
            );
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
        final Map<CurrencyUnit, Double> result =
                new HashMap<>();

        final String baseCode =
                base.getCode().toLowerCase();

        final int baseStart =
                json.indexOf("\"" + baseCode + "\":");

        if (baseStart == -1) {
            return result;
        }
        System.out.println("Base currency found: " + baseCode);

        final int braceOpen =
                json.indexOf("{", baseStart);

        if (braceOpen == -1) {
            return result;
        }

        int depth = 0;
        int braceClose = -1;

        for (int i = braceOpen; i < json.length(); i++) {

            final char c = json.charAt(i);

            if (c == '{') {
                depth++;
            }

            if (c == '}') {
                depth--;
            }

            if (depth == 0) {
                braceClose = i;
                break;
            }
        }

        if (braceClose == -1) {
            return result;
        }

        final String ratesBlock =
                json.substring(braceOpen + 1, braceClose);
        System.out.println("Rates block:");
        System.out.println(ratesBlock);

        final String[] entries =
                ratesBlock.split(",");
        
        for (final String entry : entries) {

            final String[] parts =
                    entry.split(":");

            if (parts.length != 2) {
                continue;
            }

            final String code =
                    parts[0]
                            .replace("\"", "")
                            .trim()
                            .toUpperCase();

            final String valueStr =
                    parts[1].trim();

            final CurrencyUnit unit =
                    CurrencyUnit.getByCode(code);

            if (unit != null) {
                try {
                    final double value =
                            Double.parseDouble(valueStr);

                    result.put(unit, value);

                } catch (final NumberFormatException ignored) {
                    System.out.println(
                            "Parse error in CryptoExchangeRateAPIImpl: "
                            + ignored.getMessage()
                    );
                }
            }
        }

        result.put(base, 1.0);

        return result;
    
    }

    /**
     * To be implemented.
     * Not used at the moment.
     * 
     * @base the base currency
     * @target the target currency
     * @from the start date of the historical interval (inclusive)
     * @to the end date of the historical interval (inclusive)
     * @return a map of dates to exchange-rate values
     */
    @Override
    public Map<LocalDate, Double> getHistoricalRates(
            final CurrencyUnit base,
            final CurrencyUnit target,
            final LocalDate from,
            final LocalDate to) {
        // return empty map for now, as historical rates are not implemented
        return new HashMap<>();
    }

}
