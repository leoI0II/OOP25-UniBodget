package it.unibo.unibodget.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.WalletDeletedEvent;
import it.unibo.unibodget.model.utils.event.WalletRenamedEvent;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests for wallet renaming and deletion, and for the events they publish.
 */
class WalletRenameDeleteTest {

    private static final String OLD_NAME = "Old";
    private static final String NEW_NAME = "New";

    private final List<WalletRenamedEvent> renamedEvents = new ArrayList<>();
    private final List<WalletDeletedEvent> deletedEvents = new ArrayList<>();
    private Consumer<WalletRenamedEvent> renamedToken;
    private Consumer<WalletDeletedEvent> deletedToken;
    private CashAccountService service;

    @BeforeEach
    void setUp() {
        renamedToken = MessageBus.subscribe(WalletRenamedEvent.class, renamedEvents::add);
        deletedToken = MessageBus.subscribe(WalletDeletedEvent.class, deletedEvents::add);
        service = new CashAccountService();
    }

    @AfterEach
    void tearDown() {
        MessageBus.unsubscribe(WalletRenamedEvent.class, renamedToken);
        MessageBus.unsubscribe(WalletDeletedEvent.class, deletedToken);
    }

    @Test
    void setNameTrimsAndPublishesRenameEvent() {
        final CashAccount wallet = new CashAccount(OLD_NAME, FiatCurrency.EUR);

        wallet.setName("  New name  ");

        assertEquals("New name", wallet.getName());
        assertEquals(1, renamedEvents.size());
        final WalletRenamedEvent event = renamedEvents.getFirst();
        assertSame(wallet, event.wallet());
        assertEquals(OLD_NAME, event.oldName());
        assertEquals("New name", event.newName());
    }

    @Test
    void setNameRejectsBlankNameWithoutPublishing() {
        final CashAccount wallet = new CashAccount(OLD_NAME, FiatCurrency.EUR);

        assertThrows(IllegalArgumentException.class, () -> wallet.setName("   "));

        assertEquals(OLD_NAME, wallet.getName());
        assertTrue(renamedEvents.isEmpty());
    }

    @Test
    void setNameWithSameNameIsNoOp() {
        final CashAccount wallet = new CashAccount("Same", FiatCurrency.EUR);

        wallet.setName(" Same ");

        assertTrue(renamedEvents.isEmpty());
    }

    @Test
    void renameWalletRenamesWalletById() {
        final CashAccount wallet = new CashAccount(OLD_NAME, FiatCurrency.EUR);
        service.addWallet(wallet);

        assertTrue(service.renameWallet(wallet.getId(), NEW_NAME));

        assertEquals(NEW_NAME, wallet.getName());
        assertEquals(1, renamedEvents.size());
    }

    @Test
    void renameWalletReturnsFalseForUnknownId() {
        service.addWallet(new CashAccount(OLD_NAME, FiatCurrency.EUR));

        assertFalse(service.renameWallet(UUID.randomUUID(), NEW_NAME));
        assertTrue(renamedEvents.isEmpty());
    }

    @Test
    void removeWalletPublishesDeletedEventWithRemovedWallet() {
        final CashAccount wallet = new CashAccount("To delete", FiatCurrency.EUR);
        service.addWallet(wallet);

        assertTrue(service.removeWallet(wallet.getId()));

        assertTrue(service.getWallets().isEmpty());
        assertEquals(1, deletedEvents.size());
        assertSame(wallet, deletedEvents.getFirst().wallet());
    }

    @Test
    void removeWalletWithUnknownIdPublishesNothing() {
        service.addWallet(new CashAccount("Kept", FiatCurrency.EUR));

        assertFalse(service.removeWallet(UUID.randomUUID()));

        assertEquals(1, service.getWallets().size());
        assertTrue(deletedEvents.isEmpty());
    }

    @Test
    void removingCurrentWalletSelectsFirstRemaining() {
        final CashAccount first = new CashAccount("First", FiatCurrency.EUR);
        final CashAccount second = new CashAccount("Second", FiatCurrency.USD);
        service.addWallet(first);
        service.addWallet(second);
        service.selectWallet(second.getId());

        service.removeWallet(second.getId());

        assertEquals(first, service.getCurrentWallet().orElseThrow());
    }
}
