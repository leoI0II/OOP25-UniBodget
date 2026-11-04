package it.unibo.unibodget.model.currency.api.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.MockExchangeRateAPI;

/**
 * Checks that the crypto prices are asked to CoinGecko all together in one request
 * and cached, and that the rate limit response gives no price (and no fake one).
 */
class CryptoPriceBatchTest {

    private static final BigDecimal BTC_PRICE = new BigDecimal("81738.77");
    private static final BigDecimal ETH_PRICE = new BigDecimal("2423.48");

    @Test
    void shouldReadAllThePricesOfTheResponse() throws IOException {
        final Map<CurrencyUnit, BigDecimal> prices = new CoinGeckoPriceProvider().parsePrices(
                "{\"bitcoin\":{\"usd\":81738.77},\"ethereum\":{\"usd\":2423.48}}",
                List.of(CryptoCurrency.BTC, CryptoCurrency.ETH, CryptoCurrency.SOL));

        assertEquals(0, BTC_PRICE.compareTo(prices.get(CryptoCurrency.BTC)));
        assertEquals(0, ETH_PRICE.compareTo(prices.get(CryptoCurrency.ETH)));
        // not in the response -> not in the map
        assertFalse(prices.containsKey(CryptoCurrency.SOL));
    }

    @Test
    void shouldGiveNoPricesWhenRateLimited() throws IOException {
        final Map<CurrencyUnit, BigDecimal> prices = new CoinGeckoPriceProvider().parsePrices(
                "{\"status\":{\"error_code\":429,\"error_message\":\"You've exceeded the Rate Limit.\"}}",
                List.of(CryptoCurrency.BTC));

        assertTrue(prices.isEmpty());
    }

    @Test
    void shouldAskAllTheCoinsInOneRequest() {
        final AtomicInteger requests = new AtomicInteger();
        final PriceProvider cryptoProvider = new PriceProvider() {
            @Override
            public BigDecimal getPriceInUSD(final CurrencyUnit unit) {
                throw new UnsupportedOperationException("the batch method must be used");
            }

            @Override
            public Map<CurrencyUnit, BigDecimal> getPricesInUSD(final List<? extends CurrencyUnit> units) {
                requests.incrementAndGet();
                final Map<CurrencyUnit, BigDecimal> prices = new HashMap<>();
                units.forEach(unit -> prices.put(unit, BigDecimal.TEN));
                prices.put(CryptoCurrency.BTC, BTC_PRICE);
                prices.put(CryptoCurrency.ETH, ETH_PRICE);
                return prices;
            }
        };
        final UniversalPriceService service = newService(cryptoProvider);

        assertEquals(0, BTC_PRICE.compareTo(service.getPriceInUSD(CryptoCurrency.BTC)));
        // eth and sol came with the first request, so they are already in cache
        assertEquals(0, ETH_PRICE.compareTo(service.getPriceInUSD(CryptoCurrency.ETH)));
        assertEquals(0, BigDecimal.TEN.compareTo(service.getPriceInUSD(CryptoCurrency.SOL)));
        assertEquals(1, requests.get());
    }

    @Test
    void shouldThrowWhenThePriceIsMissing() {
        // like coingecko when rate limited: the request works but there are no prices
        final UniversalPriceService service = newService(new PriceProvider() {
            @Override
            public BigDecimal getPriceInUSD(final CurrencyUnit unit) {
                return BigDecimal.ZERO;
            }

            @Override
            public Map<CurrencyUnit, BigDecimal> getPricesInUSD(final List<? extends CurrencyUnit> units) {
                return Map.of();
            }
        });

        assertThrows(IllegalArgumentException.class, () -> service.getPriceInUSD(CryptoCurrency.BTC));
    }

    private static UniversalPriceService newService(final PriceProvider cryptoProvider) {
        return new UniversalPriceService(
                new MockExchangeRateAPI(FiatCurrency.USD, Map.of()),
                cryptoProvider,
                unit -> BigDecimal.ONE
        );
    }
}
