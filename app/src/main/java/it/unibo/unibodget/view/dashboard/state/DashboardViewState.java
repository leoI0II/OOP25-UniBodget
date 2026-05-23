package it.unibo.unibodget.view.dashboard.state;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable UI state for the dashboard screen.
 *
 * <p>
 * This object contains all information needed by the dashboard view to render
 * the sidebar, navigation menu, summary cards, insights, and transaction
 * table.
 * </p>
 *
 * @param sidebar
 *            the complete sidebar state
 * @param header
 *            the dashboard header state
 * @param balanceCard
 *            the current wallet balance card
 * @param expenseRipartitionCard
 *            the full expense ripartition card containing all category slices
 * @param expenseBreakdownCard
 *            the ranked expense breakdown card
 * @param budgetCard
 *            the monthly budget card
 * @param friendLoanCard
 *            the friend-loan summary card
 * @param insightCards
 *            the wallet insight cards
 * @param transactionSection
 *            the transactions section state
 */
public record DashboardViewState(
        SidebarViewState sidebar,
        HeaderViewState header,
        BalanceCardViewState balanceCard,
        ExpenseRipartitionCardViewState expenseRipartitionCard,
        ExpenseBreakdownCardViewState expenseBreakdownCard,
        BudgetCardViewState budgetCard,
        FriendLoanCardViewState friendLoanCard,
        List<InsightCardViewState> insightCards,
        TransactionSectionViewState transactionSection) {

    /**
     * Compact canonical constructor with null-safety.
     *
     * @param sidebar
     *            sidebar state
     * @param header
     *            header state
     * @param balanceCard
     *            balance card state
     * @param expenseRipartitionCard
     *            ripartition card state
     * @param expenseBreakdownCard
     *            ranked expense card state
     * @param budgetCard
     *            budget card state
     * @param friendLoanCard
     *            friend loan card state
     * @param insightCards
     *            insight card collection
     * @param transactionSection
     *            transaction section state
     */
    public DashboardViewState {
        Objects.requireNonNull(sidebar);
        Objects.requireNonNull(header);
        Objects.requireNonNull(balanceCard);
        Objects.requireNonNull(expenseRipartitionCard);
        Objects.requireNonNull(expenseBreakdownCard);
        Objects.requireNonNull(budgetCard);
        Objects.requireNonNull(friendLoanCard);
        insightCards = List.copyOf(Objects.requireNonNull(insightCards));
        Objects.requireNonNull(transactionSection);
    }

    /**
     * Sidebar state.
     *
     * @param navigationMenu
     *            the navigation menu state
     * @param walletSection
     *            the wallet list section state
     * @param totalFooter
     *            the footer total state
     */
    public record SidebarViewState(
            NavigationMenuViewState navigationMenu,
            WalletSectionViewState walletSection,
            SidebarFooterTotalViewState totalFooter) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param navigationMenu
         *            navigation menu state
         * @param walletSection
         *            wallet section state
         * @param totalFooter
         *            footer state
         */
        public SidebarViewState {
            Objects.requireNonNull(navigationMenu);
            Objects.requireNonNull(walletSection);
            Objects.requireNonNull(totalFooter);
        }
    }

    /**
     * Navigation menu state.
     *
     * @param items
     *            the available navigation items
     */
    public record NavigationMenuViewState(
            List<NavigationItemViewState> items) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param items
         *            navigation items
         */
        public NavigationMenuViewState {
            items = List.copyOf(Objects.requireNonNull(items));
        }
    }

    /**
     * Single navigation item state.
     *
     * @param label
     *            the item label
     * @param destination
     *            the destination triggered by the item
     * @param enabled
     *            whether the item can be activated
     */
    public record NavigationItemViewState(
            String label,
            DashboardDestination destination,
            boolean enabled) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param label
         *            item label
         * @param destination
         *            item destination
         * @param enabled
         *            enabled flag
         */
        public NavigationItemViewState {
            Objects.requireNonNull(label);
            Objects.requireNonNull(destination);
        }
    }

    /**
     * Wallet section state shown in the sidebar.
     *
     * @param title
     *            section title
     * @param walletCountText
     *            wallet count label
     * @param wallets
     *            the wallet items
     */
    public record WalletSectionViewState(
            String title,
            String walletCountText,
            List<SidebarWalletItemViewState> wallets) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            section title
         * @param walletCountText
         *            wallet count text
         * @param wallets
         *            wallet item list
         */
        public WalletSectionViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(walletCountText);
            wallets = List.copyOf(Objects.requireNonNull(wallets));
        }
    }

    /**
     * Sidebar wallet item state.
     *
     * @param walletId
     *            wallet identifier
     * @param walletName
     *            wallet display name
     * @param balanceText
     *            formatted balance text
     * @param selected
     *            whether the wallet is currently selected
     */
    public record SidebarWalletItemViewState(
            UUID walletId,
            String walletName,
            String balanceText,
            boolean selected) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param walletId
         *            wallet identifier
         * @param walletName
         *            wallet name
         * @param balanceText
         *            balance text
         * @param selected
         *            selected flag
         */
        public SidebarWalletItemViewState {
            Objects.requireNonNull(walletId);
            Objects.requireNonNull(walletName);
            Objects.requireNonNull(balanceText);
        }
    }

    /**
     * Sidebar footer total state.
     *
     * @param label
     *            footer label
     * @param convertedTotalText
     *            converted total amount
     * @param baseCurrencyText
     *            base currency label
     */
    public record SidebarFooterTotalViewState(
            String label,
            String convertedTotalText,
            String baseCurrencyText) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param label
         *            footer label
         * @param convertedTotalText
         *            converted total text
         * @param baseCurrencyText
         *            base currency text
         */
        public SidebarFooterTotalViewState {
            Objects.requireNonNull(label);
            Objects.requireNonNull(convertedTotalText);
            Objects.requireNonNull(baseCurrencyText);
        }
    }

    /**
     * Header state for the dashboard.
     *
     * @param title
     *            header title
     * @param activeWalletText
     *            active wallet label
     * @param subtitle
     *            secondary subtitle
     */
    public record HeaderViewState(
            String title,
            String activeWalletText,
            String subtitle) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            title
         * @param activeWalletText
         *            wallet label
         * @param subtitle
         *            subtitle
         */
        public HeaderViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(activeWalletText);
            Objects.requireNonNull(subtitle);
        }
    }

    /**
     * Balance card state.
     *
     * @param title
     *            card title
     * @param balanceText
     *            formatted balance
     * @param currencyText
     *            wallet currency text
     */
    public record BalanceCardViewState(
            String title,
            String balanceText,
            String currencyText) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            title
         * @param balanceText
         *            balance text
         * @param currencyText
         *            currency text
         */
        public BalanceCardViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(balanceText);
            Objects.requireNonNull(currencyText);
        }
    }

    /**
     * Card state for the expense ripartition visualization.
     *
     * @param title
     *            card title
     * @param totalExpenseText
     *            total expense text displayed near the chart
     * @param categories
     *            all category slices used by the chart
     * @param emptyMessage
     *            fallback message when there is no expense data
     */
    public record ExpenseRipartitionCardViewState(
            String title,
            String totalExpenseText,
            List<CategoryBreakdownItemViewState> categories,
            String emptyMessage) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            card title
         * @param totalExpenseText
         *            total expense text
         * @param categories
         *            full category list
         * @param emptyMessage
         *            empty-state message
         */
        public ExpenseRipartitionCardViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(totalExpenseText);
            categories = List.copyOf(Objects.requireNonNull(categories));
            Objects.requireNonNull(emptyMessage);
        }
    }

    /**
     * Card state for the ranked expense breakdown.
     *
     * @param title
     *            card title
     * @param totalExpenseText
     *            formatted total expense text
     * @param categories
     *            ranked categories, typically the top few
     */
    public record ExpenseBreakdownCardViewState(
            String title,
            String totalExpenseText,
            List<CategoryBreakdownItemViewState> categories) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            card title
         * @param totalExpenseText
         *            total expense text
         * @param categories
         *            ranked categories
         */
        public ExpenseBreakdownCardViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(totalExpenseText);
            categories = List.copyOf(Objects.requireNonNull(categories));
        }
    }

    /**
     * Single category breakdown row or chart slice state.
     *
     * @param rankText
     *            rank text for list-based rendering, may be empty for chart-only
     * @param categoryName
     *            category display name
     * @param amountText
     *            formatted category amount
     * @param percentageText
     *            formatted percentage text
     * @param percentageValue
     *            raw percentage value in the 0-100 range
     * @param colorHex
     *            category color in CSS hexadecimal form
     */
    public record CategoryBreakdownItemViewState(
            String rankText,
            String categoryName,
            String amountText,
            String percentageText,
            double percentageValue,
            String colorHex) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param rankText
         *            rank label
         * @param categoryName
         *            category name
         * @param amountText
         *            amount text
         * @param percentageText
         *            percentage text
         * @param percentageValue
         *            percentage numeric value
         * @param colorHex
         *            category color
         */
        public CategoryBreakdownItemViewState {
            Objects.requireNonNull(rankText);
            Objects.requireNonNull(categoryName);
            Objects.requireNonNull(amountText);
            Objects.requireNonNull(percentageText);
            Objects.requireNonNull(colorHex);
        }
    }

    /**
     * Monthly budget card state.
     *
     * @param title
     *            card title
     * @param statusLabel
     *            budget status label
     * @param progressText
     *            formatted spending progress text
     * @param warningThresholdText
     *            threshold description
     * @param actionLabel
     *            action button label
     * @param usagePercentage
     *            percentage of the budget already used, in the 0-100 range
     * @param spentText
     *            formatted spent amount
     * @param limitText
     *            formatted budget limit amount
     * @param remainingText
     *            formatted remaining budget amount
     */
    public record BudgetCardViewState(
            String title,
            String statusLabel,
            String progressText,
            String warningThresholdText,
            String actionLabel,
            double usagePercentage,
            String spentText,
            String limitText,
            String remainingText) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            title
         * @param statusLabel
         *            status label
         * @param progressText
         *            progress text
         * @param warningThresholdText
         *            threshold text
         * @param actionLabel
         *            action label
         * @param usagePercentage
         *            usage percentage
         * @param spentText
         *            spent text
         * @param limitText
         *            limit text
         * @param remainingText
         *            remaining text
         */
        public BudgetCardViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(statusLabel);
            Objects.requireNonNull(progressText);
            Objects.requireNonNull(warningThresholdText);
            Objects.requireNonNull(actionLabel);
            Objects.requireNonNull(spentText);
            Objects.requireNonNull(limitText);
            Objects.requireNonNull(remainingText);
        }
    }

    /**
     * Friend-loan summary card state.
     *
     * @param title
     *            card title
     * @param totalOutstandingText
     *            total outstanding amount
     * @param message
     *            explanatory message
     */
    public record FriendLoanCardViewState(
            String title,
            String totalOutstandingText,
            String message) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            title
         * @param totalOutstandingText
         *            outstanding total text
         * @param message
         *            message
         */
        public FriendLoanCardViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(totalOutstandingText);
            Objects.requireNonNull(message);
        }
    }

    /**
     * Small insight card state.
     *
     * @param title
     *            insight title
     * @param message
     *            insight message
     * @param trend
     *            trend or callout text
     */
    public record InsightCardViewState(
            String title,
            String message,
            String trend) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            title
         * @param message
         *            message
         * @param trend
         *            trend text
         */
        public InsightCardViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(message);
            Objects.requireNonNull(trend);
        }
    }

    /**
     * Transactions section state.
     *
     * @param title
     *            section title
     * @param exportButtonText
     *            export button label
     * @param table
     *            transaction table state
     */
    public record TransactionSectionViewState(
            String title,
            String exportButtonText,
            TransactionTableViewState table) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param title
         *            title
         * @param exportButtonText
         *            export button text
         * @param table
         *            table state
         */
        public TransactionSectionViewState {
            Objects.requireNonNull(title);
            Objects.requireNonNull(exportButtonText);
            Objects.requireNonNull(table);
        }
    }

    /**
     * Transaction table state.
     *
     * @param rows
     *            rendered rows
     * @param emptyMessage
     *            placeholder text when no rows are available
     * @param filterInput
     *            the currently applied filter input
     */
    public record TransactionTableViewState(
            List<TransactionRowViewState> rows,
            String emptyMessage,
            TransactionFilterInput filterInput) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param rows
         *            table rows
         * @param emptyMessage
         *            empty message
         * @param filterInput
         *            active filters
         */
        public TransactionTableViewState {
            rows = List.copyOf(Objects.requireNonNull(rows));
            Objects.requireNonNull(emptyMessage);
            Objects.requireNonNull(filterInput);
        }
    }

    /**
     * Transaction row state.
     *
     * @param description
     *            transaction description
     * @param dateText
     *            formatted date text
     * @param categoryText
     *            category display text
     * @param amountText
     *            formatted amount text
     * @param categoryColorHex
     *            category badge color in CSS hexadecimal form
     * @param positiveAmount
     *            true if the amount should be rendered as positive
     */
    public record TransactionRowViewState(
            String description,
            String dateText,
            String categoryText,
            String amountText,
            String categoryColorHex,
            boolean positiveAmount) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param description
         *            description
         * @param dateText
         *            date text
         * @param categoryText
         *            category text
         * @param amountText
         *            amount text
         * @param categoryColorHex
         *            category color
         * @param positiveAmount
         *            sign styling hint
         */
        public TransactionRowViewState {
            Objects.requireNonNull(description);
            Objects.requireNonNull(dateText);
            Objects.requireNonNull(categoryText);
            Objects.requireNonNull(amountText);
            Objects.requireNonNull(categoryColorHex);
        }
    }

    /**
     * Settings summary state.
     *
     * <p>
     * This type is kept because the current controller imports it, even if the
     * current view does not actively render it yet.
     * </p>
     *
     * @param baseCurrencyText
     *            the configured base currency label
     * @param themeText
     *            the configured theme label
     */
    public record SettingsSummaryViewState(
            String baseCurrencyText,
            String themeText) {

        /**
         * Compact canonical constructor with null-safety.
         *
         * @param baseCurrencyText
         *            base currency text
         * @param themeText
         *            theme text
         */
        public SettingsSummaryViewState {
            Objects.requireNonNull(baseCurrencyText);
            Objects.requireNonNull(themeText);
        }
    }
}