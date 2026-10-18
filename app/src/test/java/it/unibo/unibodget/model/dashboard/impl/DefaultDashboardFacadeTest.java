package it.unibo.unibodget.model.dashboard.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

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
 * Tests dashboard snapshots for the currently selected cash wallet.
 */
final class DefaultDashboardFacadeTest {

    private static final String MAIN_WALLET = "Main wallet";
    private static final String SECOND_WALLET = "Second wallet";
    private static final String SALARY = "Salary";
    private static final String FOOD = "Food";
    private static final String TRAVEL = "Travel";
    private static final String FRIEND_NAME = "Marco";

    private static final BigDecimal BUDGET_LIMIT = new BigDecimal("1000.00");
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.80");
    private static final BigDecimal SECOND_BUDGET_LIMIT = new BigDecimal("500.00");
    private static final BigDecimal SECOND_WARNING_THRESHOLD = new BigDecimal("0.90");
    private static final BigDecimal SALARY_AMOUNT = new BigDecimal("2000.00");
    private static final BigDecimal FOOD_EXPENSE = new BigDecimal("300.00");
    private static final BigDecimal FIRST_EXPENSE = new BigDecimal("100.00");
    private static final BigDecimal TRAVEL_EXPENSE = new BigDecimal("50.00");
    private static final BigDecimal LOAN_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal REPAYMENT_AMOUNT = new BigDecimal("40.00");

    private static final int EXPECTED_INSIGHT_COUNT = 3;
    private static final int TWO_ITEMS = 2;

    private CashAccountService walletService;
    private DefaultDashboardFacade dashboardFacade;

    /**
     * Initializes the facade and its collaborating services.
     */
    @BeforeEach
    void setUp() {
        walletService = new CashAccountService();

        final CurrencyConverter converter = (amount, from, to) -> {
            if (!from.getCode().equals(to.getCode())) {
                throw new IllegalArgumentException(
                        "These snapshot tests do not configure cross-currency conversion."
                );
            }

            return new CurrencyConversionResult(
                    amount,
                    from,
                    to,
                    BigDecimal.ONE,
                    amount
            );
        };

        final Settings settings = new Settings();
        settings.setBaseCurrency(FiatCurrency.EUR.getCode());

        dashboardFacade = new DefaultDashboardFacade(
                walletService,
                new DefaultCategoryService(),
                new DefaultBudgetMonitor(),
                new DefaultFriendLoanSummaryService(),
                new DefaultWalletInsightService(),
                new CategoryCatalog(),
                converter,
                settings
        );
    }

    @Test
    void shouldLoadDashboardSnapshotForCurrentWallet() {
        final CashAccount wallet = new CashAccount(MAIN_WALLET, FiatCurrency.EUR);
        wallet.setBudgetSettings(
                new DefaultBudgetSettings(BUDGET_LIMIT, WARNING_THRESHOLD)
        );

        walletService.addWallet(wallet);
        walletService.selectWallet(wallet.getId());

        final Category salary = createCategory(SALARY, CategoryType.INCOME);
        final Category food = createCategory(FOOD, CategoryType.EXPENSE);
        final LocalDate today = LocalDate.now();

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, SALARY_AMOUNT),
                salary,
                today,
                SALARY,
                "Monthly salary"
        ));

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, FOOD_EXPENSE.negate()),
                food,
                today,
                "Groceries",
                "Food expenses"
        ));

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals(MAIN_WALLET, snapshot.getWalletName());
        assertEquals(wallet.getBaseCurrency().toString(), snapshot.getWalletCurrency());
        assertAmountEquals(
                SALARY_AMOUNT.subtract(FOOD_EXPENSE),
                snapshot.getTotalBalance()
        );

        assertEquals(TWO_ITEMS, snapshot.getRecentTransactions().size());
        assertAmountEquals(
                SALARY_AMOUNT,
                snapshot.getCategorySummaries().get(SALARY)
        );
        assertAmountEquals(
                FOOD_EXPENSE,
                snapshot.getCategorySummaries().get(FOOD)
        );

        assertAmountEquals(BUDGET_LIMIT, snapshot.getBudgetLimit());
        assertAmountEquals(WARNING_THRESHOLD, snapshot.getWarningThreshold());
        assertEquals(BudgetStatus.SAFE, snapshot.getBudgetStatus());

        assertTrue(snapshot.getFriendLoanSummaries().isEmpty());
        assertEquals(EXPECTED_INSIGHT_COUNT, snapshot.getWalletInsights().size());

        assertEquals(1, snapshot.getAllWallets().size());
        assertEquals(MAIN_WALLET, snapshot.getAllWallets().get(0).getName());
    }

    @Test
    void shouldLoadDashboardForSelectedWalletOnly() {
        final CashAccount firstWallet = new CashAccount("First wallet", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount(SECOND_WALLET, FiatCurrency.USD);

        firstWallet.setBudgetSettings(
                new DefaultBudgetSettings(BUDGET_LIMIT, WARNING_THRESHOLD)
        );
        secondWallet.setBudgetSettings(
                new DefaultBudgetSettings(
                        SECOND_BUDGET_LIMIT,
                        SECOND_WARNING_THRESHOLD
                )
        );

        walletService.addWallet(firstWallet);
        walletService.addWallet(secondWallet);

        final Category food = createCategory(FOOD, CategoryType.EXPENSE);
        final Category travel = createCategory(TRAVEL, CategoryType.EXPENSE);
        final LocalDate today = LocalDate.now();

        walletService.selectWallet(firstWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, FIRST_EXPENSE.negate()),
                food,
                today,
                "Groceries",
                "Food expenses"
        ));

        walletService.selectWallet(secondWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.USD, TRAVEL_EXPENSE.negate()),
                travel,
                today,
                "Taxi",
                "Taxi expense"
        ));

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals(SECOND_WALLET, snapshot.getWalletName());
        assertEquals(
                secondWallet.getBaseCurrency().toString(),
                snapshot.getWalletCurrency()
        );
        assertAmountEquals(TRAVEL_EXPENSE.negate(), snapshot.getTotalBalance());
        assertEquals(1, snapshot.getRecentTransactions().size());

        final Map<String, BigDecimal> summaries = snapshot.getCategorySummaries();
        assertEquals(1, summaries.size());
        assertAmountEquals(TRAVEL_EXPENSE, summaries.get(TRAVEL));

        assertAmountEquals(SECOND_BUDGET_LIMIT, snapshot.getBudgetLimit());
        assertAmountEquals(
                SECOND_WARNING_THRESHOLD,
                snapshot.getWarningThreshold()
        );
        assertEquals(BudgetStatus.SAFE, snapshot.getBudgetStatus());

        assertTrue(snapshot.getFriendLoanSummaries().isEmpty());
        assertEquals(EXPECTED_INSIGHT_COUNT, snapshot.getWalletInsights().size());
        assertEquals(TWO_ITEMS, snapshot.getAllWallets().size());
    }

    @Test
    void shouldIncludeFriendLoanSummariesInSnapshot() {
        final CashAccount wallet = new CashAccount(MAIN_WALLET, FiatCurrency.EUR);
        wallet.setBudgetSettings(
                new DefaultBudgetSettings(BUDGET_LIMIT, WARNING_THRESHOLD)
        );

        walletService.addWallet(wallet);
        walletService.selectWallet(wallet.getId());

        final Category loanCategory = createCategory(
                "Friend loan",
                CategoryType.FRIEND_LOAN
        );

        final UUID loanId = UUID.randomUUID();
        final LocalDate today = LocalDate.now();

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, LOAN_AMOUNT.negate()),
                loanCategory,
                today,
                "Loan to " + FRIEND_NAME,
                "Initial loan",
                loanId,
                FRIEND_NAME
        ));

        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, REPAYMENT_AMOUNT),
                loanCategory,
                today,
                FRIEND_NAME + " repayment",
                "Partial repayment",
                loanId,
                FRIEND_NAME
        ));

        final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();

        assertEquals(1, snapshot.getFriendLoanSummaries().size());

        final FriendLoanSummary summary = snapshot.getFriendLoanSummaries().get(0);

        assertEquals(FRIEND_NAME, summary.getFriendName());
        assertAmountEquals(LOAN_AMOUNT, summary.getTotalGiven());
        assertAmountEquals(REPAYMENT_AMOUNT, summary.getTotalReceived());
        assertAmountEquals(
                LOAN_AMOUNT.subtract(REPAYMENT_AMOUNT),
                summary.getNetBalance()
        );

        assertEquals(EXPECTED_INSIGHT_COUNT, snapshot.getWalletInsights().size());
        assertEquals(1, snapshot.getAllWallets().size());
    }

    /**
     * Creates a category for the test transactions.
     *
     * @param name the category name
     * @param type the category type
     * @return the category
     */
    private static Category createCategory(
            final String name,
            final CategoryType type) {

        return new Category(name, ARGBColor.WHITE, type);
    }

    /**
     * Compares amounts independently of their decimal scale.
     *
     * @param expected the expected amount
     * @param actual the actual amount
     */
    private static void assertAmountEquals(
            final BigDecimal expected,
            final BigDecimal actual) {

        assertEquals(
                expected.stripTrailingZeros(),
                actual.stripTrailingZeros()
        );
    }
}
