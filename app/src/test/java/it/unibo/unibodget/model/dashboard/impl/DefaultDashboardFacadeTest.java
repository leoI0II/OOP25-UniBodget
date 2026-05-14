package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests for {@link DefaultDashboardFacade}.
 */
class DefaultDashboardFacadeTest {

    private CashAccountService walletService;
    private DefaultCategoryService categoryService;
    private DefaultBudgetMonitor budgetMonitor;
    private DefaultFriendLoanSummaryService friendLoanSummaryService;
    private DefaultWalletInsightService walletInsightService;
    private DefaultDashboardFacade dashboardFacade;

    @BeforeEach
    void setUp() {
        walletService = new CashAccountService();
        categoryService = new DefaultCategoryService();
        budgetMonitor = new DefaultBudgetMonitor();
        friendLoanSummaryService = new DefaultFriendLoanSummaryService();
        walletInsightService = new DefaultWalletInsightService();
        dashboardFacade = new DefaultDashboardFacade(
                walletService,
                categoryService,
                budgetMonitor,
                friendLoanSummaryService,
                walletInsightService
        );
    }

    @Test
    void shouldLoadDashboardSnapshotForCurrentWallet() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        wallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal("1000.00"), new BigDecimal("0.80"))
        );
        walletService.addWallet(wallet);

        final Category salary = new Category(
                "Salary",
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );
        final Category food = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("2000.00")),
                salary,
                LocalDate.now(),
                "Salary",
                "Monthly salary"
        ));
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-300.00")),
                food,
                LocalDate.now(),
                "Groceries",
                "Food expenses"
        ));

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals("Main wallet", snapshot.getWalletName());
        assertEquals(FiatCurrency.EUR.toString(), snapshot.getWalletCurrency());
        assertEquals(0, new BigDecimal("1700.00").compareTo(snapshot.getTotalBalance()));
        assertEquals(2, snapshot.getRecentTransactions().size());
        assertEquals(0, new BigDecimal("2000.00").compareTo(snapshot.getCategorySummaries().get("Salary")));
        assertEquals(0, new BigDecimal("300.00").compareTo(snapshot.getCategorySummaries().get("Food")));
        assertEquals(0, new BigDecimal("1000.00").compareTo(snapshot.getBudgetLimit()));
        assertEquals(0, new BigDecimal("0.80").compareTo(snapshot.getWarningThreshold()));
        assertEquals(BudgetStatus.SAFE, snapshot.getBudgetStatus());
        assertEquals(0, snapshot.getFriendLoanSummaries().size());
        assertEquals(3, snapshot.getWalletInsights().size());
    }

    @Test
    void shouldLoadDashboardForSelectedWalletOnly() {
        final CashAccount firstWallet = new CashAccount("First wallet", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount("Second wallet", FiatCurrency.USD);

        firstWallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal("1000.00"), new BigDecimal("0.80"))
        );
        secondWallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal("500.00"), new BigDecimal("0.90"))
        );

        walletService.addWallet(firstWallet);
        walletService.addWallet(secondWallet);

        final Category firstCategory = new Category(
                "Food",
                new ARGBColor(0xFFFF9800),
                CategoryType.EXPENSE
        );
        final Category secondCategory = new Category(
                "Travel",
                new ARGBColor(0xFF2196F3),
                CategoryType.EXPENSE
        );

        walletService.selectWallet(firstWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                firstCategory,
                LocalDate.now(),
                "Groceries",
                "Food expenses"
        ));

        walletService.selectWallet(secondWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.USD, new BigDecimal("-50.00")),
                secondCategory,
                LocalDate.now(),
                "Taxi",
                "Taxi expense"
        ));

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
    }

    @Test
    void shouldIncludeFriendLoanSummariesInSnapshot() {
        final CashAccount wallet = new CashAccount("Main wallet", FiatCurrency.EUR);
        wallet.setBudgetSettings(
                new DefaultBudgetSettings(new BigDecimal("1000.00"), new BigDecimal("0.80"))
        );
        walletService.addWallet(wallet);

        final Category friendLoanCategory = new Category(
                "Friend loan",
                new ARGBColor(0xFF9C27B0),
                CategoryType.FRIEND_LOAN
        );

        final UUID marcoLoanId = UUID.randomUUID();

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                friendLoanCategory,
                LocalDate.now(),
                "Loan to Marco",
                "Initial loan",
                marcoLoanId,
                "Marco"
        ));

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("40.00")),
                friendLoanCategory,
                LocalDate.now(),
                "Marco repayment",
                "Partial repayment",
                marcoLoanId,
                "Marco"
        ));

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals(1, snapshot.getFriendLoanSummaries().size());
        assertEquals("Marco", snapshot.getFriendLoanSummaries().get(0).getFriendName());
        assertEquals(0, new BigDecimal("100.00").compareTo(snapshot.getFriendLoanSummaries().get(0).getTotalGiven()));
        assertEquals(0, new BigDecimal("40.00").compareTo(snapshot.getFriendLoanSummaries().get(0).getTotalReceived()));
        assertEquals(0, new BigDecimal("60.00").compareTo(snapshot.getFriendLoanSummaries().get(0).getNetBalance()));
        assertEquals(3, snapshot.getWalletInsights().size());
    }
}