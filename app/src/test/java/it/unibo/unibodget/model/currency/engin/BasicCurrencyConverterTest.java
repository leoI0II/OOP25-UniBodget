package it.unibo.unibodget.model.currency.engin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;

class BasicCurrencyConverterTest {

    /**
     * Fixed test prices:
     * 1 USD = 0.8 EUR;
     * 1 stock share = 500 USD;
     * 1 crypto unit = 60000 USD.
     */
    private BasicCurrencyConverter createConverter() {
        final UniversalPriceService service = new UniversalPriceService(
                new MockAPI(),
                unit -> new BigDecimal("60000"),
                unit -> new BigDecimal("500")
        );

        return new BasicCurrencyConverter(service);
    }

    private static void assertAmountEquals(
            final String expected,
            final BigDecimal actual) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                "Expected " + expected + ", obtained " + actual
        );
    }

    @Test
    void shouldReturnSameAmountWhenCurrenciesMatch() {
        final CurrencyConversionResult result = createConverter().convert(
                new BigDecimal("50"),
                FiatCurrency.EUR,
                FiatCurrency.EUR
        );

        assertAmountEquals("50", result.getConvertedAmount());
        assertAmountEquals("1", result.getAppliedRate());
    }

    @Test
    void shouldConvertCorrectly() {
        final CurrencyConversionResult result = createConverter().convert(
                new BigDecimal("100"),
                FiatCurrency.EUR,
                FiatCurrency.USD
        );

        // 1 EUR = 1 / 0.8 USD = 1.25 USD.
        assertAmountEquals("125", result.getConvertedAmount());
        assertAmountEquals("1.25", result.getAppliedRate());
    }

    @Test
    void shouldConvertStockToFiat() {
        final CurrencyConversionResult result = createConverter().convert(
                new BigDecimal("2"),
                StockMarketCurrency.AAPL,
                FiatCurrency.EUR
        );

        // 2 shares × 500 USD/share ÷ 1.25 USD/EUR = 800 EUR.
        assertAmountEquals("800", result.getConvertedAmount());
        assertAmountEquals("400", result.getAppliedRate());
    }

    @Test
    void shouldConvertFiatToStock() {
        final CurrencyConversionResult result = createConverter().convert(
                new BigDecimal("800"),
                FiatCurrency.EUR,
                StockMarketCurrency.AAPL
        );

        assertAmountEquals("2", result.getConvertedAmount());
        assertAmountEquals("0.0025", result.getAppliedRate());
    }

    @Test
    void shouldConvertCryptoToStock() {
        final CurrencyConversionResult result = createConverter().convert(
                BigDecimal.ONE,
                CryptoCurrency.BTC,
                StockMarketCurrency.AAPL
        );

        // 60000 USD/BTC ÷ 500 USD/share = 120 shares/BTC.
        assertAmountEquals("120", result.getConvertedAmount());
        assertAmountEquals("120", result.getAppliedRate());
    }

    @Test
    void shouldFailWhenRateMissing() {
        final CurrencyUnit fake = new Currency(
                CurrencyType.FIAT, "?", "FAKE", "Fake", "FAKE"
        );

        assertThrows(IllegalArgumentException.class, () ->
                createConverter().convert(
                        BigDecimal.TEN,
                        fake,
                        FiatCurrency.EUR
                )
        );
    }

    @Test
    void shouldUseEmergencyFallbackWhenStockPriceIsInvalid() {
        final UniversalPriceService service = new UniversalPriceService(
                new MockAPI(),
                unit -> new BigDecimal("60000"),
                unit -> BigDecimal.ZERO
        );

        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(service);

        final CurrencyConversionResult result = converter.convert(
                BigDecimal.TEN,
                FiatCurrency.USD,
                StockMarketCurrency.AAPL
        );

        // Emergency fallback: 1 stock share = 1 USD.
        assertAmountEquals("10", result.getConvertedAmount());
        assertAmountEquals("1", result.getAppliedRate());
    }

    private static final class MockAPI implements ExchangeRateAPI {

        @Override
        public Map<CurrencyUnit, Double> getLatestRates(
                final CurrencyUnit base) {
            assertEquals(FiatCurrency.USD, base);

            return Map.of(
                    FiatCurrency.USD, 1.0,
                    FiatCurrency.EUR, 0.8,
                    FiatCurrency.GBP, 0.75
            );
        }

        @Override
        public Map<LocalDate, Double> getHistoricalRates(
                final CurrencyUnit base,
                final CurrencyUnit target,
                final LocalDate from,
                final LocalDate to) {
            throw new AssertionError(
                    "Current conversion must not request historical rates"
            );
        }
    }
}
