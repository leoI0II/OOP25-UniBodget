package it.unibo.unibodget.model.transactions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

class InvestmentTransactionTest {

    @Test
    void shouldCreateInvestmentTransaction() {
        final Asset asset = new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN);
        final Asset unitPrice = new Asset(FiatCurrency.EUR, java.math.BigDecimal.ONE);
        final Asset fee = new Asset(FiatCurrency.EUR, new java.math.BigDecimal("0.5"));

        final InvestmentTransaction t = new InvestmentTransaction(
                asset,
                Category.INVESTMENT_BUY,
                LocalDate.of(2024, 1, 1),
                "desc",
                "notes",
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
                        "d",
                        "n",
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
                        "d",
                        "n",
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
                        "d",
                        "n",
                        new Asset(FiatCurrency.EUR, java.math.BigDecimal.ONE),
                        new Asset(FiatCurrency.EUR, new java.math.BigDecimal("-1"))
                )
        );
    }

}
