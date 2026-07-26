package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class InvestmentTransactionTest {

        private static final String DESC = "desc";
        private static final String SHORT_DESCR = "d";
        private static final String NOTES = "notes";
        private static final String SHORT_NOTES = "n";

    @Test
    void shouldCreateInvestmentTransaction() {
        final Asset asset = new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN);
        final Asset unitPrice = new Asset(FiatCurrency.EUR, java.math.BigDecimal.ONE);
        final Asset fee = new Asset(FiatCurrency.EUR, new java.math.BigDecimal("0.5"));

        final InvestmentTransaction t = new InvestmentTransaction(
                asset,
                Category.INVESTMENT_BUY,
                LocalDate.of(2024, 1, 1),
                DESC,
                NOTES,
                unitPrice,
                fee
        );

        assertEquals(unitPrice, t.getUnitPrice());
        assertEquals(fee, t.getFee());
    }

    @Test
    void shouldFailIfUnitPriceNull() {
        assertThrows(NullPointerException.class, () ->
                new InvestmentTransaction(
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                        Category.INVESTMENT_BUY,
                        LocalDate.now(),
                        SHORT_DESCR,
                        SHORT_NOTES,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldFailIfUnitPriceNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new InvestmentTransaction(
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                        Category.INVESTMENT_BUY,
                        LocalDate.now(),
                        SHORT_DESCR,
                        SHORT_NOTES,
                        new Asset(FiatCurrency.EUR, new java.math.BigDecimal("-1")),
                        null
                )
        );
    }

    @Test
    void shouldFailIfFeeNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new InvestmentTransaction(
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN),
                        Category.INVESTMENT_BUY,
                        LocalDate.now(),
                        SHORT_DESCR,
                        SHORT_NOTES,
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.ONE),
                        new Asset(FiatCurrency.EUR, new java.math.BigDecimal("-1"))
                )
        );
    }

}
