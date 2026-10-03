package it.unibo.unibodget.model.currency.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Retrieves cryptocurrency prices from CoinGecko.
 *
 * <p>
 * Prices are returned in USD and represent the market value
 * of a single cryptocurrency unit.
 */
public class CoinGeckoPriceProvider implements PriceProvider {

    private static final String API_URL =
        "https://api.coingecko.com/api/v3/simple/price";

    private final HttpClient client;
    private final ObjectMapper mapper;

    /**
     * Creates a new CoinGecko provider.
     */
    public CoinGeckoPriceProvider() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Requests the latest stock quote from Coin Gecko.
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

        if (!(unit instanceof CryptoCurrency crypto)) {
            throw new IllegalArgumentException(
                "CoinGecko supports only CRYPTO currencies."
            );
        }

        final String url =
            API_URL
            + "?ids="
            + crypto.getApiId()
            + "&vs_currencies=usd";

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

            return parsePrice(
                response.body(),
                crypto.getApiId()
            );

        } catch (IOException | InterruptedException e) {

            throw new IllegalStateException(
                "Failed to retrieve CoinGecko data.",
                e
            );
        }
    }

    /**
     * Parses the JSON response and extracts the USD value.
     *
     * @param json raw json response
     * @param apiId CoinGecko asset identifier
     * @return asset value expressed in USD
     * @throws IOException if parsing fails
     */
    protected BigDecimal parsePrice(final String json, final String apiId)
        throws IOException {
        final JsonNode root = mapper.readTree(json);

        if (root == null) {
            return BigDecimal.ZERO;
        }

        return root
                .path(apiId)
                .path("usd")
                .decimalValue();
    }

}
