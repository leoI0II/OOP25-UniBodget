package it.unibo.unibodget.controller.dashboard.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.dashboard.impl.DefaultTotalCashBalanceService;
import it.unibo.unibodget.model.dashboard.impl.DefaultTransactionHistoryFilterService;
import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.dashboard.impl.TransactionFilterCriteria;
import it.unibo.unibodget.model.dashboard.impl.TransactionSortOrder;
import it.unibo.unibodget.model.dashboard.impl.WalletInsight;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.CashTransactionFactory;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.view.dashboard.api.DashboardView;
import it.unibo.unibodget.view.dashboard.api.DashboardViewActions;
import it.unibo.unibodget.view.dashboard.impl.BaseCurrencyDialog;
import it.unibo.unibodget.view.dashboard.impl.EditBudgetDialog;
import it.unibo.unibodget.view.dashboard.impl.FriendLoanOperation;
import it.unibo.unibodget.view.dashboard.impl.NewCategoryRequest;
import it.unibo.unibodget.view.dashboard.impl.NewTransactionDialog;
import it.unibo.unibodget.view.dashboard.impl.NewTransactionRequest;
import it.unibo.unibodget.view.dashboard.impl.NewWalletDialog;
import it.unibo.unibodget.view.dashboard.state.DashboardDestination;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.BalanceCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.BudgetCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.CategoryBreakdownItemViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.ExpenseBreakdownCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.ExpenseRipartitionCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.FriendLoanCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.HeaderViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.InsightCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.NavigationItemViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.NavigationMenuViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.SidebarFooterTotalViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.SidebarViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.SidebarWalletItemViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionRowViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionSectionViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionTableViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.WalletSectionViewState;
import it.unibo.unibodget.view.dashboard.state.TransactionFilterInput;

/**
 * Default controller for the dashboard screen.
 */
public final class DefaultDashboardController implements DashboardViewActions {

    private static final int MAX_CATEGORY_ROWS = 4;
    private static final String DEFAULT_CATEGORY_COLOR = "#7F8CFF";

    private final DashboardView view;
    private final DashboardFacade dashboardFacade;
    private final CashAccountService cashAccountService;
    private final DefaultTotalCashBalanceService totalCashBalanceService;
    private final Settings settings;
    private final CashTransactionFactory cashTransactionFactory;
    private final DefaultTransactionHistoryFilterService transactionHistoryFilterService;

    private TransactionFilterInput currentFilterInput;

    public DefaultDashboardController(
            final DashboardView view,
            final DashboardFacade dashboardFacade,
            final CashAccountService cashAccountService,
            final DefaultTotalCashBalanceService totalCashBalanceService,
            final Settings settings,
            final CashTransactionFactory cashTransactionFactory) {
        this.view = Objects.requireNonNull(view);
        this.dashboardFacade = Objects.requireNonNull(dashboardFacade);
        this.cashAccountService = Objects.requireNonNull(cashAccountService);
        this.totalCashBalanceService = Objects.requireNonNull(totalCashBalanceService);
        this.settings = Objects.requireNonNull(settings);
        this.cashTransactionFactory = Objects.requireNonNull(cashTransactionFactory);
        this.transactionHistoryFilterService = new DefaultTransactionHistoryFilterService();
        this.currentFilterInput = new TransactionFilterInput(
                null,
                null,
                null,
                null,
                null,
                "Newest first"
        );
    }

    public void init() {
        view.bindActions(this);
    }

    @Override
    public void onViewOpened() {
        refreshDashboard();
    }

    @Override
    public void onWalletSelected(final UUID walletId) {
        try {
            final boolean selected = cashAccountService.selectWallet(walletId);
            if (!selected) {
                view.showError("Unable to switch wallet.");
                return;
            }
            refreshDashboard();
        } catch (Exception exception) {
            view.showError("Unable to switch wallet.");
        }
    }

    @Override
    public void onNavigationRequested(final DashboardDestination destination) {
        if (destination == DashboardDestination.SETTINGS) {
            openBaseCurrencyDialog();
        }
    }

    @Override
    public void onSettingsRequested() {
        onNavigationRequested(DashboardDestination.SETTINGS);
    }

    @Override
    public void onTransactionFiltersChanged(final TransactionFilterInput input) {
        this.currentFilterInput = Objects.requireNonNull(input);
        refreshDashboard();
    }

    @Override
    public void onExportTransactionsRequested() {
    }

    @Override
    public void onCreateWalletRequested() {
        try {
            final FiatCurrency currentCurrency = settings.getBaseCurrency() instanceof FiatCurrency fiat
                    ? fiat
                    : FiatCurrency.EUR;

            final NewWalletDialog dialog = new NewWalletDialog(currentCurrency);
            dialog.showAndWait().ifPresent(newWallet -> {
                cashAccountService.addWallet(newWallet);
                cashAccountService.selectWallet(newWallet.getId());
                refreshDashboard();
            });
        } catch (Exception exception) {
            view.showError("Unable to create wallet.");
        }
    }

    @Override
    public void onCreateTransactionRequested() {
        try {
            final CashAccount currentWallet = cashAccountService.getCurrentWallet()
                    .orElseThrow(() -> new IllegalStateException("No wallet is currently selected."));

            final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();
            final List<FriendLoanSummary> openFriendLoans = snapshot.getFriendLoanSummaries().stream()
                    .filter(summary -> summary.getNetBalance().signum() > 0)
                    .toList();

            final NewTransactionDialog dialog = new NewTransactionDialog(
                    currentWallet.getBaseCurrency(),
                    dashboardFacade.getCategoryCatalog(),
                    openFriendLoans,
                    this::createCustomCategory
            );

            dialog.showAndWait().ifPresent(request -> {
                validateFriendLoanRequest(request, openFriendLoans);
                final CashTransaction transaction = toCashTransaction(
                        request,
                        currentWallet.getBaseCurrency(),
                        openFriendLoans
                );
                cashAccountService.addTransaction(transaction);
                refreshDashboard();
            });
        } catch (Exception exception) {
            view.showError("Unable to create transaction.");
        }
    }

    @Override
    public void onEditBudgetRequested() {
        openEditBudgetDialog();
    }

    private void openEditBudgetDialog() {
        try {
            final CashAccount currentWallet = cashAccountService.getCurrentWallet()
                    .orElseThrow(() -> new IllegalStateException("No wallet is currently selected."));

            final EditBudgetDialog dialog = new EditBudgetDialog(currentWallet.getBudgetSettings());
            dialog.showAndWait().ifPresent(request -> {
                currentWallet.setBudgetSettings(new DefaultBudgetSettings(
                        request.limitValue(),
                        request.warningThreshold()
                ));
                refreshDashboard();
            });
        } catch (Exception exception) {
            view.showError("Unable to update monthly budget.");
        }
    }

    private Optional<Category> createCustomCategory(
            final NewCategoryRequest request,
            final CategoryType type) {
        try {
            final Category createdCategory = new Category(
                    request.name(),
                    new ARGBColor(request.colorHex()),
                    type
            );
            dashboardFacade.getCategoryCatalog().addCustomCategory(createdCategory);
            return Optional.of(createdCategory);
        } catch (Exception exception) {
            view.showError("Unable to create category.");
            return Optional.empty();
        }
    }

    private void validateFriendLoanRequest(
            final NewTransactionRequest request,
            final List<FriendLoanSummary> openFriendLoans) {
        if (request.transactionType() != CategoryType.FRIEND_LOAN) {
            return;
        }

        if (request.friendLoanOperation() == FriendLoanOperation.REPAYMENT) {
            final FriendLoanSummary targetLoan = openFriendLoans.stream()
                    .filter(summary -> summary.getFriendLoanId().equals(request.existingFriendLoanId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Selected loan is no longer open."));

            if (request.unsignedAmount().compareTo(targetLoan.getNetBalance()) > 0) {
                throw new IllegalArgumentException("Repayment amount exceeds residual loan amount.");
            }
        }
    }

    private CashTransaction toCashTransaction(
            final NewTransactionRequest request,
            final CurrencyUnit walletCurrency,
            final List<FriendLoanSummary> openFriendLoans) {

        if (request.transactionType() == CategoryType.FRIEND_LOAN) {
            if (request.friendLoanOperation() == FriendLoanOperation.REPAYMENT) {
                final FriendLoanSummary targetLoan = openFriendLoans.stream()
                        .filter(summary -> summary.getFriendLoanId().equals(request.existingFriendLoanId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown friend loan id."));

                return cashTransactionFactory.createFriendLoanRepayment(
                        request.unsignedAmount(),
                        walletCurrency,
                        request.transactionType(),
                        request.category(),
                        request.date(),
                        request.description(),
                        request.notes(),
                        targetLoan.getFriendName(),
                        targetLoan.getFriendLoanId()
                );
            }

            return cashTransactionFactory.createFriendLoan(
                    request.unsignedAmount(),
                    walletCurrency,
                    request.transactionType(),
                    request.category(),
                    request.date(),
                    request.description(),
                    request.notes(),
                    request.friendName()
            );
        }

        return cashTransactionFactory.create(
                request.unsignedAmount(),
                walletCurrency,
                request.transactionType(),
                request.category(),
                request.date(),
                request.description(),
                request.notes()
        );
    }

    private void refreshDashboard() {
        try {
            final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();
            view.render(toViewState(snapshot));
        } catch (Exception exception) {
            view.showError("Unable to load dashboard data.");
        }
    }

    private DashboardViewState toViewState(final DashboardSnapshot snapshot) {
        final Asset totalAcrossWallets =
                totalCashBalanceService.getTotalBalanceIn(settings.getBaseCurrency());

        final String walletCurrencyLabel = snapshot.getWalletCurrency();

        final CurrencyUnit selectedWalletCurrency = cashAccountService.getCurrentWallet()
                .map(CashAccount::getBaseCurrency)
                .orElse(settings.getBaseCurrency());

        final List<SidebarWalletItemViewState> wallets = cashAccountService.getWallets().stream()
                .map(wallet -> new SidebarWalletItemViewState(
                        wallet.getId(),
                        wallet.getName(),
                        formatSignedAmount(wallet.getBalance().amount(), wallet.getBaseCurrency()),
                        wallet.getName().equals(snapshot.getWalletName())
                ))
                .toList();

        final Map<String, BigDecimal> expenseCategorySummaries =
                buildExpenseCategorySummaries(snapshot);

        final BigDecimal totalExpense = expenseCategorySummaries.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final List<CategoryBreakdownItemViewState> ripartitionCategories =
                buildRipartitionCategoryBreakdown(expenseCategorySummaries, totalExpense, selectedWalletCurrency);

        final List<CategoryBreakdownItemViewState> rankedCategories =
                buildRankedCategoryBreakdown(expenseCategorySummaries, totalExpense, selectedWalletCurrency);

        final TransactionFilterCriteria filterCriteria = toFilterCriteria(currentFilterInput);

        final List<TransactionRowViewState> transactionRows = transactionHistoryFilterService
                .filter(
                        snapshot.getRecentTransactions().stream()
                                .filter(CashTransaction.class::isInstance)
                                .map(CashTransaction.class::cast)
                                .toList(),
                        filterCriteria
                ).stream()
                .map(transaction -> new TransactionRowViewState(
                        transaction.getDescription(),
                        transaction.getDate().toString(),
                        transaction.getCategory().getName(),
                        formatSignedAmount(transaction),
                        toCssHex(transaction.getCategory().getColorHex()),
                        isPositiveTransaction(transaction)
                ))
                .toList();

        final List<InsightCardViewState> insights = snapshot.getWalletInsights().stream()
                .filter(this::isSupportedInsight)
                .map(insight -> new InsightCardViewState(
                        insight.title(),
                        insight.message(),
                        insight.trend().name()
                ))
                .toList();

        final BigDecimal budgetLimit = snapshot.getBudgetLimit();
        final BigDecimal spentAmount = totalExpense;
        final BigDecimal remainingAmount = budgetLimit.subtract(spentAmount);
        final double budgetUsagePercentage = computeUsagePercentage(spentAmount, budgetLimit);

        return new DashboardViewState(
                new SidebarViewState(
                        new NavigationMenuViewState(
                                List.of(
                                        new NavigationItemViewState("Dashboard", DashboardDestination.DASHBOARD, true),
                                        new NavigationItemViewState("Currency Converter", DashboardDestination.TRANSACTIONS, true),
                                        new NavigationItemViewState("Investment", DashboardDestination.CATEGORIES, true),
                                        new NavigationItemViewState("Settings", DashboardDestination.SETTINGS, true)
                                )
                        ),
                        new WalletSectionViewState(
                                "Wallets",
                                cashAccountService.getWallets().size() + " wallets",
                                wallets
                        ),
                        new SidebarFooterTotalViewState(
                                "Total across wallets",
                                formatSignedAmount(totalAcrossWallets.amount(), settings.getBaseCurrency()),
                                formatCurrencyLabel(settings.getBaseCurrency())
                        )
                ),
                new HeaderViewState(
                        "Financial Overview",
                        "Active wallet: " + snapshot.getWalletName(),
                        "Summary of the current wallet and aggregated totals"
                ),
                new BalanceCardViewState(
                        "Wallet Net Balance",
                        formatSignedAmount(snapshot.getTotalBalance(), selectedWalletCurrency),
                        walletCurrencyLabel
                ),
                new ExpenseRipartitionCardViewState(
                        "Expense Ripartition",
                        formatAmountWithCurrency(totalExpense, selectedWalletCurrency),
                        ripartitionCategories,
                        "No expense data available."
                ),
                new ExpenseBreakdownCardViewState(
                        "Top Expense Categories",
                        formatAmountWithCurrency(totalExpense, selectedWalletCurrency),
                        rankedCategories
                ),
                new BudgetCardViewState(
                        "Monthly Budget",
                        snapshot.getBudgetStatus().name(),
                        formatAmountWithCurrency(spentAmount, selectedWalletCurrency)
                                + " / " + formatAmountWithCurrency(budgetLimit, selectedWalletCurrency),
                        "Warning threshold: " + formatPercentage(snapshot.getWarningThreshold()),
                        "Edit budget",
                        budgetUsagePercentage,
                        formatAmountWithCurrency(spentAmount, selectedWalletCurrency),
                        formatAmountWithCurrency(budgetLimit, selectedWalletCurrency),
                        formatSignedAmount(remainingAmount, selectedWalletCurrency)
                ),
                new FriendLoanCardViewState(
                        "Friend Loans",
                        formatAmountWithCurrency(
                                sumFriendLoanOutstanding(snapshot.getFriendLoanSummaries()),
                                selectedWalletCurrency
                        ),
                        buildFriendLoanMessage(snapshot.getFriendLoanSummaries())
                ),
                insights,
                new TransactionSectionViewState(
                        "Wallet Transactions",
                        "Export CSV",
                        new TransactionTableViewState(
                                transactionRows,
                                "No transactions available.",
                                currentFilterInput
                        )
                )
        );
    }

    private Map<String, BigDecimal> buildExpenseCategorySummaries(final DashboardSnapshot snapshot) {
        final Map<String, BigDecimal> summaries = new LinkedHashMap<>();

        snapshot.getRecentTransactions().stream()
                .filter(CashTransaction.class::isInstance)
                .map(CashTransaction.class::cast)
                .filter(this::isExpenseChartTransaction)
                .forEach(transaction -> summaries.merge(
                        transaction.getCategory().getName(),
                        transaction.getAsset().amount().abs(),
                        BigDecimal::add
                ));

        return summaries;
    }

    private boolean isExpenseChartTransaction(final CashTransaction transaction) {
        final CategoryType type = transaction.getCategory().getType();
        final BigDecimal amount = transaction.getAsset().amount();

        if (type == CategoryType.EXPENSE) {
            return amount.signum() < 0;
        }

        if (type == CategoryType.FRIEND_LOAN) {
            return amount.signum() < 0;
        }

        return false;
    }

    private List<CategoryBreakdownItemViewState> buildRipartitionCategoryBreakdown(
            final Map<String, BigDecimal> categorySummaries,
            final BigDecimal total,
            final CurrencyUnit currency) {
        return categorySummaries.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new CategoryBreakdownItemViewState(
                        "",
                        entry.getKey(),
                        formatAmountWithCurrency(entry.getValue(), currency),
                        total.signum() == 0
                                ? "0%"
                                : formatPercentage(entry.getValue().divide(total, 4, RoundingMode.HALF_UP)),
                        total.signum() == 0
                                ? 0.0
                                : entry.getValue()
                                        .divide(total, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                        .doubleValue(),
                        resolveCategoryColor(entry.getKey())
                ))
                .toList();
    }

    private List<CategoryBreakdownItemViewState> buildRankedCategoryBreakdown(
            final Map<String, BigDecimal> categorySummaries,
            final BigDecimal total,
            final CurrencyUnit currency) {
        final List<Map.Entry<String, BigDecimal>> rankedEntries = categorySummaries.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_CATEGORY_ROWS)
                .toList();

        return IntStream.range(0, rankedEntries.size())
                .mapToObj(index -> {
                    final Map.Entry<String, BigDecimal> entry = rankedEntries.get(index);
                    final BigDecimal ratio = total.signum() == 0
                            ? BigDecimal.ZERO
                            : entry.getValue().divide(total, 4, RoundingMode.HALF_UP);

                    return new CategoryBreakdownItemViewState(
                            String.valueOf(index + 1),
                            entry.getKey(),
                            formatAmountWithCurrency(entry.getValue(), currency),
                            total.signum() == 0 ? "0%" : formatPercentage(ratio),
                            ratio.multiply(BigDecimal.valueOf(100)).doubleValue(),
                            resolveCategoryColor(entry.getKey())
                    );
                })
                .toList();
    }

    private boolean isSupportedInsight(final WalletInsight insight) {
        final String title = insight.title().toLowerCase();
        final String message = insight.message().toLowerCase();
        return !title.contains("bank loan") && !message.contains("bank loan");
    }

    private String buildFriendLoanMessage(final List<FriendLoanSummary> summaries) {
        if (summaries.isEmpty()) {
            return "No open friend loans for the current wallet.";
        }
        final FriendLoanSummary first = summaries.get(0);
        return "Loan with " + first.getFriendName() + " is still open and should be monitored.";
    }

    private BigDecimal sumFriendLoanOutstanding(final List<FriendLoanSummary> summaries) {
        return summaries.stream()
                .map(FriendLoanSummary::getNetBalance)
                .filter(balance -> balance.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatAmount(final BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    private String formatAmountWithCurrency(final BigDecimal amount, final CurrencyUnit currency) {
        return currency.getSymbol() + " " + formatAmount(amount.abs());
    }

    private String formatSignedAmount(final CashTransaction transaction) {
        final BigDecimal signedAmount = transaction.getAsset().amount();
        final String sign = signedAmount.signum() >= 0 ? "+" : "-";
        final String currencySymbol = transaction.getAsset().currency().getSymbol();
        return sign + " " + currencySymbol + " " + formatAmount(signedAmount.abs());
    }

    private String formatSignedAmount(final BigDecimal amount, final CurrencyUnit currency) {
        final String sign = amount.signum() >= 0 ? "+" : "-";
        return sign + " " + currency.getSymbol() + " " + formatAmount(amount.abs());
    }

    private String formatPercentage(final BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private double computeUsagePercentage(final BigDecimal spentAmount, final BigDecimal budgetLimit) {
        if (budgetLimit == null || budgetLimit.signum() <= 0) {
            return 0.0;
        }
        return spentAmount
                .divide(budgetLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private boolean isPositiveTransaction(final CashTransaction transaction) {
        return transaction.getAsset().amount().signum() >= 0;
    }

    private String resolveCategoryColor(final String categoryName) {
        return dashboardFacade.getCategoryCatalog().getActiveCategories().stream()
                .filter(category -> category.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .map(Category::getColorHex)
                .map(this::toCssHex)
                .orElse(DEFAULT_CATEGORY_COLOR);
    }

    private String toCssHex(final ARGBColor color) {
        if (color == null) {
            return DEFAULT_CATEGORY_COLOR;
        }
        return String.format("#%02X%02X%02X", color.red(), color.green(), color.blue());
    }

    private void openBaseCurrencyDialog() {
        try {
            final FiatCurrency currentCurrency = settings.getBaseCurrency() instanceof FiatCurrency fiat
                    ? fiat
                    : FiatCurrency.EUR;

            final BaseCurrencyDialog dialog = new BaseCurrencyDialog(currentCurrency);
            dialog.showAndWait().ifPresent(selectedCurrency -> {
                settings.setBaseCurrency(selectedCurrency);
                refreshDashboard();
            });
        } catch (Exception exception) {
            view.showError("Unable to change base currency.");
        }
    }

    private String formatCurrencyLabel(final Object currency) {
        if (currency instanceof FiatCurrency fiat) {
            return fiat.getSymbol() + " " + fiat.getShortName();
        }
        return String.valueOf(currency);
    }

    private TransactionFilterCriteria toFilterCriteria(final TransactionFilterInput input) {
        final TransactionFilterInput safeInput = input == null
                ? new TransactionFilterInput(null, null, null, null, null, "Newest first")
                : input;

        return new TransactionFilterCriteria(
                normalizeBlank(safeInput.categoryName()),
                parseCategoryType(safeInput.categoryType()),
                safeInput.fromDate(),
                safeInput.toDate(),
                normalizeBlank(safeInput.keyword()),
                parseSortOrder(safeInput.sortOrder())
        );
    }

    private CategoryType parseCategoryType(final String rawType) {
        final String normalized = normalizeBlank(rawType);
        if (normalized == null) {
            return null;
        }
        return CategoryType.valueOf(normalized.toUpperCase().replace(' ', '_'));
    }

    private TransactionSortOrder parseSortOrder(final String rawSortOrder) {
        final String normalized = normalizeBlank(rawSortOrder);
        if (normalized == null) {
            return TransactionSortOrder.NEWEST_FIRST;
        }

        return switch (normalized.toLowerCase()) {
            case "newest first" -> TransactionSortOrder.NEWEST_FIRST;
            case "oldest first" -> TransactionSortOrder.OLDEST_FIRST;
            case "highest amount" -> TransactionSortOrder.HIGHEST_AMOUNT_FIRST;
            default -> TransactionSortOrder.valueOf(normalized.toUpperCase().replace(' ', '_'));
        };
    }

    private String normalizeBlank(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}