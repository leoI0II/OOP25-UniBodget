package it.unibo.unibodget.model.currency.engin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;

class CurrencyConverterTest {

    @Test
    void shouldConvertCorrectly() {
        final ExchangeRateAPI api = new ExchangeRateAPI() {

            @Override
            public Map<CurrencyUnit, Double> getLatestRates(
                    final CurrencyUnit base) {

                assertEquals(FiatCurrency.USD, base);

                return Map.of(
                        FiatCurrency.USD, 1.0,
                        FiatCurrency.EUR, 0.8
                );
            }

            @Override
            public Map<LocalDate, Double> getHistoricalRates(
                    final CurrencyUnit base,
                    final CurrencyUnit target,
                    final LocalDate from,
                    final LocalDate to) {

                throw new AssertionError(
                        "Historical rates must not be requested"
                );
            }
        };

        final UniversalPriceService service = new UniversalPriceService(
                api,
                unit -> {
                    throw new AssertionError(
                            "Crypto provider must not be called"
                    );
                },
                unit -> {
                    throw new AssertionError(
                            "Stock provider must not be called"
                    );
                }
        );

        final CurrencyConverter converter =
                new BasicCurrencyConverter(service);

        final CurrencyConversionResult result = converter.convert(
                new BigDecimal("100"),
                FiatCurrency.EUR,
                FiatCurrency.USD
        );

        assertEquals(
                0,
                new BigDecimal("125").compareTo(result.getConvertedAmount())
        );

        assertEquals(
                0,
                new BigDecimal("1.25").compareTo(result.getAppliedRate())
        );
    }
}
