package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests for {@link DefaultDashboardFacade}.
 */
class DefaultDashboardFacadeTest {

    private static final String MAIN_WALLET = "Main wallet";
    private static final String S_1000 = "1000.00";
    private static final String S_0_80 = "0.80";
    private static final String SALARY = "Salary";
    private static final String FOOD = "Food";
    private static final String MARCO_P = "Marco";

    private CashAccountService walletService;
    private DefaultDashboardFacade dashboardFacade;

    @BeforeEach
    void setUp() {
        walletService = new CashAccountService();
        final DefaultCategoryService categoryService = new DefaultCategoryService();
        final DefaultBudgetMonitor budgetMonitor = new DefaultBudgetMonitor();
        final DefaultFriendLoanSummaryService friendLoanSummaryService = new DefaultFriendLoanSummaryService();
        final DefaultWalletInsightService walletInsightService = new DefaultWalletInsightService();
        final CategoryCatalog categoryCatalog = new CategoryCatalog();
        final CurrencyConverter converter = (amount, from, to) ->
                new CurrencyConversionResult(amount, from, to, BigDecimal.ONE, amount);
        final Settings settings = new Settings();

        dashboardFacade = new DefaultDashboardFacade(
                walletService,
                categoryService,
                budgetMonitor,
                friendLoanSummaryService,
                walletInsightService,
                categoryCatalog,
                converter,
                settings
        );
    }

    @Test
    void shouldLoadDashboardSnapshotForCurrentWallet() {
        final CashAccount wallet = new CashAccount(MAIN_WALLET, FiatCurrency.EUR);
        wallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80))
        );
        walletService.addWallet(wallet);

        final Category salary = new Category(
                SALARY,
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );
        final Category food = new Category(
                FOOD,
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        walletService.addTransaction(CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("2000.00")))
                .category(salary)
                .date(LocalDate.now())
                .description(SALARY)
                .notes("Monthly salary")
                .build());
        walletService.addTransaction(CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-300.00")))
                .category(food)
                .date(LocalDate.now())
                .description("Groceries")
                .notes("Food expenses")
                .build());

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals(MAIN_WALLET, snapshot.getWalletName());
        assertEquals(FiatCurrency.EUR.toString(), snapshot.getWalletCurrency());
        assertEquals(0, new BigDecimal("1700.00").compareTo(snapshot.getTotalBalance()));
        assertEquals(2, snapshot.getRecentTransactions().size());
        assertEquals(0, new BigDecimal("2000.00").compareTo(snapshot.getCategorySummaries().get(SALARY)));
        assertEquals(0, new BigDecimal("300.00").compareTo(snapshot.getCategorySummaries().get(FOOD)));
        assertEquals(0, new BigDecimal(S_1000).compareTo(snapshot.getBudgetLimit()));
        assertEquals(0, new BigDecimal(S_0_80).compareTo(snapshot.getWarningThreshold()));
        assertEquals(BudgetStatus.SAFE, snapshot.getBudgetStatus());
        assertEquals(0, snapshot.getFriendLoanSummaries().size());
        assertEquals(3, snapshot.getWalletInsights().size());
        assertEquals(1, snapshot.getAllWallets().size());
        assertEquals("Main wallet", snapshot.getAllWallets().get(0).getName());
    }

    @Test
    void shouldLoadDashboardForSelectedWalletOnly() {
        final CashAccount firstWallet = new CashAccount("First wallet", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount("Second wallet", FiatCurrency.USD);

        firstWallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80))
        );
        secondWallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal("500.00"), new BigDecimal("0.90"))
        );

        walletService.addWallet(firstWallet);
        walletService.addWallet(secondWallet);

        final Category firstCategory = new Category(
                FOOD,
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        final Category secondCategory = new Category(
                "Travel",
                new ARGBColor(0xFF2196F3),
                CategoryType.EXPENSE
        );

        walletService.selectWallet(firstWallet.getId());
        walletService.addTransaction(CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")))
                .category(firstCategory)
                .date(LocalDate.now())
                .description("Groceries")
                .notes("Food expenses")
                .build());

        walletService.selectWallet(secondWallet.getId());
        walletService.addTransaction(CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.USD, new BigDecimal("-50.00")))
                .category(secondCategory)
                .date(LocalDate.now())
                .description("Taxi")
                .notes("Taxi expense")
                .build());

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals("Second wallet", snapshot.getWalletName());
        assertEquals(FiatCurrency.USD.toString(), snapshot.getWalletCurrency());
        assertEquals(0, new BigDecimal("-50.00").compareTo(snapshot.getTotalBalance()));
        assertEquals(1, snapshot.getRecentTransactions().size());

        final Map<String, BigDecimal> categorySummaries = snapshot.getCategorySummaries();
        assertEquals(0, new BigDecimal("50.00").compareTo(categorySummaries.get("Travel")));
        assertEquals(1, categorySummaries.size());

        assertEquals(0, new BigDecimal("500.00").compareTo(snapshot.getBudgetLimit()));
        assertEquals(0, new BigDecimal("0.90").compareTo(snapshot.getWarningThreshold()));
        assertEquals(BudgetStatus.SAFE, snapshot.getBudgetStatus());
        assertEquals(0, snapshot.getFriendLoanSummaries().size());
        assertEquals(3, snapshot.getWalletInsights().size());
        assertEquals(2, snapshot.getAllWallets().size());
    }

    @Test
    void shouldIncludeFriendLoanSummariesInSnapshot() {
        final CashAccount wallet = new CashAccount(MAIN_WALLET, FiatCurrency.EUR);
        wallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80))
        );
        walletService.addWallet(wallet);

        final Category friendLoanCategory = new Category(
                "Friend loan",
                new ARGBColor(0xFF9C27B0),
                CategoryType.FRIEND_LOAN
        );

        final UUID marcoLoanId = UUID.randomUUID();

        walletService.addTransaction(CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")))
                .category(friendLoanCategory)
                .date(LocalDate.now())
                .description("Loan to " + MARCO_P)
                .notes("Initial loan")
                .loanId(marcoLoanId)
                .friendName(MARCO_P)
                .build());

        walletService.addTransaction(CashTransaction.builder()
                .asset(Asset.of(FiatCurrency.EUR, new BigDecimal("40.00")))
                .category(friendLoanCategory)
                .date(LocalDate.now())
                .description(MARCO_P + " repayment")
                .notes("Partial repayment")
                .loanId(marcoLoanId)
                .friendName(MARCO_P)
                .build());

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals(1, snapshot.getFriendLoanSummaries().size());
        assertEquals(MARCO_P, snapshot.getFriendLoanSummaries().get(0).getFriendName());
        assertEquals(0, new BigDecimal("100.00").compareTo(snapshot.getFriendLoanSummaries().get(0).getTotalGiven()));
        assertEquals(0, new BigDecimal("40.00").compareTo(snapshot.getFriendLoanSummaries().get(0).getTotalReceived()));
        assertEquals(0, new BigDecimal("60.00").compareTo(snapshot.getFriendLoanSummaries().get(0).getNetBalance()));
        assertEquals(3, snapshot.getWalletInsights().size());
        assertEquals(1, snapshot.getAllWallets().size());
    }
}
