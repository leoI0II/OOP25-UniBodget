package it.unibo.unibodget.model.currency.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * <p>Requests the latest price from CoinGecko.
     * Returns {@link BigDecimal#ZERO} if no valid price is available.</p>
     *
     * @param unit the cryptocurrency whose price is requested
     * @return the USD price of one unit, or zero if unavailable
     * @throws IllegalArgumentException if the asset is not a {@link CryptoCurrency}
     */
    @Override
    public BigDecimal getPriceInUSD(final CurrencyUnit unit) {
        return getPricesInUSD(List.of(unit)).getOrDefault(unit, BigDecimal.ZERO);
    }

    /**
     * {@inheritDoc}
     *
     * <p>All the coins are asked in ONE request ({@code ?ids=bitcoin,ethereum,...}),
     * for the rate limit of CoinGecko it counts as one call, not one per coin.</p>
     *
     * @throws IllegalArgumentException if one of the assets is not a {@link CryptoCurrency}
     * @throws IllegalStateException if the request fails
     */
    @Override
    public Map<CurrencyUnit, BigDecimal> getPricesInUSD(final List<? extends CurrencyUnit> units) {
        final List<CryptoCurrency> cryptos = units.stream()
                .map(CoinGeckoPriceProvider::asCrypto)
                .toList();

        final String url =
            API_URL
            + "?ids="
            + cryptos.stream()
                    .map(CryptoCurrency::getApiId)
                    .distinct()
                    .collect(Collectors.joining(","))
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

            return parsePrices(response.body(), cryptos);

        } catch (final IOException e) {
            throw new IllegalStateException("Failed to retrieve CoinGecko data.", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to retrieve CoinGecko data.", e);
        }
    }

    /**
     * Checks that the asset is a cryptocurrency, the only type CoinGecko knows.
     *
     * @param unit the asset
     * @return the asset as a {@link CryptoCurrency}
     * @throws IllegalArgumentException if it is not a cryptocurrency
     */
    private static CryptoCurrency asCrypto(final CurrencyUnit unit) {
        if (!(unit instanceof CryptoCurrency crypto)) {
            throw new IllegalArgumentException(
                "CoinGecko supports only CRYPTO currencies."
            );
        }
        return crypto;
    }

    /**
     * Parses the JSON response and extracts the USD value of each coin.
     *
     * <p>Response example: {@code {"bitcoin":{"usd":81738.77},"ethereum":{"usd":2423.48}}}.
     * A coin without a valid number (e.g. the response of the rate limit has only
     * an error message) is not put in the map.</p>
     *
     * @param json raw json response
     * @param cryptos the coins that were requested
     * @return the USD value of each coin found in the response
     * @throws IOException if parsing fails
     */
    protected Map<CurrencyUnit, BigDecimal> parsePrices(final String json, final List<CryptoCurrency> cryptos)
        throws IOException {
        final Map<CurrencyUnit, BigDecimal> prices = new HashMap<>();
        final JsonNode root = mapper.readTree(json);
        if (root == null) {
            return prices;
        }
        for (final CryptoCurrency crypto : cryptos) {
            final JsonNode usd = root.path(crypto.getApiId()).path("usd");
            if (usd.isNumber()) {
                prices.put(crypto, usd.decimalValue());
            }
        }
        return prices;
    }

}
