package it.unibo.unibodget.model.dashboard.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.dashboard.support.TransactionObserver;

final class DefaultMovementHistoryServiceTest {

    @Test
    void shouldAddTransactionToHistory() {
        final var service = new DefaultMovementHistoryService();
        service.addTransaction("Coffee -5");
        assertEquals(List.of("Coffee -5"), service.getRecentTransactions());
    }

    @Test
    void shouldRemoveTransactionFromHistory() {
        final var service = new DefaultMovementHistoryService(List.of("Coffee -5", "Salary +1000"));
        final boolean removed = service.removeTransaction("Coffee -5");
        assertTrue(removed);
        assertEquals(List.of("Salary +1000"), service.getRecentTransactions());
    }

    @Test
    void shouldReturnFalseWhenRemovingMissingTransaction() {
        final var service = new DefaultMovementHistoryService(List.of("Coffee -5"));
        final boolean removed = service.removeTransaction("Book -20");
        assertFalse(removed);
        assertEquals(List.of("Coffee -5"), service.getRecentTransactions());
    }

    @Test
    void shouldNotifyObserversWhenHistoryChanges() {
        final var service = new DefaultMovementHistoryService();
        final var updates = new AtomicInteger();
        final TransactionObserver observer = updates::incrementAndGet;
        service.addObserver(observer);
        service.addTransaction("Coffee -5");
        service.removeTransaction("Coffee -5");
        assertEquals(2, updates.get());
    }
}