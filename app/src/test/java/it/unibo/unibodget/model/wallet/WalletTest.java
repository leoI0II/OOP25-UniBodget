package it.unibo.unibodget.model.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

class WalletTest {

    @Test
    void shouldAddTransaction() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        final CashTransaction tx = new CashTransaction(
                new Asset(FiatCurrency.EUR, new java.math.BigDecimal("10")),
                Category.FOOD,
                LocalDate.now(),
                "desc",
                "notes"
        );

        acc.addTransaction(tx);

        assertTrue(acc.getHistory().getTransactions().contains(tx));
    }

    @Test
    void shouldRejectZeroAmountTransaction() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        final CashTransaction zeroTx = new CashTransaction(
                new Asset(FiatCurrency.EUR, java.math.BigDecimal.ZERO),
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

    @Test
    void shouldSaveAndReloadCashAccounts() {
        final CashAccountManager manager = new CashAccountManager();
        final CashAccount account =
                new CashAccount(
                        "Wallet",
                        FiatCurrency.EUR
                );
        manager.saveAll(List.of(account));
        final List<CashAccount> reloaded =
                manager.loadAll();
        assertFalse(reloaded.isEmpty());
        assertEquals(
                account.getName(),
                reloaded.getFirst().getName()
        );
    }

}
