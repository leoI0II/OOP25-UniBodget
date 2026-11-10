package it.unibo.unibodget.model.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.persistency.AppDataPathResolver;

class WalletTest {

    private static final Path DATA_FILE = AppDataPathResolver.resolveJson("wallet/CashAccounts.json");
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
    void shouldAddTransaction() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        final CashTransaction tx = CashTransaction.builder()
                .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.TEN))
                .category(Category.FOOD)
                .date(LocalDate.now())
                .description("desc")
                .notes("notes")
                .build();

        acc.addTransaction(tx);

        assertTrue(acc.getHistory().getTransactions().contains(tx));
    }

    @Test
    void shouldRejectZeroAmountTransaction() {
        final CashAccount acc = new CashAccount("MyCash", FiatCurrency.EUR);

        final CashTransaction zeroTx = CashTransaction.builder()
                .asset(new Asset(FiatCurrency.EUR, java.math.BigDecimal.ZERO))
                .category(Category.FOOD)
                .date(LocalDate.now())
                .description("desc")
                .notes("notes")
                .build();

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
        assertEquals(account.getId(), reloaded.getFirst().getId());
    }

}
