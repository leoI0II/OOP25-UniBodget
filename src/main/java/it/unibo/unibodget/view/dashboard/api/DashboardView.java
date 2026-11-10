package it.unibo.unibodget.view.dashboard.api;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.view.dashboard.impl.EditBudgetRequest;
import it.unibo.unibodget.view.dashboard.impl.NewCategoryRequest;
import it.unibo.unibodget.view.dashboard.impl.NewTransactionRequest;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState;

/**
 * Contract for the dashboard view.
 *
 * <p>The dashboard view is responsible for rendering UI state, forwarding user interactions
 * through bound actions, and presenting user input dialogs.</p>
 */
public interface DashboardView {

    /**
     * Binds the action callbacks used by this view.
     *
     * @param actions the action handlers exposed by the controller
     */
    void bindActions(DashboardViewActions actions);

    /**
     * Renders the given immutable dashboard UI state.
     *
     * @param state the state to render
     */
    void render(DashboardViewState state);

    /**
     * Displays an error message in the view.
     *
     * @param message the message to show
     */
    void showError(String message);

    /**
     * Prompts the user to create a new wallet.
     *
     * @param defaultCurrency the default currency for the new wallet
     * @param existingWallets the list of existing wallets for duplicate validation
     * @return an {@link Optional} containing the created {@link CashAccount}, or empty if cancelled
     */
    default Optional<CashAccount> promptCreateWallet(
            final FiatCurrency defaultCurrency,
            final List<CashAccount> existingWallets) {
        return Optional.empty();
    }

    /**
     * Prompts the user to create a new transaction.
     *
     * @param currency the wallet base currency
     * @param catalog the category catalog
     * @param openFriendLoans the list of active open friend loans
     * @param categoryCreator callback for creating custom categories
     * @return an {@link Optional} containing the {@link NewTransactionRequest}, or empty if cancelled
     */
    default Optional<NewTransactionRequest> promptCreateTransaction(
            final CurrencyUnit currency,
            final CategoryCatalog catalog,
            final List<FriendLoanSummary> openFriendLoans,
            final BiFunction<NewCategoryRequest, CategoryType, Optional<Category>> categoryCreator) {
        return Optional.empty();
    }

    /**
     * Prompts the user to edit an existing transaction.
     *
     * @param currency the wallet base currency
     * @param catalog the category catalog
     * @param openFriendLoans the list of active open friend loans
     * @param categoryCreator callback for creating custom categories
     * @param originalTransaction the transaction being edited
     * @return an {@link Optional} containing the updated {@link NewTransactionRequest}, or empty if cancelled
     */
    default Optional<NewTransactionRequest> promptEditTransaction(
            final CurrencyUnit currency,
            final CategoryCatalog catalog,
            final List<FriendLoanSummary> openFriendLoans,
            final BiFunction<NewCategoryRequest, CategoryType, Optional<Category>> categoryCreator,
            final CashTransaction originalTransaction) {
        return Optional.empty();
    }

    /**
     * Prompts the user to edit the monthly budget settings.
     *
     * @param currentSettings the current budget settings
     * @return an {@link Optional} containing the {@link EditBudgetRequest}, or empty if cancelled
     */
    default Optional<EditBudgetRequest> promptEditBudget(final DefaultBudgetSettings currentSettings) {
        return Optional.empty();
    }

    /**
     * Prompts the user to choose a destination file for CSV export.
     *
     * @param title the dialog title
     * @param defaultFileName the suggested file name
     * @return an {@link Optional} containing the target {@link File}, or empty if cancelled
     */
    default Optional<File> promptSaveCsv(final String title, final String defaultFileName) {
        return Optional.empty();
    }
}
