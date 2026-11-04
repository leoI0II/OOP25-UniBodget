package it.unibo.unibodget.persistency;

import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.MessageEvent;
import it.unibo.unibodget.model.utils.event.TransactionAddedEvent;
import it.unibo.unibodget.model.utils.event.TransactionRemovedEvent;
import it.unibo.unibodget.model.utils.event.TransactionReplacedEvent;
import it.unibo.unibodget.model.utils.event.WalletAddedEvent;
import it.unibo.unibodget.model.utils.event.WalletDeletedEvent;
import it.unibo.unibodget.model.utils.event.WalletRenamedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists the wallets as soon as they change, so that no operation is lost if the application
 * terminates abnormally.
 *
 * <p>Each wallet kind is registered once, as a pair "where the wallets are read from" / "how they
 * are saved"; the saver does not need to know which kinds exist. After {@link #start()} it listens
 * on the {@link MessageBus} to every event that changes wallet data and saves all registered kinds.
 *
 * <pre>{@code
 * final WalletAutoSaver saver = new WalletAutoSaver()
 *         .register(cashService::getWallets, cashManager::saveAll)
 *         .register(investmentService::getWallets, investmentManager::saveAll);
 * saver.start();
 * }</pre>
 */
public final class WalletAutoSaver {

    private static final Logger LOGGER = Logger.getLogger(WalletAutoSaver.class.getName());

    private static final List<Class<? extends MessageEvent>> SAVE_TRIGGERS =
            List.of(
                    WalletAddedEvent.class,
                    WalletRenamedEvent.class,
                    WalletDeletedEvent.class,
                    TransactionAddedEvent.class,
                    TransactionRemovedEvent.class,
                    TransactionReplacedEvent.class);

    private final List<Runnable> saveTasks = new ArrayList<>();
    private final List<Consumer<?>> subscriptions = new ArrayList<>();

    /**
     * Registers a kind of wallet to be saved.
     *
     * @param <W> the wallet type
     * @param wallets supplies the current wallets of this kind
     * @param saver persists the whole list of wallets of this kind
     * @return this saver, for chaining
     */
    public <W> WalletAutoSaver register(
            final Supplier<List<W>> wallets, final Consumer<List<W>> saver) {
        Objects.requireNonNull(wallets);
        Objects.requireNonNull(saver);
        saveTasks.add(() -> saver.accept(wallets.get()));
        return this;
    }

    /**
     * Starts saving on every wallet change. Call it once the wallets have been loaded, otherwise
     * loading them would trigger useless saves.
     */
    public void start() {
        if (!subscriptions.isEmpty()) {
            return;
        }
        for (final Class<? extends MessageEvent> trigger : SAVE_TRIGGERS) {
            subscriptions.add(MessageBus.subscribe(trigger, event -> saveOnChange()));
        }
    }

    /** Stops listening to wallet changes. Wallets can still be saved with {@link #saveAll()}. */
    public void stop() {
        MessageBus.unsubscribeAll(subscriptions);
        subscriptions.clear();
    }

    /**
     * Saves every registered kind of wallet. Each kind is saved independently, so a failure on one
     * does not prevent saving the others.
     *
     * @throws IllegalStateException if at least one kind could not be saved; the individual
     *     failures are attached as suppressed
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void saveAll() {
        final List<RuntimeException> failures = new ArrayList<>();
        for (final Runnable task : saveTasks) {
            // CHECKSTYLE: IllegalCatch OFF
            try {
                task.run();
            } catch (final RuntimeException e) {
                // CHECKSTYLE: IllegalCatch ON
                LOGGER.log(Level.SEVERE, "Wallet saving failed", e);
                failures.add(e);
            }
        }
        if (!failures.isEmpty()) {
            final IllegalStateException failure =
                    new IllegalStateException(
                            "Unable to save wallets: " + failures.getFirst().getMessage());
            failures.forEach(failure::addSuppressed);
            throw failure;
        }
        LOGGER.fine("Wallets saved");
    }

    private void saveOnChange() {
        try {
            saveAll();
        } catch (final IllegalStateException e) {
            MessageBus.send(new MainErrorNotificationEvent(e.getMessage()));
        }
    }
}
