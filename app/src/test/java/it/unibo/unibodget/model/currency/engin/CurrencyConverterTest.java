package it.unibo.unibodget.model.currency.engin;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;

class CurrencyConverterTest {

    @Test
    void shouldConvertCorrectly() {
        ExchangeRateAPI api = new ExchangeRateAPI() {
            @Override
            public Map<CurrencyUnit, Double> getLatestRates(CurrencyUnit base) {
                return Map.of(
                    FiatCurrency.EUR, 1.0,
                    FiatCurrency.USD, 1.2
                );
            }

            @Override
            public Map<LocalDate, Double> getHistoricalRates(
                    CurrencyUnit base, CurrencyUnit target,
                    LocalDate from, LocalDate to) {
                return Map.of();
            }
        };

        BasicCurrencyConverter converter =
                new BasicCurrencyConverter(api, FiatCurrency.EUR);

        CurrencyConversionResult result =
                converter.convert(new BigDecimal("100"), FiatCurrency.EUR, FiatCurrency.USD);

        assertEquals(new BigDecimal("120.0000000000"), result.getConvertedAmount());
        assertEquals(new BigDecimal("1.2000000000"), result.getAppliedRate());
    }

}
