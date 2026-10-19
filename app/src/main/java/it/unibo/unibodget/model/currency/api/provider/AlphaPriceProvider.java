package it.unibo.unibodget.model.currency.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves stock prices from Alpha Vantage.
 *
 * <p>Prices are returned in USD and represent the market value of a single stock share.
 */
public class AlphaPriceProvider implements PriceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaPriceProvider.class);
    private static final String NOTE_KEY = "Note";
    private static final String API_URL = "https://www.alphavantage.co/query";
    private static final String ALPHA_KEY_VARIABLE = "ALPHA_VANTAGE_API_KEY";
    private static final String DEFAULT_ALPHA_KEY = "B56AX33NF4F29HHZ";

    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper mapper;

    /**
     * Creates a provider using a system property, environment variable or the default API key, in
     * that order.
     */
    public AlphaPriceProvider() {
        this(resolveAlphaKey());
    }

    /**
     * Creates a provider using the specified API key.
     *
     * @param apiKey Alpha Vantage API key
     * @throws IllegalStateException if the key is null or blank
     */
    public AlphaPriceProvider(final String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Alpha Vantage API key non configurata all'avvio.");
        }

        this.apiKey = apiKey.strip();
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Resolves the API key, ignoring null and blank configuration values.
     *
     * @return the first non-blank system property or environment value, or the default API key
     */
    private static String resolveAlphaKey() {
        final String propertyKey = System.getProperty(ALPHA_KEY_VARIABLE);
        if (propertyKey != null && !propertyKey.isBlank()) {
            return propertyKey.strip();
        }

        final String environmentKey = System.getenv(ALPHA_KEY_VARIABLE);
        if (environmentKey != null && !environmentKey.isBlank()) {
            return environmentKey.strip();
        }

        return DEFAULT_ALPHA_KEY;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Requests the latest stock quote from Alpha Vantage. Returns zero if the request or parsing
     * fails, or if no valid price is available.
     *
     * @param unit the stock whose price is requested
     * @return the USD price of one share, or zero if unavailable
     * @throws IllegalArgumentException if the asset type is not {@link CurrencyType#STOCK}
     */
    @Override
    public BigDecimal getPriceInUSD(final CurrencyUnit unit) {
        if (unit.getType() != CurrencyType.STOCK) {
            throw new IllegalArgumentException("Alpha Vantage supports STOCK assets only.");
        }

        final String url =
                API_URL
                        + "?function=GLOBAL_QUOTE"
                        + "&symbol="
                        + unit.getCode()
                        + "&apikey="
                        + apiKey;

        try {
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            final HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return parsePrice(response.body());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("[ALPHA] Richiesta interrotta.");
            return BigDecimal.ZERO;
        } catch (final IOException e) {
            LOGGER.error("[ALPHA] Errore di connessione: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Extracts the stock price from the API response.
     *
     * <p>Returns zero if the response reports a rate limit or an error, or if the price is missing
     * or invalid.
     *
     * @param json the raw API response
     * @return the stock price in USD, or zero if unavailable
     * @throws IOException if JSON parsing fails
     */
    protected BigDecimal parsePrice(final String json) throws IOException {
        final JsonNode root = mapper.readTree(json);
        if (root == null) {
            return BigDecimal.ZERO;
        }

        if (root.has(NOTE_KEY) || root.has("Information")) {
            final String limitNotice =
                    root.has(NOTE_KEY)
                            ? root.path(NOTE_KEY).asText()
                            : root.path("Information").asText();

            LOGGER.warn("[ALPHA RATE LIMIT] {}", limitNotice);
            return BigDecimal.ZERO;
        }

        if (root.has("Error Message")) {
            LOGGER.error("[ALPHA ERROR] {}", root.path("Error Message").asText());
            return BigDecimal.ZERO;
        }

        final JsonNode priceNode = root.path("Global Quote").path("05. price");

        if (priceNode.isMissingNode() || priceNode.isNull()) {
            LOGGER.warn("[ALPHA] Campo prezzo non trovato nella risposta.");
            return BigDecimal.ZERO;
        }

        try {
            final BigDecimal price = new BigDecimal(priceNode.asText());
            return price.signum() > 0 ? price : BigDecimal.ZERO;
        } catch (final NumberFormatException e) {
            LOGGER.warn("[ALPHA] Prezzo non valido: {}", priceNode.asText());
            return BigDecimal.ZERO;
        }
    }
}
