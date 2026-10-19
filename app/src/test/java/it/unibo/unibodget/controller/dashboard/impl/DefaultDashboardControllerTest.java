package it.unibo.unibodget.controller.dashboard.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetMonitor;
import it.unibo.unibodget.model.dashboard.impl.DefaultCategoryService;
import it.unibo.unibodget.model.dashboard.impl.DefaultDashboardFacade;
import it.unibo.unibodget.model.dashboard.impl.DefaultFriendLoanSummaryService;
import it.unibo.unibodget.model.dashboard.impl.DefaultWalletInsightService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.CashTransactionFactory;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.view.dashboard.api.DashboardView;
import it.unibo.unibodget.view.dashboard.api.DashboardViewActions;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState;

/**
 * Tests for {@link DefaultDashboardController}, focusing on CSV export functionality.
 */
class DefaultDashboardControllerTest {

    private static final String WALLET_NAME = "Main Wallet";
    private static final String GROCERIES = "Groceries";
    private static final String SALARY = "Salary";

    private CashAccountService cashAccountService;
    private DefaultDashboardController controller;

    @BeforeEach
    void setUp() {
        cashAccountService = new CashAccountService();
        final DefaultCategoryService categoryService = new DefaultCategoryService();
        final DefaultBudgetMonitor budgetMonitor = new DefaultBudgetMonitor();
        final DefaultFriendLoanSummaryService friendLoanSummaryService = new DefaultFriendLoanSummaryService();
        final DefaultWalletInsightService walletInsightService = new DefaultWalletInsightService();
        final CategoryCatalog categoryCatalog = new CategoryCatalog();
        final CurrencyConverter converter = (amount, from, to) ->
                new CurrencyConversionResult(amount, from, to, BigDecimal.ONE, amount);
        final Settings settings = new Settings();

        final DefaultDashboardFacade facade = new DefaultDashboardFacade(
                cashAccountService,
                categoryService,
                budgetMonitor,
                friendLoanSummaryService,
                walletInsightService,
                categoryCatalog,
                converter,
                settings
        );

        final DashboardView dummyView = new DashboardView() {
            @Override
            public void bindActions(final DashboardViewActions actions) {
            }

            @Override
            public void render(final DashboardViewState state) {
            }

            @Override
            public void showError(final String message) {
            }
        };

        controller = new DefaultDashboardController(
                dummyView,
                facade,
                cashAccountService,
                settings,
                new CashTransactionFactory()
        );
    }

    @Test
    void shouldExportTransactionsToCsvSuccessfully(@TempDir final File tempDir) throws IOException {
        final CashAccount wallet = new CashAccount(WALLET_NAME, FiatCurrency.EUR);
        cashAccountService.addWallet(wallet);

        final Category foodCategory = new Category(
                GROCERIES,
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        final Category salaryCategory = new Category(
                SALARY,
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );

        final CashTransaction tx1 = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("1500.00")))
                .category(salaryCategory)
                .date(LocalDate.now())
                .description("Monthly Paycheck")
                .notes("October income")
                .build();

        final CashTransaction tx2 = CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-45.50")))
                .category(foodCategory)
                .date(LocalDate.now().minusDays(1))
                .description("Supermarket trip")
                .notes("Weekly groceries")
                .build();

        cashAccountService.addTransaction(tx1);
        cashAccountService.addTransaction(tx2);

        final File exportFile = new File(tempDir, "exported_transactions.csv");
        final boolean success = controller.exportTransactionsToCsv(exportFile, wallet);

        assertTrue(success, "Export should return true on success");
        assertTrue(exportFile.exists(), "Export file should exist");
        assertTrue(exportFile.length() > 0, "Export file should not be empty");

        final List<String> lines = readLines(exportFile);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        // Header section check (semicolon separated as defined in CsvExportFormat)
        assertEquals("Wallet;Currency;Balance;Export Date;Total Rows", lines.get(0));
        assertTrue(lines.get(1).startsWith(WALLET_NAME + ";EUR;"));
        assertTrue(lines.get(1).endsWith(";2"));

        // Table header check
        final int tableHeaderIndex = findLineStartingWith(lines, "Date;Description;Category;Type;Amount;Currency;Notes");
        assertTrue(tableHeaderIndex >= 0, "Transactions table header should be present");

        // Transaction rows check
        final String allRemainingContent = String.join("\n", lines.subList(tableHeaderIndex + 1, lines.size()));
        assertTrue(allRemainingContent.contains("Monthly Paycheck"));
        assertTrue(allRemainingContent.contains(SALARY));
        assertTrue(allRemainingContent.contains("1500.00"));
        assertTrue(allRemainingContent.contains("Supermarket trip"));
        assertTrue(allRemainingContent.contains(GROCERIES));
        assertTrue(allRemainingContent.contains("-45.50"));
    }

    @Test
    void shouldThrowWhenExportArgumentsAreNull(@TempDir final File tempDir) {
        final CashAccount wallet = new CashAccount(WALLET_NAME, FiatCurrency.EUR);
        final File exportFile = new File(tempDir, "test.csv");

        assertThrows(NullPointerException.class, () -> controller.exportTransactionsToCsv(null, wallet));
        assertThrows(NullPointerException.class, () -> controller.exportTransactionsToCsv(exportFile, null));
    }

    private static List<String> readLines(final File file) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static int findLineStartingWith(final List<String> lines, final String prefix) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }
}
