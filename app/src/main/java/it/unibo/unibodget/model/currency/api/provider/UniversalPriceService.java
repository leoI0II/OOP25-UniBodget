package it.unibo.unibodget.model.currency.api.provider;

import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves asset prices in USD and caches stock and crypto prices.
 */
public class UniversalPriceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniversalPriceService.class);
    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    private static final int PRICE_SCALE = 20;
    private static final String USD_SUFFIX = " USD";

    private final ExchangeRateAPI fiatApi;
    private final PriceProvider cryptoProvider;
    private final PriceProvider stockProvider;

    private final Map<String, CachedPrice> cachedPrices = new HashMap<>();

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

        LOGGER.debug("[PRICE] Resolving {} as {}", unit.getCode(), unit.getType());

        return switch (unit.getType()) {
            case FIAT, CUSTOM -> resolveFiatPrice(unit);
            case CRYPTO -> getCachedPrice(
                    unit, cryptoProvider, "CoinGecko",
                    unit instanceof CryptoCurrency ? List.of(CryptoCurrency.values()) : List.of(unit));
            case STOCK -> getCachedPrice(
                    unit, stockProvider, "Alpha Vantage", List.of(unit));
        };
    }

    /**
     * Retrieves a cached price or fetches a new one after expiration.
     * Failed requests and invalid prices are never cached.
     *
     * <p>On a miss the prices of all the {@code batch} assets are requested together
     * and all the valid ones are cached, not only the one asked.</p>
     *
     * @param unit the currency or asset
     * @param provider the price provider
     * @param providerName the name of the price provider for logging
     * @param batch the list of assets to request in batch
     * @return the resolved USD price
     */
    private synchronized BigDecimal getCachedPrice(
            final CurrencyUnit unit,
            final PriceProvider provider,
            final String providerName,
            final List<? extends CurrencyUnit> batch) {

        final String key = cacheKey(unit);
        final CachedPrice cached = cachedPrices.get(key);
        final Instant now = Instant.now();

        if (cached != null && now.isBefore(cached.fetchedAt().plus(CACHE_DURATION))) {
            LOGGER.debug(
                    "[CACHE] HIT {}: {}{} — no HTTP request", key, cached.price(), USD_SUFFIX);
            return cached.price();
        }

        LOGGER.debug(
                "[CACHE] {}{} — requesting {}",
                cached == null ? "MISS " : "EXPIRED ",
                key,
                providerName);

        Map<CurrencyUnit, BigDecimal> prices;
        try {
            prices = provider.getPricesInUSD(batch);
        } catch (final IllegalArgumentException | IllegalStateException e) {
            LOGGER.error(
                    "[CACHE] Errore richiesta provider per {}: {}",
                    unit.getCode(),
                    e.getMessage());
            prices = Map.of();
        }

        final Instant fetchedAt = Instant.now();
        prices.forEach((asset, assetPrice) -> {
            if (assetPrice != null && assetPrice.signum() > 0) {
                cachedPrices.put(cacheKey(asset), new CachedPrice(assetPrice, fetchedAt));
            }
        });

        final BigDecimal price = prices.get(unit);

        if (price == null || price.signum() <= 0) {
            if (cached != null) {
                LOGGER.debug(
                        "[CACHE] Fallback su vecchia cache per {}: {}{}",
                        key,
                        cached.price(),
                        USD_SUFFIX);
                return cached.price();
            }

            throw new IllegalArgumentException(
                    "No price available for " + key + " (" + providerName + " not reachable)");
        }

        LOGGER.debug(
                "[CACHE] STORED {} prices from {} ({}: {}{}) — expires at {}",
                prices.size(),
                providerName,
                key,
                price,
                USD_SUFFIX,
                fetchedAt.plus(CACHE_DURATION));

        return price;
    }

    /**
     * Returns the cache key of an asset, e.g. {@code CRYPTO:BTC}.
     *
     * @param unit the currency unit
     * @return the cache key string
     */
    private static String cacheKey(final CurrencyUnit unit) {
        return unit.getType().name()
                + ":"
                + unit.getCode().toUpperCase(Locale.ROOT);
    }

    /**
     * Resolves fiat prices using the existing fiat API cache.
     *
     * @param unit the currency unit
     * @return the calculated USD price
     */
    private BigDecimal resolveFiatPrice(final CurrencyUnit unit) {
        if ("USD".equalsIgnoreCase(unit.getCode())) {
            return BigDecimal.ONE;
        }

        final Map<CurrencyUnit, Double> rates = fiatApi.getLatestRates(FiatCurrency.USD);

        final Double rawRate =
                rates.entrySet().stream()
                        .filter(entry -> entry.getKey().getCode().equalsIgnoreCase(unit.getCode()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);

        if (rawRate == null || !Double.isFinite(rawRate) || rawRate <= 0) {
            throw new IllegalArgumentException(
                    "No valid USD -> " + unit.getCode() + " rate available");
        }

        final BigDecimal usdToTarget = BigDecimal.valueOf(rawRate);

        final BigDecimal targetInUsd =
                BigDecimal.ONE.divide(usdToTarget, PRICE_SCALE, RoundingMode.HALF_UP);

        if (targetInUsd.signum() <= 0) {
            throw new IllegalArgumentException("Invalid USD price for " + unit.getCode());
        }

        LOGGER.debug("[PRICE] Fiat provider: 1 USD = {} {}", usdToTarget, unit.getCode());
        LOGGER.debug("[PRICE] Inverted: 1 {} = {} USD", unit.getCode(), targetInUsd);

        return targetInUsd;
    }

    /**
     * A successfully retrieved price and its retrieval time.
     *
     * @param price the cached price
     * @param fetchedAt the timestamp when the price was fetched
     */
    private record CachedPrice(BigDecimal price, Instant fetchedAt) { }
}
