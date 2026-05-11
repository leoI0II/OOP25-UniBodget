package it.unibo.unibodget.model.converter.provider;

import java.math.BigDecimal;
import java.util.Map;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;

/**
 * Mock implementation of {@link ExchangeRateProvider} for testing purposes.
 * Returns hardcoded exchange rates for a fixed set of currency pairs.
 * Falls back to a rate of {@code 1.0} for any unknown pair.
 */
public class MockExchangeRateProvider implements ExchangeRateProvider {

    private record Pair(CurrencyUnit from, CurrencyUnit to) {}

    private static final Map<Pair, BigDecimal> RATES = Map.ofEntries(
        // Fiat ↔ Fiat
        Map.entry(new Pair(FiatCurrency.EUR, FiatCurrency.USD),  new BigDecimal("1.10")),
        Map.entry(new Pair(FiatCurrency.USD, FiatCurrency.EUR),  new BigDecimal("0.91")),
        Map.entry(new Pair(FiatCurrency.EUR, FiatCurrency.GBP),  new BigDecimal("0.86")),
        Map.entry(new Pair(FiatCurrency.GBP, FiatCurrency.EUR),  new BigDecimal("1.17")),
        Map.entry(new Pair(FiatCurrency.EUR, FiatCurrency.CHF),  new BigDecimal("0.94")),
        Map.entry(new Pair(FiatCurrency.CHF, FiatCurrency.EUR),  new BigDecimal("1.06")),
        Map.entry(new Pair(FiatCurrency.USD, FiatCurrency.GBP),  new BigDecimal("0.79")),
        Map.entry(new Pair(FiatCurrency.GBP, FiatCurrency.USD),  new BigDecimal("1.27")),
        Map.entry(new Pair(FiatCurrency.USD, FiatCurrency.CHF),  new BigDecimal("0.91")),
        Map.entry(new Pair(FiatCurrency.CHF, FiatCurrency.USD),  new BigDecimal("1.10")),
        // Crypto → Fiat
        Map.entry(new Pair(CryptoCurrency.BTC,  FiatCurrency.USD),    new BigDecimal("50000.0")),
        Map.entry(new Pair(CryptoCurrency.BTC,  FiatCurrency.EUR),    new BigDecimal("45500.0")),
        Map.entry(new Pair(CryptoCurrency.BTC,  CryptoCurrency.USDT), new BigDecimal("50000.0")),
        Map.entry(new Pair(CryptoCurrency.ETH,  FiatCurrency.USD),    new BigDecimal("4000.0")),
        Map.entry(new Pair(CryptoCurrency.ETH,  FiatCurrency.EUR),    new BigDecimal("3640.0")),
        Map.entry(new Pair(CryptoCurrency.ETH,  CryptoCurrency.USDT), new BigDecimal("4000.0")),
        Map.entry(new Pair(CryptoCurrency.USDT, FiatCurrency.USD),  new BigDecimal("1.0")),
        Map.entry(new Pair(CryptoCurrency.USDT, FiatCurrency.EUR),  new BigDecimal("0.91")),
        Map.entry(new Pair(FiatCurrency.USD,    CryptoCurrency.USDT), new BigDecimal("1.0")),
        // Fiat → Crypto
        Map.entry(new Pair(FiatCurrency.USD, CryptoCurrency.BTC),  new BigDecimal("0.00002")),
        Map.entry(new Pair(FiatCurrency.USD, CryptoCurrency.ETH),  new BigDecimal("0.00025")),
        // Stocks → Fiat
        Map.entry(new Pair(StockMarketCurrency.AAPL, FiatCurrency.USD), new BigDecimal("150.0")),
        Map.entry(new Pair(StockMarketCurrency.MSFT, FiatCurrency.USD), new BigDecimal("420.0")),
        Map.entry(new Pair(StockMarketCurrency.NVDA, FiatCurrency.USD), new BigDecimal("900.0"))
    );

    @Override
    public Asset convert(final Asset src, final CurrencyUnit target) {
        if (src.currency().equals(target)) {
            return src;
        }
        final BigDecimal rate = RATES.getOrDefault(
            new Pair(src.currency(), target),
            BigDecimal.ONE
        );
        return Asset.of(target, src.amount().multiply(rate));
    }

}
