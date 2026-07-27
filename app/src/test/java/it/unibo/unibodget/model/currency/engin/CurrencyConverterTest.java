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

class CurrencyConverterTest {

    private static final double VAL_1_0 = 1.0;
    private static final double VAL_1_2 = 1.2;

    @Test
    void shouldConvertCorrectly() {
        final ExchangeRateAPI api = new ExchangeRateAPI() {
            @Override
            public Map<CurrencyUnit, Double> getLatestRates(final CurrencyUnit base) {
                return Map.of(
                    FiatCurrency.EUR, VAL_1_0,
                    FiatCurrency.USD, VAL_1_2
                );
            }

            @Override
            public Map<LocalDate, Double> getHistoricalRates(
                    final CurrencyUnit base, final CurrencyUnit target,
                    final LocalDate from, final LocalDate to) {
                return Map.of();
            }
        };

        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(api, FiatCurrency.EUR);

        final CurrencyConversionResult result =
                converter.convert(new BigDecimal("100"), FiatCurrency.EUR, FiatCurrency.USD);

        assertEquals(new BigDecimal("120.0000000000"), result.getConvertedAmount());
        assertEquals(new BigDecimal("1.2000000000"), result.getAppliedRate());
    }

}
