package it.unibo.unibodget.model.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.persistency.AppDataPathResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks that an investment account keeps its id after being saved and reloaded, since the balance
 * snapshots are stored per account id.
 */
class InvestmentAccountPersistenceTest {

    // Same file used by InvestmentAccountManager: it is backed up and restored around each test.
    private static final Path DATA_FILE = AppDataPathResolver.resolveJson("wallet/InvestmentAccounts.json");
    private static final CurrencyConverter NO_CONVERSION = (amount, from, to) -> null;

    private byte[] backup;

    @BeforeEach
    void backupDataFile() throws IOException {
        backup = Files.exists(DATA_FILE) ? Files.readAllBytes(DATA_FILE) : null;
    }

    @AfterEach
    void restoreDataFile() throws IOException {
        if (backup == null) {
            Files.deleteIfExists(DATA_FILE);
        } else {
            Files.write(DATA_FILE, backup);
        }
    }

    @Test
    void shouldKeepIdAfterSaveAndReload() {
        final InvestmentAccountManager manager = new InvestmentAccountManager(NO_CONVERSION);
        final InvestmentAccount account =
                new InvestmentAccount("Broker", FiatCurrency.EUR, NO_CONVERSION);

        manager.saveAll(List.of(account));
        final List<InvestmentAccount> reloaded = manager.loadAll();

        assertEquals(1, reloaded.size());
        assertEquals(account.getId(), reloaded.getFirst().getId());
    }

    @Test
    void withProviderShouldKeepId() {
        final InvestmentAccount account =
                new InvestmentAccount("Broker", FiatCurrency.EUR, NO_CONVERSION);

        assertEquals(account.getId(), account.withProvider(NO_CONVERSION).getId());
    }
}
