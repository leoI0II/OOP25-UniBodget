package it.unibo.unibodget.model.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

class CashAccountTest {

    @Test
    void shouldComputeBalanceCorrectly() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        acc.addTransaction(new CashTransaction(
                new Asset(FiatCurrency.EUR, new BigDecimal("10")),
                Category.FOOD,
                LocalDate.now(),
                "desc",
                "notes"
        ));

        acc.addTransaction(new CashTransaction(
                new Asset(FiatCurrency.EUR, new BigDecimal("5")),
                Category.FOOD,
                LocalDate.now(),
                "desc",
                "notes"
        ));

        assertEquals(new BigDecimal("15"), acc.getBalance().amount());
    }

    @Test
    void shouldUseDefaultBudgetSettingsWhenNull() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        // DefaultBudgetSettings(BigDecimal.ZERO)
        assertEquals(BigDecimal.ZERO, acc.getBudgetSettings().getLimitValue());
        assertEquals(new BigDecimal("0.8"), acc.getBudgetSettings().getWarningThreshold());
    }

    @Test
    void shouldSetBudgetSettings() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        final DefaultBudgetSettings newSettings = new DefaultBudgetSettings(new BigDecimal("500"));
        acc.setBudgetSettings(newSettings);

        assertEquals(new BigDecimal("500"), acc.getBudgetSettings().getLimitValue());
        assertEquals(new BigDecimal("0.8"), acc.getBudgetSettings().getWarningThreshold());
    }

    @Test
    void shouldRejectZeroAmountTransaction() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        final CashTransaction zeroTx = new CashTransaction(
                new Asset(FiatCurrency.EUR, BigDecimal.ZERO),
                Category.FOOD,
                LocalDate.now(),
                "desc",
                "notes"
        );

        assertThrows(IllegalArgumentException.class, () -> acc.addTransaction(zeroTx));
    }

    @Test
    void shouldGenerateDefaultName() {
        final CashAccount acc = new CashAccount("", FiatCurrency.EUR);

        assertTrue(acc.getName().startsWith("Cash Account"));
    }

}
