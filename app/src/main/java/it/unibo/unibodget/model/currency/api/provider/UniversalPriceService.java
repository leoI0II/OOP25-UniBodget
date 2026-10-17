package it.unibo.unibodget.model.currency.api.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;

/**
 * Resolves asset prices in USD and caches stock and crypto prices.
 */
public class UniversalPriceService {

    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    private static final int PRICE_SCALE = 20;

    private final ExchangeRateAPI fiatApi;
    private final PriceProvider cryptoProvider;
    private final PriceProvider stockProvider;

    private final Map<String, CachedPrice> cachedPrices = new HashMap<>();

    /**
     * A successfully retrieved price and its retrieval time.
     */
    private record CachedPrice(BigDecimal price, Instant fetchedAt) {
    }

    /**
     * Creates the universal price service.
     *
     * @param fiatApi fiat exchange-rate API
     * @param cryptoProvider cryptocurrency price provider
     * @param stockProvider stock price provider
     */
    public UniversalPriceService(
            final ExchangeRateAPI fiatApi,
            final PriceProvider cryptoProvider,
            final PriceProvider stockProvider) {

        this.fiatApi = Objects.requireNonNull(fiatApi);
        this.cryptoProvider = Objects.requireNonNull(cryptoProvider);
        this.stockProvider = Objects.requireNonNull(stockProvider);
    }

    /**
     * Returns the USD value of one asset unit.
     *
     * @param unit the currency or asset
     * @return a finite, positive price in USD
     */
    public BigDecimal getPriceInUSD(final CurrencyUnit unit) {
        Objects.requireNonNull(unit, "unit");

        System.out.println("[PRICE] Resolving " + unit.getCode()
                + " as " + unit.getType());

        return switch (unit.getType()) {
            case FIAT, CUSTOM -> resolveFiatPrice(unit);
            // all the coins of the enum in one request, so the others are already in cache
            case CRYPTO -> getCachedPrice(
                    unit, cryptoProvider, "CoinGecko",
                    unit instanceof CryptoCurrency ? List.of(CryptoCurrency.values()) : List.of(unit)
            );
            // alpha vantage (free) gives one stock for request, so only the asked one
            case STOCK -> getCachedPrice(
                    unit, stockProvider, "Alpha Vantage", List.of(unit)
            );
            default -> throw new IllegalArgumentException(
                    "Unsupported asset type: " + unit.getType()
            );
        };
    }

    /**
     * Retrieves a cached price or fetches a new one after expiration.
     * Failed requests and invalid prices are never cached.
     *
     * <p>On a miss the prices of all the {@code batch} assets are requested together
     * and all the valid ones are cached, not only the one asked.</p>
     */
    private synchronized BigDecimal getCachedPrice(
            final CurrencyUnit unit,
            final PriceProvider provider,
            final String providerName,
            final List<? extends CurrencyUnit> batch) {

        final String key = cacheKey(unit);

        final CachedPrice cached = cachedPrices.get(key);
        final Instant now = Instant.now();

        if (cached != null
                && now.isBefore(
                        cached.fetchedAt().plus(CACHE_DURATION)
                )) {

            System.out.println("[CACHE] HIT " + key
                    + ": " + cached.price() + " USD"
                    + " — no HTTP request");

            return cached.price();
        }

        System.out.println("[CACHE] "
                + (cached == null ? "MISS " : "EXPIRED ")
                + key + " — requesting " + providerName);

        Map<CurrencyUnit, BigDecimal> prices;
        try {
            prices = provider.getPricesInUSD(batch);
        } catch (final Exception e) {
            System.err.println("[CACHE] Errore richiesta provider per "
                    + unit.getCode() + ": " + e.getMessage());
            prices = Map.of();
        }

        // Timestamp only after a successful request.
        final Instant fetchedAt = Instant.now();
        prices.forEach((asset, assetPrice) -> {
            if (assetPrice != null && assetPrice.signum() > 0) {
                cachedPrices.put(cacheKey(asset), new CachedPrice(assetPrice, fetchedAt));
            }
        });

        final BigDecimal price = prices.get(unit);

        if (price == null || price.signum() <= 0) {
            if (cached != null) {
                System.out.println("[CACHE] Fallback su vecchia cache per "
                        + key + ": " + cached.price() + " USD");
                return cached.price();
            }

            // before here it returned 1 USD, but a fake price is worse than an error:
            // e.g. 3 BTC were valued 3 USD. with the exception the caller can use its own fallback
            // (InvestmentAccount uses the average buy price of the position)
            throw new IllegalArgumentException(
                    "No price available for " + key + " (" + providerName + " not reachable)"
            );
        }

        System.out.println("[CACHE] STORED " + prices.size() + " prices from " + providerName
                + " (" + key + ": " + price + " USD)"
                + " — expires at "
                + fetchedAt.plus(CACHE_DURATION));

        return price;
    }

    /**
     * Returns the cache key of an asset, e.g. {@code CRYPTO:BTC}.
     */
    private static String cacheKey(final CurrencyUnit unit) {
        return unit.getType().name()
                + ":"
                + unit.getCode().toUpperCase(Locale.ROOT);
    }

    /**
     * Resolves fiat prices using the existing fiat API cache.
     */
    private BigDecimal resolveFiatPrice(final CurrencyUnit unit) {
        if ("USD".equalsIgnoreCase(unit.getCode())) {
            return BigDecimal.ONE;
        }

        final Map<CurrencyUnit, Double> rates =
                fiatApi.getLatestRates(FiatCurrency.USD);

        final Double rawRate = rates.entrySet().stream()
                .filter(entry -> entry.getKey().getCode()
                        .equalsIgnoreCase(unit.getCode()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (rawRate == null
                || !Double.isFinite(rawRate)
                || rawRate <= 0) {
            throw new IllegalArgumentException(
                    "No valid USD -> " + unit.getCode()
                            + " rate available"
            );
        }

        final BigDecimal usdToTarget = BigDecimal.valueOf(rawRate);

        final BigDecimal targetInUsd = BigDecimal.ONE.divide(
                usdToTarget,
                PRICE_SCALE,
                RoundingMode.HALF_UP
        );

        if (targetInUsd.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid USD price for " + unit.getCode()
            );
        }

        System.out.println("[PRICE] Fiat provider: 1 USD = "
                + usdToTarget + " " + unit.getCode());
        System.out.println("[PRICE] Inverted: 1 "
                + unit.getCode() + " = "
                + targetInUsd + " USD");

        return targetInUsd;
    }

}
