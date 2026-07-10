package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;

/**
 * Client implementation of {@link ExchangeRateAPI} that retrieves historical
 * currency exchange rates from the Frankfurter API.
 * <p>
 * This class performs HTTP GET requests, parses JSON responses using a
 * lightweight regular-expression-based extractor, and returns a chronological
 * map of exchange rates keyed by {@link LocalDate}.
 * <p>
 * Notes:
 * <ul>
 *     <li>The Frankfurter API supports historical ranges using the format
 *         {@code /YYYY-MM-DD..YYYY-MM-DD?from=XXX&to=YYY}.</li>
 *     <li>Debug print statements are included to help diagnose malformed URLs,
 *         empty responses, or unexpected JSON formats.</li>
 *     <li>JSON parsing is intentionally minimal and tailored to the API's
 *         predictable structure.</li>
 * </ul>
 */
public class ExchangeRateAPIClient implements ExchangeRateAPI {

    /**
     * Fetches historical exchange rates for a given currency pair within the
     * specified date range.
     * <p>
     * The method constructs a Frankfurter API URL, performs an HTTP request,
     * and parses the JSON response into a sorted map of dates and rates.
     *
     * @param base   the base currency (e.g., EUR)
     * @param target the target currency (e.g., USD)
     * @param from   the start date of the historical range
     * @param to     the end date of the historical range
     * @return a {@link TreeMap} containing dates mapped to exchange rates;
     *         empty if an error occurs or no data is available
     */
    @Override
    public Map<LocalDate, Double> getHistoricalRates(
            CurrencyUnit base, CurrencyUnit target,
            LocalDate from, LocalDate to) {

        String urlString = String.format(
                "https://api.frankfurter.app/%s..%s?from=%s&to=%s",
                from, to, base.getCode(), target.getCode()
        );

        System.out.println("DEBUG URL: " + urlString);

        try {
            String json = fetchJson(urlString);
            System.out.println("DEBUG JSON: " + json);
            return parseFrankfurterJson(json, target.getCode());
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Historical API failed, switching to offline.");
            return new TreeMap<>();
        }
    }

    /**
     * Parses the JSON returned by the Frankfurter API to extract historical
     * exchange rates for the specified target currency.
     * <p>
     * Expected JSON format:
     * <pre>
     * {
     *   "rates": {
     *     "2026-06-05": {"USD": 1.05},
     *     "2026-06-06": {"USD": 1.06}
     *   }
     * }
     * </pre>
     * <p>
     * A regular expression is used to extract date–rate pairs without relying
     * on external JSON libraries.
     *
     * @param json   the raw JSON response
     * @param target the target currency code (e.g., "USD")
     * @return a sorted map of dates and exchange rates
     */
    protected Map<LocalDate, Double> parseFrankfurterJson(String json, String target) {
        Map<LocalDate, Double> history = new TreeMap<>();

        Pattern p = Pattern.compile(
                "\"(\\d{4}-\\d{2}-\\d{2})\":\\{[^}]*\"" + target + "\":([\\d.]+)\\}"
        );
        Matcher m = p.matcher(json);

        while (m.find()) {
            LocalDate date = LocalDate.parse(m.group(1));
            double rate = Double.parseDouble(m.group(2));
            history.put(date, rate);
        }

        return history;
    }

    /**
     * Fetches the raw JSON response from the specified URL using an HTTP GET request.
     * <p>
     * The method handles redirects and sets a user-agent header to avoid
     * request rejections. If the request fails or returns a non-200 status code,
     * an empty string is returned.
     * 
     * @param url the URL to fetch
     * @return the raw JSON response as a string, or an empty string on failure
     */
    private String fetchJson(String url) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("HTTP error: " + response.statusCode());
                return "";
            }
            return response.body();
        } catch (Exception e) {
            // offline, DNS error, connect error, timeout, ecc.
            return "";
        }
    }

    /**
     * Retrieves the latest exchange rates for the given base currency.
     * <p>
     * This implementation currently returns an empty map and serves as a
     * placeholder for future API integration.
     *
     * @param base the base currency for which latest rates should be fetched
     * @return an empty map (not yet implemented)
     */
    @Override
    public Map<CurrencyUnit, Double> getLatestRates(CurrencyUnit base) {
        return new HashMap<>();
    }
}
