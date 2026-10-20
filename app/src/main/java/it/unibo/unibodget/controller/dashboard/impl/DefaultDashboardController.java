package it.unibo.unibodget.controller.dashboard.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVPrinter;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.dashboard.impl.DefaultTransactionHistoryFilterService;
import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.dashboard.impl.TransactionFilterCriteria;
import it.unibo.unibodget.model.dashboard.impl.TransactionSortOrder;
import it.unibo.unibodget.model.dashboard.support.WalletObserver;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.CashTransactionFactory;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.MainInfoNotificationEvent;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.persistency.CsvExportFormat;
import it.unibo.unibodget.view.dashboard.api.DashboardView;
import it.unibo.unibodget.view.dashboard.api.DashboardViewActions;
import it.unibo.unibodget.view.dashboard.impl.EditBudgetDialog;
import it.unibo.unibodget.view.dashboard.impl.FriendLoanOperation;
import it.unibo.unibodget.view.dashboard.impl.NewCategoryRequest;
import it.unibo.unibodget.view.dashboard.impl.NewTransactionDialog;
import it.unibo.unibodget.view.dashboard.impl.NewTransactionRequest;
import it.unibo.unibodget.view.dashboard.impl.NewWalletDialog;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.BalanceCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.BudgetCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.CategoryBreakdownItemViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.ExpenseBreakdownCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.ExpenseRipartitionCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.FriendLoanCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.HeaderViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.InsightCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionRowViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionSectionViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionTableViewState;
import it.unibo.unibodget.view.dashboard.state.TransactionFilterInput;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Default implementation of the dashboard controller.
 *
 * <p>
 * This controller coordinates the dashboard view and the underlying model
 * services. It receives actions from the view, loads aggregated data from the
 * dashboard facade, transforms that data into immutable view-state records, and
 * triggers UI updates.
 * </p>
 *
 * <p>
 * It acts as a {@link WalletObserver} on the {@link CashAccountService},
 * automatically refreshing the dashboard UI and notifying registered listeners
 * whenever the wallet or its transactions are modified.
 * </p>
 */
public final class DefaultDashboardController implements DashboardViewActions, WalletObserver {

    private static final int MAX_CATEGORY_ROWS = 4;
    private static final String DEFAULT_CATEGORY_COLOR = "#7F8CFF";
    private static final String DEFAULT_SORT_ORDER_LABEL = "Newest first";
    private static final String ERR_UNABLE_TO_SWITCH_WALLET = "Unable to switch wallet.";
    private static final String ERR_UNABLE_TO_CREATE_WALLET = "Unable to create wallet.";
    private static final String ERR_UNABLE_TO_DELETE_TRANSACTION = "Unable to delete transaction.";
    private static final String ERR_UNABLE_TO_CREATE_TRANSACTION = "Unable to create transaction.";
    private static final String ERR_UNABLE_TO_EDIT_TRANSACTION = "Unable to edit transaction.";

    private final DashboardView view;
    private final DashboardFacade dashboardFacade;
    private final CashAccountService cashAccountService;
    private final Settings settings;
    private final CashTransactionFactory cashTransactionFactory;
    private final DefaultTransactionHistoryFilterService transactionHistoryFilterService;

    private TransactionFilterInput currentFilterInput;
    private Runnable onDashboardRefreshed = () -> { };
    private boolean refreshing;

    /**
     * Creates a new dashboard controller.
     *
     * @param view
     *            the dashboard view; must not be {@code null}
     * @param dashboardFacade
     *            the dashboard facade; must not be {@code null}
     * @param cashAccountService
     *            the cash-account service; must not be {@code null}
     * @param settings
     *            the application settings; must not be {@code null}
     * @param cashTransactionFactory
     *            the transaction factory; must not be {@code null}
     */
    public DefaultDashboardController(
            final DashboardView view,
            final DashboardFacade dashboardFacade,
            final CashAccountService cashAccountService,
            final Settings settings,
            final CashTransactionFactory cashTransactionFactory) {
        this.view = Objects.requireNonNull(view);
        this.dashboardFacade = Objects.requireNonNull(dashboardFacade);
        this.cashAccountService = Objects.requireNonNull(cashAccountService);
        this.settings = Objects.requireNonNull(settings);
        this.cashTransactionFactory = Objects.requireNonNull(cashTransactionFactory);
        this.transactionHistoryFilterService = new DefaultTransactionHistoryFilterService();
        this.currentFilterInput = new TransactionFilterInput(
                null, null, null, null, null, DEFAULT_SORT_ORDER_LABEL
        );
        this.cashAccountService.addObserver(this);
    }

    /**
     * Initializes the controller and binds it to the view.
     */
    public void init() {
        view.bindActions(this);
    }

    /**
     * Registers a callback invoked after every dashboard refresh (i.e. after any
     * mutation such as add/edit/delete transaction, wallet or budget change).
     *
     * <p>Used by the shell to keep the shared sidebar in sync with dashboard data.</p>
     *
     * @param callback the callback to run after each refresh; must not be {@code null}
     */
    public void setOnDashboardRefreshed(final Runnable callback) {
        this.onDashboardRefreshed = Objects.requireNonNull(callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        try {
            if (Platform.isFxApplicationThread()) {
                refreshDashboard();
            } else {
                Platform.runLater(this::refreshDashboard);
            }
        } catch (final IllegalStateException exception) {
            refreshDashboard();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewOpened() {
        refreshDashboard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWalletSelected(final UUID walletId) {
        try {
            final boolean selected = cashAccountService.selectWallet(walletId);
            if (!selected) {
                view.showError(ERR_UNABLE_TO_SWITCH_WALLET);
                MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_SWITCH_WALLET));
            }
        } catch (final IllegalArgumentException | IllegalStateException exception) {
            view.showError(ERR_UNABLE_TO_SWITCH_WALLET);
            MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_SWITCH_WALLET));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTransactionFiltersChanged(final TransactionFilterInput input) {
        this.currentFilterInput = Objects.requireNonNull(input);
        refreshDashboard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExportTransactionsRequested() {
        final Optional<CashAccount> walletOpt = cashAccountService.getCurrentWallet();
        if (walletOpt.isEmpty()) {
            MessageBus.send(new MainErrorNotificationEvent("No active wallet to export."));
            return;
        }

        final CashAccount wallet = walletOpt.get();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV transactions data");

        final String safeWalletName = wallet.getName().replaceAll("\\s+", "_");
        fileChooser.setInitialFileName(safeWalletName + "_transactions.csv");

        final File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (downloadsDir.exists()) {
            fileChooser.setInitialDirectory(downloadsDir);
        }
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        final Window window = view instanceof Node node && node.getScene() != null
                ? node.getScene().getWindow()
                : null;
        final File selectedFile = window instanceof Stage stage
                ? fileChooser.showSaveDialog(stage)
                : fileChooser.showSaveDialog(null);

        if (selectedFile == null) {
            return;
        }

        final boolean success = exportTransactionsToCsv(selectedFile, wallet);
        if (success) {
            MessageBus.send(new MainInfoNotificationEvent("Transactions exported successfully!"));
        } else {
            MessageBus.send(new MainErrorNotificationEvent("Failed to export transactions."));
        }
    }

    /**
     * Exports the filtered transactions of the given wallet to a CSV file.
     *
     * @param file the destination CSV file
     * @param wallet the cash wallet whose transactions are exported
     * @return {@code true} if export succeeded, {@code false} otherwise
     */
    public boolean exportTransactionsToCsv(final File file, final CashAccount wallet) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(wallet, "wallet");

        final TransactionFilterCriteria filterCriteria = toFilterCriteria(currentFilterInput);
        final List<CashTransaction> filtered = transactionHistoryFilterService.filter(
                wallet.getHistory().getTransactions(),
                filterCriteria
        );

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, CsvExportFormat.getFormat())) {

            printer.printRecord(
                    "Wallet", "Currency", "Balance", "Export Date", "Total Rows");
            printer.printRecord(
                    wallet.getName(),
                    wallet.getBaseCurrency().getCode(),
                    wallet.getBalance().amount().toPlainString(),
                    LocalDate.now().toString(),
                    filtered.size());
            printer.println();

            printer.printRecord(
                    "Date", "Description", "Category", "Type", "Amount", "Currency", "Notes");

            for (final CashTransaction transaction : filtered) {
                printer.printRecord(
                        transaction.getDate().toString(),
                        transaction.getDescription(),
                        transaction.getCategory().getName(),
                        transaction.getCategory().getType().name(),
                        transaction.getAsset().amount().toPlainString(),
                        wallet.getBaseCurrency().getCode(),
                        transaction.getNotes());
            }

            return true;
        } catch (final IOException exception) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateWalletRequested() {
        try {
            final FiatCurrency currentCurrency = settings.getBaseCurrencyUnit() instanceof FiatCurrency fiat
                    ? fiat
                    : FiatCurrency.EUR;

            final NewWalletDialog dialog = new NewWalletDialog(currentCurrency);
            dialog.showAndWait().ifPresent(newWallet -> {
                cashAccountService.addWallet(newWallet);
                cashAccountService.selectWallet(newWallet.getId());
                MessageBus.send(new MainInfoNotificationEvent(
                        "Wallet '" + newWallet.getName() + "' created successfully!"));
            });
        } catch (final IllegalArgumentException | IllegalStateException exception) {
            view.showError(ERR_UNABLE_TO_CREATE_WALLET);
            MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_CREATE_WALLET));
        }
    }

    /**
     * Renames the cash wallet with the given identifier and refreshes the dashboard.
     *
     * @param walletId the wallet identifier
     * @param newName  the new display name
     * @throws IllegalArgumentException if the name breaks the wallet naming rules;
     *                                  left to the caller, which asks the user again
     */
    public void renameWallet(final UUID walletId, final String newName) {
        if (!cashAccountService.renameWallet(walletId, newName)) {
            view.showError("Unable to rename wallet: wallet not found.");
            MessageBus.send(new MainErrorNotificationEvent("Unable to rename wallet: wallet not found."));
            return;
        }
        MessageBus.send(new MainInfoNotificationEvent("Wallet renamed to '" + newName + "' successfully!"));
        refreshDashboard();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Removes the cash wallet and its whole history. If it was the selected wallet,
     * the first remaining one becomes current.</p>
     */
    @Override
    public void onDeleteWalletRequested(final UUID walletId) {
        if (!cashAccountService.removeWallet(walletId)) {
            view.showError("Unable to delete wallet: wallet not found.");
            MessageBus.send(new MainErrorNotificationEvent("Unable to delete wallet: wallet not found."));
            return;
        }
        MessageBus.send(new MainInfoNotificationEvent("Wallet deleted successfully!"));
        refreshDashboard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateTransactionRequested() {
        try {
            final CashAccount currentWallet = getRequiredCurrentWallet();
            final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();
            final List<FriendLoanSummary> openFriendLoans = getOpenFriendLoans(snapshot);

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
                MessageBus.send(new MainInfoNotificationEvent("Transaction added successfully!"));
            });
        } catch (final IllegalArgumentException exception) {
            view.showError(exception.getMessage());
            MessageBus.send(new MainErrorNotificationEvent(exception.getMessage()));
        } catch (final IllegalStateException exception) {
            view.showError(ERR_UNABLE_TO_CREATE_TRANSACTION);
            MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_CREATE_TRANSACTION));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEditBudgetRequested() {
        openEditBudgetDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEditTransactionRequested(final UUID transactionRowId) {
        try {
            final CashAccount currentWallet = getRequiredCurrentWallet();
            final CashTransaction originalTransaction = findTransactionById(currentWallet, transactionRowId)
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

            final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();
            final List<FriendLoanSummary> openFriendLoans = getOpenFriendLoans(snapshot);

            final NewTransactionDialog dialog = new NewTransactionDialog(
                    currentWallet.getBaseCurrency(),
                    dashboardFacade.getCategoryCatalog(),
                    openFriendLoans,
                    this::createCustomCategory,
                    originalTransaction
            );

            dialog.showAndWait().ifPresent(request -> {
                validateFriendLoanRequest(request, openFriendLoans);
                final CashTransaction replacement = toCashTransactionPreservingId(
                        request,
                        originalTransaction,
                        currentWallet.getBaseCurrency(),
                        openFriendLoans
                );
                cashAccountService.replaceTransaction(originalTransaction, replacement);
                MessageBus.send(new MainInfoNotificationEvent("Transaction updated successfully!"));
            });
        } catch (final IllegalArgumentException exception) {
            view.showError(exception.getMessage());
            MessageBus.send(new MainErrorNotificationEvent(exception.getMessage()));
        } catch (final IllegalStateException exception) {
            view.showError(ERR_UNABLE_TO_EDIT_TRANSACTION);
            MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_EDIT_TRANSACTION));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeleteTransactionRequested(final UUID transactionRowId) {
        try {
            final CashAccount currentWallet = getRequiredCurrentWallet();
            final CashTransaction transactionToDelete = findTransactionById(currentWallet, transactionRowId)
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

            final boolean removed = cashAccountService.removeTransaction(transactionToDelete);
            if (!removed) {
                view.showError(ERR_UNABLE_TO_DELETE_TRANSACTION);
                MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_DELETE_TRANSACTION));
            } else {
                MessageBus.send(new MainInfoNotificationEvent("Transaction deleted successfully!"));
            }
        } catch (final IllegalArgumentException exception) {
            view.showError(exception.getMessage());
            MessageBus.send(new MainErrorNotificationEvent(exception.getMessage()));
        } catch (final IllegalStateException exception) {
            view.showError(ERR_UNABLE_TO_DELETE_TRANSACTION);
            MessageBus.send(new MainErrorNotificationEvent(ERR_UNABLE_TO_DELETE_TRANSACTION));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onManageCategoriesRequested() {
        view.showError("Category management is not available yet.");
        MessageBus.send(new MainErrorNotificationEvent("Category management is not available yet."));
    }

    /**
     * Opens the dialog used to edit the current wallet budget.
     */
    private void openEditBudgetDialog() {
        try {
            final CashAccount currentWallet = getRequiredCurrentWallet();
            final EditBudgetDialog dialog = new EditBudgetDialog(currentWallet.getBudgetSettings());
            dialog.showAndWait().ifPresent(request -> {
                currentWallet.setBudgetSettings(new DefaultBudgetSettings(
                        request.limitValue(),
                        request.warningThreshold()
                ));
                cashAccountService.notifyObservers();
                MessageBus.send(new MainInfoNotificationEvent("Monthly budget updated successfully!"));
            });
        } catch (final IllegalArgumentException | IllegalStateException exception) {
            view.showError("Unable to update monthly budget.");
            MessageBus.send(new MainErrorNotificationEvent("Unable to update monthly budget."));
        }
    }

    /**
     * Creates and stores a custom category.
     *
     * @param request
     *            the category creation request
     * @param type
     *            the category type
     * @return the created category, if successful
     */
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
            MessageBus.send(new MainInfoNotificationEvent(
                    "Category '" + createdCategory.getName() + "' created successfully!"));
            return Optional.of(createdCategory);
        } catch (final IllegalArgumentException | IllegalStateException exception) {
            view.showError("Unable to create category.");
            MessageBus.send(new MainErrorNotificationEvent("Unable to create category."));
            return Optional.empty();
        }
    }

    /**
     * Validates friend-loan-specific transaction requests.
     *
     * @param request
     *            the request to validate
     * @param openFriendLoans
     *            currently open friend loans
     */
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

    /**
     * Converts a dialog request into a new transaction.
     *
     * @param request
     *            the dialog request
     * @param walletCurrency
     *            the current wallet currency
     * @param openFriendLoans
     *            currently open friend loans
     * @return the new transaction
     */
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

                return this.cashTransactionFactory.createFriendLoanRepayment(
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

            return this.cashTransactionFactory.createFriendLoan(
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

        return this.cashTransactionFactory.create(
                request.unsignedAmount(),
                walletCurrency,
                request.transactionType(),
                request.category(),
                request.date(),
                request.description(),
                request.notes()
        );
    }

    /**
     * Converts a dialog request into a replacement transaction while preserving
     * the original identifier.
     *
     * @param request
     *            the edited request
     * @param originalTransaction
     *            the transaction being replaced
     * @param walletCurrency
     *            the current wallet currency
     * @param openFriendLoans
     *            currently open friend loans
     * @return the replacement transaction
     */
    private CashTransaction toCashTransactionPreservingId(
            final NewTransactionRequest request,
            final CashTransaction originalTransaction,
            final CurrencyUnit walletCurrency,
            final List<FriendLoanSummary> openFriendLoans) {

        final CashTransaction created = toCashTransaction(request, walletCurrency, openFriendLoans);
        return CashTransaction.builder(created)
                .id(originalTransaction.getId())
                .build();
    }

    /**
     * Reloads dashboard data and asks the view to render the new state.
     */
    private void refreshDashboard() {
        if (this.refreshing) {
            return;
        }
        this.refreshing = true;
        try {
            if (dashboardFacade.getAllCashAccounts().isEmpty()) {
                view.showError("No wallet yet — use \"+ Add wallet\" to create one.");
                onDashboardRefreshed.run();
                return;
            }
            if (dashboardFacade.getCurrentSelectedCashAccount().isEmpty()) {
                dashboardFacade.selectWallet(dashboardFacade.getAllCashAccounts().getFirst().getId());
            }
            try {
                final DashboardSnapshot snapshot = dashboardFacade.loadDashboard();
                view.render(toViewState(snapshot));
            } catch (final IllegalArgumentException | IllegalStateException exception) {
                view.showError("Unable to load dashboard data.");
            }
            onDashboardRefreshed.run();
        } finally {
            this.refreshing = false;
        }
    }

    /**
     * Maps a dashboard snapshot into immutable dashboard view state.
     *
     * @param snapshot
     *            the dashboard snapshot
     * @return the corresponding view state
     */
    private DashboardViewState toViewState(final DashboardSnapshot snapshot) {
        final String walletCurrencyLabel = snapshot.getWalletCurrency();

        final CurrencyUnit selectedWalletCurrency = cashAccountService.getCurrentWallet()
                .map(CashAccount::getBaseCurrency)
                .orElse(settings.getBaseCurrencyUnit());

        final Map<String, BigDecimal> expenseCategorySummaries = snapshot.getExpenseCategorySummaries();
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
                )
                .stream()
                .map(transaction -> new TransactionRowViewState(
                        transaction.getId(),
                        transaction.getDescription(),
                        transaction.getDate().toString(),
                        transaction.getCategory().getName(),
                        formatSignedAmount(transaction),
                        toCssHex(transaction.getCategory().getColorHex()),
                        isPositiveTransaction(transaction)
                ))
                .toList();

        final List<InsightCardViewState> insights = snapshot.getWalletInsights().stream()
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
                                + " / "
                                + formatAmountWithCurrency(budgetLimit, selectedWalletCurrency),
                        "Warning threshold " + formatPercentage(snapshot.getWarningThreshold()),
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
                        buildFriendLoanMessage(snapshot.getFriendLoanSummaries(), selectedWalletCurrency)
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

    /**
     * Builds the full expense ripartition breakdown list.
     *
     * @param categorySummaries
     *            per-category expense totals
     * @param total
     *            total expense amount
     * @param currency
     *            the selected wallet currency
     * @return the breakdown rows
     */
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

    /**
     * Builds the ranked expense breakdown list limited to the configured number
     * of rows.
     *
     * @param categorySummaries
     *            per-category expense totals
     * @param total
     *            total expense amount
     * @param currency
     *            the selected wallet currency
     * @return the ranked breakdown rows
     */
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

    /**
     * Builds a textual message describing current open friend loans.
     *
     * @param summaries
     *            friend-loan summaries
     * @param currency
     *            selected wallet currency
     * @return the message to display
     */
    private String buildFriendLoanMessage(
            final List<FriendLoanSummary> summaries,
            final CurrencyUnit currency) {
        final List<FriendLoanSummary> openLoans = summaries.stream()
                .filter(summary -> summary.getNetBalance() != null)
                .filter(summary -> summary.getNetBalance().signum() > 0)
                .toList();

        if (openLoans.isEmpty()) {
            return "No open friend loans for the current wallet.";
        }

        return openLoans.stream()
                .map(summary -> summary.getFriendName() + ": "
                        + formatAmountWithCurrency(summary.getNetBalance(), currency))
                .collect(Collectors.joining(", "));
    }

    /**
     * Sums all outstanding positive friend-loan balances.
     *
     * @param summaries
     *            friend-loan summaries
     * @return the total outstanding amount
     */
    private BigDecimal sumFriendLoanOutstanding(final List<FriendLoanSummary> summaries) {
        return summaries.stream()
                .map(FriendLoanSummary::getNetBalance)
                .filter(Objects::nonNull)
                .filter(balance -> balance.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Formats a raw amount without sign decoration.
     *
     * @param amount
     *            the amount to format
     * @return the formatted amount text
     */
    private String formatAmount(final BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    /**
     * Formats an absolute amount with currency symbol.
     *
     * @param amount
     *            the amount to format
     * @param currency
     *            the currency
     * @return the formatted amount text
     */
    private String formatAmountWithCurrency(final BigDecimal amount, final CurrencyUnit currency) {
        return currency.getSymbol() + formatAmount(amount.abs());
    }

    /**
     * Formats a transaction amount with its explicit sign and currency symbol.
     *
     * @param transaction
     *            the transaction to format
     * @return the formatted amount text
     */
    private String formatSignedAmount(final CashTransaction transaction) {
        final BigDecimal signedAmount = transaction.getAsset().amount();
        final String sign = signedAmount.signum() < 0 ? "-" : "";
        final String currencySymbol = transaction.getAsset().currency().getSymbol();
        return sign + currencySymbol + formatAmount(signedAmount.abs());
    }

    /**
     * Formats a signed amount with currency symbol.
     *
     * @param amount
     *            the amount to format
     * @param currency
     *            the currency
     * @return the formatted amount text
     */
    private String formatSignedAmount(final BigDecimal amount, final CurrencyUnit currency) {
        final String sign = amount.signum() < 0 ? "-" : "";
        return sign + currency.getSymbol() + formatAmount(amount.abs());
    }

    /**
     * Formats a ratio as percentage text.
     *
     * @param value
     *            the ratio value, where {@code 1.0} means 100%
     * @return the formatted percentage string
     */
    private String formatPercentage(final BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
    }

    /**
     * Computes budget usage percentage.
     *
     * @param spentAmount
     *            the spent amount
     * @param budgetLimit
     *            the budget limit
     * @return the percentage value
     */
    private double computeUsagePercentage(final BigDecimal spentAmount, final BigDecimal budgetLimit) {
        if (budgetLimit == null || budgetLimit.signum() == 0) {
            return 0.0;
        }
        return spentAmount.divide(budgetLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Returns whether the transaction should be rendered as positive.
     *
     * @param transaction
     *            the transaction to inspect
     * @return {@code true} if the amount is non-negative
     */
    private boolean isPositiveTransaction(final CashTransaction transaction) {
        return transaction.getAsset().amount().signum() >= 0;
    }

    /**
     * Resolves the color associated with a category name.
     *
     * @param categoryName
     *            the category name
     * @return the CSS hexadecimal color string
     */
    private String resolveCategoryColor(final String categoryName) {
        return dashboardFacade.getCategoryCatalog().getActiveCategories().stream()
                .filter(category -> category.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .map(Category::getColorHex)
                .map(this::toCssHex)
                .orElse(DEFAULT_CATEGORY_COLOR);
    }

    /**
     * Converts an {@link ARGBColor} into a CSS RGB hexadecimal string.
     *
     * @param color
     *            the color to convert
     * @return the CSS hexadecimal string
     */
    private String toCssHex(final ARGBColor color) {
        if (color == null) {
            return DEFAULT_CATEGORY_COLOR;
        }
        return String.format("#%02X%02X%02X", color.red(), color.green(), color.blue());
    }

    /**
     * Converts view-side filter input into model-side filter criteria.
     *
     * @param input
     *            the filter input
     * @return the filter criteria
     */
    private TransactionFilterCriteria toFilterCriteria(final TransactionFilterInput input) {
        final TransactionFilterInput safeInput = input == null
                ? new TransactionFilterInput(null, null, null, null, null, DEFAULT_SORT_ORDER_LABEL)
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

    /**
     * Parses a category type label coming from the view.
     *
     * @param rawType
     *            the raw type label
     * @return the parsed type or {@code null}
     */
    private CategoryType parseCategoryType(final String rawType) {
        final String normalized = normalizeBlank(rawType);
        if (normalized == null) {
            return null;
        }
        return CategoryType.valueOf(normalized.toUpperCase(java.util.Locale.ROOT).replace(' ', '_'));
    }

    /**
     * Parses a transaction sort-order label coming from the view.
     *
     * @param rawSortOrder
     *            the raw sort-order label
     * @return the parsed sort order
     */
    private TransactionSortOrder parseSortOrder(final String rawSortOrder) {
        final String normalized = normalizeBlank(rawSortOrder);
        if (normalized == null) {
            return TransactionSortOrder.NEWEST_FIRST;
        }

        return switch (normalized.toLowerCase(java.util.Locale.ROOT)) {
            case "newest first", "newest" -> TransactionSortOrder.NEWEST_FIRST;
            case "oldest first", "oldest" -> TransactionSortOrder.OLDEST_FIRST;
            case "highest amount", "highest amount first", "highest" ->
                    TransactionSortOrder.HIGHEST_AMOUNT_FIRST;
            default -> TransactionSortOrder.NEWEST_FIRST;
        };
    }

    /**
     * Normalizes blank strings to {@code null}.
     *
     * @param value
     *            the raw value
     * @return the trimmed value or {@code null}
     */
    private String normalizeBlank(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Returns the currently selected wallet.
     *
     * @return the selected wallet
     * @throws IllegalStateException
     *             if no wallet is currently selected
     */
    private CashAccount getRequiredCurrentWallet() {
        return cashAccountService.getCurrentWallet()
                .orElseThrow(() -> new IllegalStateException("No wallet is currently selected."));
    }

    /**
     * Finds a transaction by identifier inside the given wallet.
     *
     * @param wallet
     *            the wallet to inspect
     * @param transactionId
     *            the target transaction identifier
     * @return the matching transaction, if found
     */
    private Optional<CashTransaction> findTransactionById(
            final CashAccount wallet,
            final UUID transactionId) {
        return wallet.getHistory().getTransactions().stream()
                .filter(transaction -> transactionId.equals(transaction.getId()))
                .findFirst();
    }

    /**
     * Extracts currently open friend loans from the snapshot.
     *
     * @param snapshot
     *            the dashboard snapshot
     * @return the list of open friend loans
     */
    private List<FriendLoanSummary> getOpenFriendLoans(final DashboardSnapshot snapshot) {
        return snapshot.getFriendLoanSummaries().stream()
                .filter(summary -> summary.getNetBalance() != null)
                .filter(summary -> summary.getNetBalance().signum() > 0)
                .toList();
    }

}
