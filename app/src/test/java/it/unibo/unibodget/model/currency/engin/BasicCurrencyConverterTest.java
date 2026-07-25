package it.unibo.unibodget.model.currency.engin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;

class BasicCurrencyConverterTest {

    static class MockAPI implements ExchangeRateAPI {
        @Override
        public Map<CurrencyUnit, Double> getLatestRates(final CurrencyUnit base) {
            return Map.of(
                FiatCurrency.EUR, 1.0,
                FiatCurrency.USD, 1.2,
                FiatCurrency.GBP, 0.8
            );
        }

        @Override
        public Map<LocalDate, Double> getHistoricalRates(final CurrencyUnit base, final CurrencyUnit target,
                                                         final LocalDate from, final LocalDate to) {
            return Map.of();
        }
    }

    @Test
    void shouldConvertCorrectly() {
        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(new MockAPI(), FiatCurrency.EUR);

        final CurrencyConversionResult result =
                converter.convert(new BigDecimal("100"), FiatCurrency.EUR, FiatCurrency.USD);

        assertEquals(new BigDecimal("120.0000000000"), result.getConvertedAmount());
        assertEquals(new BigDecimal("1.2000000000"), result.getAppliedRate());
    }

    @Test
    void shouldReturnSameAmountWhenCurrenciesMatch() {
        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(new MockAPI(), FiatCurrency.EUR);

        final CurrencyConversionResult result =
                converter.convert(new BigDecimal("50"), FiatCurrency.EUR, FiatCurrency.EUR);

        assertEquals(new BigDecimal("50"), result.getConvertedAmount());
        assertEquals(BigDecimal.ONE, result.getAppliedRate());
    }

    @Test
    void shouldFailOnStockCurrency() {
        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(new MockAPI(), FiatCurrency.EUR);

        assertThrows(IllegalArgumentException.class, () ->
                converter.convert(new BigDecimal("10"), StockMarketCurrency.AAPL, FiatCurrency.EUR)
        );
    }

    @Test
    void shouldFailWhenRateMissing() {
        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(new MockAPI(), FiatCurrency.EUR);

        final CurrencyUnit fake = new CurrencyUnit() {
            @Override 
            public CurrencyType getType() { 
                return CurrencyType.FIAT; 
            }

            @Override 
            public String getSymbol() { 
                return "?"; 
            }

            @Override 
            public String getShortName() { 
                return "FAKE"; 
            }

            @Override 
            public String getFullName() { 
                return "Fake"; 
            }

            @Override 
            public String getCode() { 
                return "FAKE"; 
            }
        };

        assertThrows(IllegalArgumentException.class, () ->
                converter.convert(new BigDecimal("10"), fake, FiatCurrency.EUR)
        );
    }

}
