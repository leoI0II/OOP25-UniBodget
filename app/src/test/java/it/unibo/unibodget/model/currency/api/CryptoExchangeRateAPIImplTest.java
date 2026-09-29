package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoExchangeRateAPIImplTest {

    @Test
    void shouldGetBitcoinExchangeRates() {
        final CryptoExchangeRateAPIImpl api =
                new CryptoExchangeRateAPIImpl();

        final Map<CurrencyUnit, Double> rates =
                api.getLatestRates(CryptoCurrency.BTC);

        assertNotNull(rates);
        assertFalse(rates.isEmpty());

        assertTrue(rates.containsKey(CryptoCurrency.BTC));
    }

    @Test
    void shouldContainEuroRate() {
        final CryptoExchangeRateAPIImpl api =
                new CryptoExchangeRateAPIImpl();

        final Map<CurrencyUnit, Double> rates =
                api.getLatestRates(CryptoCurrency.BTC);

        assertTrue(rates.containsKey(FiatCurrency.EUR));
        assertTrue(rates.get(FiatCurrency.EUR) > 0);
    }

    @Test
    void shouldContainDollarRate() {
        final CryptoExchangeRateAPIImpl api =
                new CryptoExchangeRateAPIImpl();

        final Map<CurrencyUnit, Double> rates =
                api.getLatestRates(CryptoCurrency.BTC);

        assertTrue(rates.containsKey(FiatCurrency.USD));
        assertTrue(rates.get(FiatCurrency.USD) > 0);
    }
}