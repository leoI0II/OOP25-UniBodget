package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.service.DefaultWalletService;
import it.unibo.unibodget.model.service.WalletService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests for {@link DefaultWalletService}.
 */
class DefaultWalletServiceTest {

    private WalletService<CashTransaction, CashAccount> service;

    @BeforeEach
    void setUp() {
        service = new DefaultWalletService<>();
    }

    @Test
    void shouldSetFirstAddedWalletAsCurrentWallet() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);

        service.addWallet(wallet);

        assertTrue(service.getCurrentWallet().isPresent());
        assertEquals(wallet, service.getCurrentWallet().get());
    }

    @Test
    void shouldSelectWalletById() {
        final CashAccount firstWallet = new CashAccount("First", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount("Second", FiatCurrency.USD);

        service.addWallet(firstWallet);
        service.addWallet(secondWallet);

        final boolean selected = service.selectWallet(secondWallet.getId());

        assertTrue(selected);
        assertEquals(secondWallet, service.getCurrentWallet().get());
    }

    @Test
    void shouldReturnFalseWhenSelectingUnknownWallet() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        final CashAccount otherWallet = new CashAccount("Other wallet", FiatCurrency.USD);

        service.addWallet(wallet);

        final boolean selected = service.selectWallet(otherWallet.getId());

        assertFalse(selected);
        assertEquals(wallet, service.getCurrentWallet().get());
    }

    @Test
    void shouldAddTransactionToCurrentWallet() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        service.addWallet(wallet);

        final Category category = new Category(
                "Test",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        final CashTransaction transaction = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("1000.00")),
                category,
                LocalDate.now(),
                "Salary",
                "Monthly salary"
        );

        service.addTransaction(transaction);

        assertEquals(1, service.getCurrentTransactions().size());
        assertEquals(transaction, service.getCurrentTransactions().get(0));
    }

    @Test
    void shouldRemoveTransactionFromCurrentWallet() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        service.addWallet(wallet);

        final Category category = new Category(
                "Test",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        final CashTransaction transaction = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-50.00")),
                category,
                LocalDate.now(),
                "Groceries",
                "Groceries purchase"
        );

        service.addTransaction(transaction);
        final boolean removed = service.removeTransaction(transaction);

        assertTrue(removed);
        assertTrue(service.getCurrentTransactions().isEmpty());
    }

    @Test
    void shouldClearCurrentWalletHistory() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        service.addWallet(wallet);

        final Category category = new Category(
                "Test",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        service.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("1000.00")),
                category,
                LocalDate.now(),
                "Salary",
                "Monthly salary"
        ));

        service.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-400.00")),
                category,
                LocalDate.now(),
                "Rent",
                "Monthly rent"
        ));

        service.clearCurrentWalletHistory();

        assertTrue(service.getCurrentTransactions().isEmpty());
    }

    @Test
    void shouldNotifyObserversWhenWalletStateChanges() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        final AtomicBoolean notified = new AtomicBoolean(false);

        service.addObserver(() -> notified.set(true));
        service.addWallet(wallet);

        assertTrue(notified.get());
    }

    @Test
    void shouldInitializeCurrentWalletFromConstructorList() {
        final CashAccount firstWallet = new CashAccount("First", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount("Second", FiatCurrency.USD);

        final WalletService<CashTransaction, CashAccount> walletService =
                new DefaultWalletService<>(List.of(firstWallet, secondWallet));

        assertTrue(walletService.getCurrentWallet().isPresent());
        assertEquals(firstWallet, walletService.getCurrentWallet().get());
    }

    @Test
    void shouldSelectAnotherWalletWhenCurrentWalletIsRemoved() {
        final CashAccount firstWallet = new CashAccount("First", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount("Second", FiatCurrency.USD);

        service.addWallet(firstWallet);
        service.addWallet(secondWallet);

        final boolean removed = service.removeWallet(firstWallet.getId());

        assertTrue(removed);
        assertTrue(service.getCurrentWallet().isPresent());
        assertEquals(secondWallet, service.getCurrentWallet().get());
    }
}