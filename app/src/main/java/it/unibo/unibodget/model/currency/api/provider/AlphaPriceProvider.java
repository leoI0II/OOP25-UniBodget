package it.unibo.unibodget.model.currency.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;

/**
 * Retrieves stock prices from Alpha Vantage.
 *
 * <p>
 * Prices are returned in USD and represent the market value
 * of a single stock share.
 */
public class AlphaPriceProvider implements PriceProvider {

    private static final String API_URL =
        "https://www.alphavantage.co/query";

    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper mapper;

    /**
     * Costruttore di default che recupera la chiave API configurata
     * all'avvio in System Property o variabile d'ambiente.
     */
    public AlphaPriceProvider() {
        this(System.getProperty("ALPHA_VANTAGE_API_KEY", System.getenv("ALPHA_VANTAGE_API_KEY")));
    }

    /**
     * Creates a provider using the specified API key.
     *
     * @param apiKey Alpha Vantage API key
     */
    public AlphaPriceProvider(final String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Alpha Vantage API key non configurata all'avvio.");
        }
        this.apiKey = apiKey;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Requests the latest stock quote from Alpha Vantage.
     * Returns {@link BigDecimal#ZERO} if the request or parsing fails,
     * or if no valid price is available.</p>
     *
     * @param unit the stock whose price is requested
     * @return the USD price of one share, or zero if unavailable
     * @throws IllegalArgumentException if the asset type is not
     *         {@link CurrencyType#STOCK}
     */
    @Override
    public BigDecimal getPriceInUSD(final CurrencyUnit unit) {

        if (unit.getType() != CurrencyType.STOCK) {
            throw new IllegalArgumentException(
                "Alpha Vantage supports STOCK assets only."
            );
        }

        final String url =
            API_URL
            + "?function=GLOBAL_QUOTE"
            + "&symbol="
            + unit.getCode()
            + "&apikey="
            + apiKey;

        try {
            final HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            final HttpResponse<String> response =
                client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            return parsePrice(response.body());

        } catch (final IOException | InterruptedException e) {
            System.err.println("[ALPHA] Errore di connessione: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Extracts the stock price from the API response.
     * Se l'API restituisce un rate-limit o un avviso, stampa il messaggio
     * e restituisce 0.0 senza far crashare la GUI con un'eccezione non gestita.
     *
     * @param json raw response
     * @return stock price in USD, o 0.0 se non disponibile/in rate-limit
     * @throws IOException if parsing fails
     */
    protected BigDecimal parsePrice(final String json) throws IOException {
        final JsonNode root = mapper.readTree(json);
        if (root == null) {
            return BigDecimal.ZERO;
        }

        // Manage rate-limit / alert Alpha Vantage
        if (root.has("Note") || root.has("Information")) {
            final String limitNotice = root.has("Note")
                    ? root.path("Note").asText()
                    : root.path("Information").asText();

            System.err.println("[ALPHA RATE LIMIT] " + limitNotice);
            return BigDecimal.ZERO;
        }

        if (root.has("Error Message")) {
            System.err.println("[ALPHA ERROR] "
                    + root.path("Error Message").asText());
            return BigDecimal.ZERO;
        }

        final JsonNode priceNode = root
                .path("Global Quote")
                .path("05. price");

        if (priceNode.isMissingNode() || priceNode.isNull()) {
            System.err.println("[ALPHA] Campo prezzo non trovato nella risposta.");
            return BigDecimal.ZERO;
        }

        try {
            final BigDecimal price = new BigDecimal(priceNode.asText());
            return price.signum() > 0 ? price : BigDecimal.ZERO;
        } catch (final NumberFormatException e) {
            System.err.println("[ALPHA] Prezzo non valido: " + priceNode.asText());
            return BigDecimal.ZERO;
        }
    }

}
