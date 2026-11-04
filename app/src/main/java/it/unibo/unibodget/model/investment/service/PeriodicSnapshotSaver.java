package it.unibo.unibodget.model.investment.service;

import it.unibo.unibodget.model.investment.BalanceSnapshot;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.TickEvent;
import it.unibo.unibodget.model.utils.event.TransactionAddedEvent;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves a balance snapshot of every investment account at a fixed interval while the app is open,
 * so the P/L history grows also without new transactions.
 *
 * <p>It listens to the {@link TickEvent} of the app ticker: at every tick, for each account, it
 * saves a snapshot only if the last one is older than the interval. The time of the last snapshot
 * is read once from the {@link InvestmentsSnapshotService} and then kept in memory; the snapshots
 * saved after a new transaction count too.
 */
public final class PeriodicSnapshotSaver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicSnapshotSaver.class);

    private final Supplier<List<InvestmentAccount>> accounts;
    private final InvestmentsSnapshotService snapshotService;
    private final Duration interval;
    private final Map<UUID, LocalDateTime> lastSaved = new HashMap<>();
    private final List<Consumer<?>> subscriptions = new ArrayList<>();

    /**
     * Creates the saver, it does nothing until {@link #start()}.
     *
     * @param accounts supplies the current investment accounts
     * @param snapshotService where the snapshots are saved and read
     * @param interval minimum time between two snapshots of the same account
     */
    public PeriodicSnapshotSaver(
            final Supplier<List<InvestmentAccount>> accounts,
            final InvestmentsSnapshotService snapshotService,
            final Duration interval) {
        this.accounts = Objects.requireNonNull(accounts);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.interval = Objects.requireNonNull(interval);
    }

    /** Starts listening to the ticks and to the new transactions. */
    public void start() {
        subscriptions.add(MessageBus.subscribe(TickEvent.class, event -> onTick(event.time())));
        // the snapshot service already saves after each transaction, so that is the last snapshot
        subscriptions.add(
                MessageBus.subscribe(
                        TransactionAddedEvent.class,
                        event -> {
                            if (event.wallet() instanceof InvestmentAccount account) {
                                lastSaved.put(account.getId(), LocalDateTime.now());
                            }
                        }));
    }

    /** Stops listening. */
    public void stop() {
        MessageBus.unsubscribeAll(subscriptions);
        subscriptions.clear();
    }

    /**
     * Saves a snapshot of every account whose last snapshot is older than the interval.
     *
     * @param now the time of the tick
     */
    void onTick(final LocalDateTime now) {
        for (final InvestmentAccount account : accounts.get()) {
            if (isDue(account.getId(), now)) {
                save(account, now);
            }
        }
    }

    private boolean isDue(final UUID accountId, final LocalDateTime now) {
        final LocalDateTime last = lastSaved.computeIfAbsent(accountId, this::lastSnapshotTime);
        // no snapshot yet, or the last one is at least "interval" old
        return last == null || !now.isBefore(last.plus(interval));
    }

    private LocalDateTime lastSnapshotTime(final UUID accountId) {
        final List<BalanceSnapshot> snapshots = snapshotService.getSnapshots(accountId);
        return snapshots.isEmpty() ? null : snapshots.getLast().timestamp();
    }

    private void save(final InvestmentAccount account, final LocalDateTime now) {
        try {
            snapshotService.save(account.getId(), BalanceSnapshot.buildInvestmentSnapshot(account));
            lastSaved.put(account.getId(), now);
        } catch (final IllegalStateException | IllegalArgumentException e) {
            // e.g. a price that is not available: it will be tried again at the next tick
            LOGGER.warn("Snapshot of {} not saved: {}", account.getName(), e.getMessage());
        }
    }
}
