package it.unibo.unibodget.persistency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.ClearAllDataRequestedEvent;
import it.unibo.unibodget.model.utils.event.LoadDemoDataRequestedEvent;
import it.unibo.unibodget.model.utils.event.WalletsReplacedEvent;
import it.unibo.unibodget.model.wallet.AbstractWallet;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.CashAccountManager;
import it.unibo.unibodget.model.wallet.InvestmentAccountManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks that the demo data inside the jar replaces all the wallets, that its P/L snapshots are
 * copied and that all the data can be cleared. The snapshots are written in a temporary folder.
 */
class DemoDataLoaderTest {

    // empty demo accounts are not used: the prices are never asked
    private static final CurrencyConverter NO_CONVERSION = (amount, from, to) -> null;
    // the last snapshot saved in the demo csv
    private static final LocalDateTime LAST_SNAPSHOT = LocalDateTime.parse("2026-11-04T22:35:00.375180288");
    private static final String BINANCE_ID = "61d2439c-e52b-4740-8cbe-a59e4f306de8";
    private static final int DEMO_INVESTMENT_TRANSACTIONS = 10;
    private static final int DEMO_SNAPSHOTS = 79;

    @TempDir
    private Path snapshotsDir;

    private CashAccountService cashService;
    private InvestmentAccountService investmentService;
    private DemoDataLoader loader;
    private final List<WalletsReplacedEvent> replacedEvents = new ArrayList<>();
    private Consumer<WalletsReplacedEvent> replacedToken;

    @BeforeEach
    void setUp() {
        cashService = new CashAccountService();
        investmentService = new InvestmentAccountService();
        loader = new DemoDataLoader(
                cashService,
                investmentService,
                new CashAccountManager(),
                new InvestmentAccountManager(NO_CONVERSION),
                snapshotsDir);
        replacedToken = MessageBus.subscribe(WalletsReplacedEvent.class, replacedEvents::add);
    }

    @AfterEach
    void tearDown() {
        loader.stop();
        MessageBus.unsubscribe(WalletsReplacedEvent.class, replacedToken);
    }

    @Test
    void loadDemoReplacesAllTheWallets() {
        cashService.addWallet(new CashAccount("My old wallet", FiatCurrency.EUR));

        assertTrue(loader.loadDemo());

        assertEquals(List.of("Main account", "Holidays"), names(cashService.getWallets()));
        assertEquals(List.of("Binance"), names(investmentService.getWallets()));
        assertEquals(DEMO_INVESTMENT_TRANSACTIONS,
                investmentService.getWallets().getFirst().getHistory().getTransactions().size());
        assertEquals(1, replacedEvents.size());
    }

    @Test
    void loadDemoCopiesTheSnapshots() throws IOException {
        loader.loadDemo();

        final List<String> lines = Files.readAllLines(snapshotsDir.resolve(BINANCE_ID + "_snapshots.csv"));
        assertEquals(DEMO_SNAPSHOTS, lines.size());
        final LocalDateTime last = LocalDateTime.parse(lines.getLast().substring(0, lines.getLast().indexOf(',')));
        assertEquals(LAST_SNAPSHOT, last);
    }

    @Test
    void clearAllRemovesEveryWallet() {
        loader.loadDemo();

        loader.clearAll();

        assertTrue(cashService.getWallets().isEmpty());
        assertTrue(investmentService.getWallets().isEmpty());
        assertEquals(2, replacedEvents.size());
    }

    @Test
    void requestsFromTheSettingsAreHandledOnlyAfterStart() {
        MessageBus.send(new LoadDemoDataRequestedEvent());
        assertTrue(investmentService.getWallets().isEmpty());

        loader.start();
        MessageBus.send(new LoadDemoDataRequestedEvent());
        assertFalse(investmentService.getWallets().isEmpty());

        MessageBus.send(new ClearAllDataRequestedEvent());
        assertTrue(investmentService.getWallets().isEmpty());
    }

    private static List<String> names(final List<? extends AbstractWallet<?>> wallets) {
        return wallets.stream().map(AbstractWallet::getName).toList();
    }
}
