package it.unibo.unibodget.persistency;

import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.ClearAllDataRequestedEvent;
import it.unibo.unibodget.model.utils.event.LoadDemoDataRequestedEvent;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.MainInfoNotificationEvent;
import it.unibo.unibodget.model.utils.event.WalletsReplacedEvent;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.CashAccountManager;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccountManager;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces all the wallets with the demo data inside the jar, or deletes all of them, when the user
 * asks it from the settings ({@link LoadDemoDataRequestedEvent}, {@link ClearAllDataRequestedEvent}).
 *
 * <p>The demo is in {@code /demo/}: {@code CashAccounts.json} and {@code InvestmentAccounts.json},
 * with the same format of the saved files, and the P/L snapshots of each investment account in
 * {@code /demo/snapshots/<accountId>_snapshots.csv}, copied as they are in the snapshot folder.
 *
 * <p>The wallets are changed only through the services: the usual events are published, so
 * {@link WalletAutoSaver} saves the new wallets and the snapshots of the removed ones are deleted.
 * At the end a {@link WalletsReplacedEvent} tells the pages to reload.
 */
public final class DemoDataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataLoader.class);
    private static final String DEMO_DIR = "/demo/";
    private static final String CASH_RESOURCE = DEMO_DIR + "CashAccounts.json";
    private static final String INVESTMENT_RESOURCE = DEMO_DIR + "InvestmentAccounts.json";
    private static final String SNAPSHOTS_RESOURCE_DIR = DEMO_DIR + "snapshots/";
    private static final String SNAPSHOT_SUFFIX = "_snapshots.csv";

    private final CashAccountService cashService;
    private final InvestmentAccountService investmentService;
    private final CashAccountManager cashManager;
    private final InvestmentAccountManager investmentManager;
    private final Path snapshotsDir;
    private final List<Consumer<?>> subscriptions = new ArrayList<>();

    /**
     * Creates the loader.
     *
     * @param cashService       the service of the cash accounts
     * @param investmentService the service of the investment accounts
     * @param cashManager       used only to read the demo cash accounts
     * @param investmentManager used only to read the demo investment accounts
     * @param snapshotsDir      the folder of the P/L snapshots (the same of the snapshot service)
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of stateful services")
    public DemoDataLoader(
            final CashAccountService cashService,
            final InvestmentAccountService investmentService,
            final CashAccountManager cashManager,
            final InvestmentAccountManager investmentManager,
            final Path snapshotsDir) {
        this.cashService = Objects.requireNonNull(cashService);
        this.investmentService = Objects.requireNonNull(investmentService);
        this.cashManager = Objects.requireNonNull(cashManager);
        this.investmentManager = Objects.requireNonNull(investmentManager);
        this.snapshotsDir = Objects.requireNonNull(snapshotsDir);
    }

    /** Starts listening to the requests coming from the settings. */
    public void start() {
        subscriptions.add(MessageBus.subscribe(LoadDemoDataRequestedEvent.class, event -> loadDemo()));
        subscriptions.add(MessageBus.subscribe(ClearAllDataRequestedEvent.class, event -> clearAll()));
    }

    /** Stops listening to the requests. */
    public void stop() {
        MessageBus.unsubscribeAll(subscriptions);
        subscriptions.clear();
    }

    /**
     * Replaces all the wallets with the demo ones. If the demo cannot be read nothing is changed and
     * an error is shown.
     *
     * @return {@code true} if the demo has been loaded
     */
    public boolean loadDemo() {
        final List<CashAccount> demoCash;
        final List<InvestmentAccount> demoInvestments;
        try {
            demoCash = cashManager.fromJson(readJson(CASH_RESOURCE));
            demoInvestments = investmentManager.fromJson(readJson(INVESTMENT_RESOURCE));
        } catch (final IOException e) {
            LOGGER.error("Unable to read the demo data", e);
            MessageBus.send(new MainErrorNotificationEvent("Unable to load the demo data."));
            return false;
        }

        // first the old wallets go away (their snapshots are deleted by the snapshot service), then
        // the demo snapshots are copied, and only at the end the demo wallets are added
        removeAllWallets();
        try {
            copySnapshots(demoInvestments);
        } catch (final IOException e) {
            LOGGER.error("Unable to write the demo snapshots", e);
            MessageBus.send(new MainErrorNotificationEvent("The demo P/L chart could not be saved."));
        }
        demoCash.forEach(cashService::addWallet);
        demoInvestments.forEach(investmentService::addWallet);

        MessageBus.send(new WalletsReplacedEvent());
        MessageBus.send(new MainInfoNotificationEvent("Demo data loaded."));
        return true;
    }

    /** Deletes all the wallets (cash and investment) and their P/L snapshots. */
    public void clearAll() {
        removeAllWallets();
        MessageBus.send(new WalletsReplacedEvent());
        MessageBus.send(new MainInfoNotificationEvent("All data cleared."));
    }

    /**
     * Removes every wallet through its service, so the delete events are published.
     */
    private void removeAllWallets() {
        // copy of the ids: the list of the service changes while removing
        cashService.getWallets().stream().map(CashAccount::getId).toList()
                .forEach(cashService::removeWallet);
        investmentService.getWallets().stream().map(InvestmentAccount::getId).toList()
                .forEach(investmentService::removeWallet);
    }

    /**
     * Reads a JSON file of the demo from the jar.
     *
     * @param resource the path of the file in the jar
     * @return the JSON tree
     * @throws IOException if the file is missing or not valid
     */
    private static JsonNode readJson(final String resource) throws IOException {
        try (InputStream in = DemoDataLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing demo resource " + resource);
            }
            return PersistenceJacksonConfig.mapper().readTree(in);
        }
    }

    /**
     * Copies the demo snapshots of the given accounts (if the jar has them) in the snapshot folder.
     *
     * @param accounts the demo investment accounts
     * @throws IOException if a file cannot be copied
     */
    private void copySnapshots(final List<InvestmentAccount> accounts) throws IOException {
        Files.createDirectories(snapshotsDir);
        for (final InvestmentAccount account : accounts) {
            final String fileName = account.getId() + SNAPSHOT_SUFFIX;
            try (InputStream in = DemoDataLoader.class.getResourceAsStream(SNAPSHOTS_RESOURCE_DIR + fileName)) {
                if (in != null) {
                    Files.copy(in, snapshotsDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
