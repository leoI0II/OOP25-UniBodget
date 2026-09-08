package it.unibo.unibodget.view.dashboard.impl;

import java.util.Objects;
import java.util.UUID;

import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.view.dashboard.api.DashboardView;
import it.unibo.unibodget.view.dashboard.api.DashboardViewActions;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.BalanceCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.BudgetCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.CategoryBreakdownItemViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.ExpenseBreakdownCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.ExpenseRipartitionCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.FriendLoanCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.HeaderViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.InsightCardViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.SidebarFooterTotalViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.SidebarWalletItemViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionRowViewState;
import it.unibo.unibodget.view.dashboard.state.DashboardViewState.TransactionSectionViewState;
import it.unibo.unibodget.view.dashboard.state.TransactionFilterInput;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Default JavaFX implementation of the dashboard view.
 *
 * <p>
 * This view is responsible for rendering the dashboard UI from immutable
 * {@link DashboardViewState} instances and forwarding user interactions through
 * {@link DashboardViewActions}.
 * </p>
 *
 * <p>
 * The view includes:
 * </p>
 * <ul>
 * <li>a sidebar with navigation and wallet selection,</li>
 * <li>summary cards for balance, expense distribution, budget, friend loans,
 * and insights,</li>
 * <li>a transaction table with filters and row-level actions,</li>
 * <li>theme-aware styling for the full dashboard scene.</li>
 * </ul>
 */
public final class DefaultDashboardView extends BorderPane implements DashboardView {

    /**
     * Style class applied to standard dashboard cards.
     */
    private static final String CARD_STYLE_CLASS = "dashboard-card";

    /**
     * Preferred visible height for the scrollable insights area.
     */
    private static final double INSIGHTS_VIEWPORT_HEIGHT = 220.0;

    /**
     * Minimum visible height for the scrollable insights area.
     */
    private static final double INSIGHTS_MIN_VIEWPORT_HEIGHT = 180.0;

    /**
     * Horizontal padding used inside each insight card.
     */
    private static final double INSIGHT_CARD_HORIZONTAL_PADDING = 28.0;

    private DashboardViewActions actions;
    private boolean initialLoadTriggered;
    private boolean updatingFilters;

    private final VBox sidebarRoot;
    private final MenuButton navigationMenuButton;
    private final Label walletsSectionTitle;
    private final Label walletsCountLabel;
    private final Button createWalletButton;
    private final VBox walletListBox;
    private final ScrollPane walletScrollPane;
    private final Label totalFooterLabel;
    private final Label totalFooterAmountLabel;
    private final Label totalFooterCurrencyLabel;

    private final Label headerTitleLabel;
    private final Label headerWalletLabel;
    private final Label headerSubtitleLabel;

    private final Label balanceCardTitleLabel;
    private final Label balanceAmountLabel;
    private final Label balanceCurrencyLabel;

    private final Label ripartitionCardTitleLabel;
    private final Label ripartitionTotalLabel;
    private final PieChart ripartitionChart;
    private final Label ripartitionEmptyLabel;

    private final Label expenseCardTitleLabel;
    private final Label expenseTotalLabel;
    private final VBox expenseBreakdownBox;

    private final Label budgetCardTitleLabel;
    private final Label budgetStatusLabel;
    private final Label budgetProgressLabel;
    private final Label budgetThresholdLabel;
    private final Button editBudgetButton;

    private final StackPane budgetCirclePane;
    private final Circle budgetCircleTrack;
    private final Circle budgetCircleProgress;
    private final Label budgetCirclePercentageLabel;

    private final Label friendLoanTitleLabel;
    private final Label friendLoanTotalLabel;
    private final Label friendLoanMessageLabel;

    private final VBox insightsContainer;
    private final ScrollPane insightsScrollPane;

    private final Label transactionSectionTitleLabel;
    private final Button createTransactionButton;
    private final Button exportCsvButton;
    private final TableView<TransactionRowViewState> transactionTable;

    private final TextField transactionKeywordField;
    private final TextField transactionCategoryField;
    private final ComboBox<String> transactionCategoryTypeBox;
    private final DatePicker transactionFromDatePicker;
    private final DatePicker transactionToDatePicker;
    private final ComboBox<String> transactionSortOrderBox;
    private final Button clearFiltersButton;

    /**
     * Creates the dashboard view and initializes all UI components.
     */
    public DefaultDashboardView() {
        this.sidebarRoot = new VBox(16);
        this.navigationMenuButton = new MenuButton("\u2630");
        this.walletsSectionTitle = new Label();
        this.walletsCountLabel = new Label();
        this.createWalletButton = new Button("+ New Wallet");
        this.walletListBox = new VBox(10);
        this.walletScrollPane = new ScrollPane(this.walletListBox);
        this.totalFooterLabel = new Label();
        this.totalFooterAmountLabel = new Label();
        this.totalFooterCurrencyLabel = new Label();

        this.headerTitleLabel = new Label();
        this.headerWalletLabel = new Label();
        this.headerSubtitleLabel = new Label();

        this.balanceCardTitleLabel = new Label();
        this.balanceAmountLabel = new Label();
        this.balanceCurrencyLabel = new Label();

        this.ripartitionCardTitleLabel = new Label();
        this.ripartitionTotalLabel = new Label();
        this.ripartitionChart = new PieChart();
        this.ripartitionEmptyLabel = new Label();

        this.expenseCardTitleLabel = new Label();
        this.expenseTotalLabel = new Label();
        this.expenseBreakdownBox = new VBox(10);

        this.budgetCardTitleLabel = new Label();
        this.budgetStatusLabel = new Label();
        this.budgetProgressLabel = new Label();
        this.budgetThresholdLabel = new Label();
        this.editBudgetButton = new Button();

        this.budgetCirclePane = new StackPane();
        this.budgetCircleTrack = new Circle(46);
        this.budgetCircleProgress = new Circle(46);
        this.budgetCirclePercentageLabel = new Label();

        this.friendLoanTitleLabel = new Label();
        this.friendLoanTotalLabel = new Label();
        this.friendLoanMessageLabel = new Label();

        this.insightsContainer = new VBox(12);
        this.insightsScrollPane = new ScrollPane(this.insightsContainer);

        this.transactionSectionTitleLabel = new Label();
        this.createTransactionButton = new Button("+ New Transaction");
        this.exportCsvButton = new Button();
        this.transactionTable = new TableView<>();

        this.transactionKeywordField = new TextField();
        this.transactionCategoryField = new TextField();
        this.transactionCategoryTypeBox = new ComboBox<>();
        this.transactionFromDatePicker = new DatePicker();
        this.transactionToDatePicker = new DatePicker();
        this.transactionSortOrderBox = new ComboBox<>();
        this.clearFiltersButton = new Button("Clear filters");

        buildLayout();
        configureTable();
        configureActions();
        configureLifecycle();
        applyTheme();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindActions(final DashboardViewActions actions) {
        this.actions = Objects.requireNonNull(actions);
        triggerInitialLoadIfReady();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final DashboardViewState state) {
        Objects.requireNonNull(state);

        renderSidebar(state);
        renderHeader(state.header());
        renderBalanceCard(state.balanceCard());
        renderExpenseRipartitionCard(state.expenseRipartitionCard());
        renderExpenseCard(state.expenseBreakdownCard());
        renderBudgetCard(state.budgetCard());
        renderFriendLoanCard(state.friendLoanCard());
        renderInsights(state);
        renderTransactions(state.transactionSection());
        applyTheme();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showError(final String message) {
        this.headerSubtitleLabel.setText(message);
    }

    /**
     * Builds the overall dashboard layout.
     */
    private void buildLayout() {
        setPadding(new Insets(18));
        setPrefSize(1440, 900);

        buildSidebar();
        setLeft(this.sidebarRoot);

        final VBox centerContent = new VBox(18);
        centerContent.setPadding(new Insets(4, 0, 0, 18));

        final VBox headerBox = new VBox(4, this.headerTitleLabel, this.headerWalletLabel, this.headerSubtitleLabel);

        final GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(16);
        cardsGrid.setVgap(16);

        final ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setPercentWidth(46);

        final ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setPercentWidth(27);

        final ColumnConstraints thirdColumn = new ColumnConstraints();
        thirdColumn.setPercentWidth(27);

        cardsGrid.getColumnConstraints().addAll(firstColumn, secondColumn, thirdColumn);

        final VBox balanceContentBox = new VBox(10,
                this.balanceAmountLabel,
                this.balanceCurrencyLabel,
                this.createTransactionButton
        );
        balanceContentBox.setAlignment(Pos.CENTER);
        balanceContentBox.setFillWidth(true);
        VBox.setVgrow(balanceContentBox, Priority.ALWAYS);

        final VBox balanceCard = createCard(
                this.balanceCardTitleLabel,
                balanceContentBox
        );
        balanceCard.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(balanceContentBox, Priority.ALWAYS);

        final VBox ripartitionCard = createRipartitionCard();
        final VBox expenseCard = createExpenseCard();

        final VBox budgetTextBox = new VBox(
                8,
                this.budgetStatusLabel,
                this.budgetProgressLabel,
                this.budgetThresholdLabel,
                this.editBudgetButton
        );
        budgetTextBox.setAlignment(Pos.CENTER_LEFT);
        budgetTextBox.setFillWidth(true);

        final Region budgetSpacer = new Region();
        HBox.setHgrow(budgetSpacer, Priority.ALWAYS);

        final HBox budgetContent = new HBox(20, createBudgetCircle(), budgetTextBox, budgetSpacer);
        budgetContent.setAlignment(Pos.CENTER_LEFT);
        budgetContent.setMinHeight(140);

        final VBox budgetCard = createCard(
                this.budgetCardTitleLabel,
                budgetContent
        );

        final VBox friendLoanCard = createCard(
                this.friendLoanTitleLabel,
                this.friendLoanTotalLabel,
                this.friendLoanMessageLabel
        );

        final VBox insightsCard = createInsightsCard();

        cardsGrid.add(balanceCard, 0, 0);
        cardsGrid.add(ripartitionCard, 1, 0);
        cardsGrid.add(expenseCard, 2, 0);
        cardsGrid.add(budgetCard, 0, 1);
        cardsGrid.add(friendLoanCard, 1, 1);
        cardsGrid.add(insightsCard, 2, 1);

        final VBox transactionsCard = createTransactionsCard();
        VBox.setVgrow(transactionsCard, Priority.ALWAYS);

        centerContent.getChildren().addAll(headerBox, cardsGrid, transactionsCard);
        VBox.setVgrow(cardsGrid, Priority.NEVER);
        VBox.setVgrow(centerContent, Priority.ALWAYS);

        setCenter(centerContent);
    }

    /**
     * Builds the left sidebar.
     */
    private void buildSidebar() {
        this.sidebarRoot.setPadding(new Insets(18));
        this.sidebarRoot.setPrefWidth(320);
        this.sidebarRoot.setMinWidth(290);
        this.sidebarRoot.setMaxWidth(360);

        this.navigationMenuButton.setFocusTraversable(false);
        this.navigationMenuButton.setMaxWidth(Double.MAX_VALUE);

        final Region walletHeaderSpacer = new Region();
        HBox.setHgrow(walletHeaderSpacer, Priority.ALWAYS);

        final VBox walletTextBox = new VBox(4, this.walletsSectionTitle, this.walletsCountLabel);
        final HBox walletSectionHeader = new HBox(
                10,
                walletTextBox,
                walletHeaderSpacer,
                this.createWalletButton
        );
        walletSectionHeader.setAlignment(Pos.CENTER_LEFT);

        this.walletListBox.setFillWidth(true);
        this.walletListBox.setStyle("-fx-background-color: transparent;");

        this.walletScrollPane.setFitToWidth(true);
        this.walletScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.walletScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.walletScrollPane.setPrefViewportHeight(420);
        this.walletScrollPane.setStyle(
                "-fx-background: transparent;"
                + "-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
        );
        VBox.setVgrow(this.walletScrollPane, Priority.ALWAYS);

        final VBox totalFooterBox = createSidebarFooterCard(
                this.totalFooterLabel,
                this.totalFooterAmountLabel,
                this.totalFooterCurrencyLabel
        );

        final Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        this.sidebarRoot.getChildren().addAll(
                this.navigationMenuButton,
                walletSectionHeader,
                this.walletScrollPane,
                spacer,
                new Separator(),
                totalFooterBox
        );
    }

    /**
     * Creates the expense ripartition card.
     *
     * @return the configured card node
     */
    private VBox createRipartitionCard() {
        this.ripartitionChart.setLegendVisible(false);
        this.ripartitionChart.setLabelsVisible(false);
        this.ripartitionChart.setClockwise(true);
        this.ripartitionChart.setStartAngle(90);
        this.ripartitionChart.setAnimated(false);
        this.ripartitionChart.setMinHeight(170);
        this.ripartitionChart.setPrefHeight(170);
        this.ripartitionChart.setMaxHeight(170);
        this.ripartitionChart.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        this.ripartitionEmptyLabel.setWrapText(true);
        this.ripartitionEmptyLabel.setVisible(false);
        this.ripartitionEmptyLabel.setManaged(false);

        final StackPane chartPane = new StackPane(this.ripartitionChart, this.ripartitionEmptyLabel);
        chartPane.setMinHeight(170);
        chartPane.setPrefHeight(170);
        chartPane.setAlignment(Pos.CENTER);

        return createCard(this.ripartitionCardTitleLabel, this.ripartitionTotalLabel, chartPane);
    }

    /**
     * Creates the top expense card.
     *
     * @return the configured card node
     */
    private VBox createExpenseCard() {
        final VBox card = createCard(this.expenseCardTitleLabel, this.expenseTotalLabel);
        card.getChildren().add(this.expenseBreakdownBox);
        return card;
    }

    /**
     * Creates the circular budget progress node.
     *
     * @return the configured budget circle
     */
    private Node createBudgetCircle() {
        this.budgetCircleTrack.setFill(Color.TRANSPARENT);
        this.budgetCircleTrack.setStroke(Color.rgb(255, 255, 255, 0.14));
        this.budgetCircleTrack.setStrokeWidth(10);

        this.budgetCircleProgress.setFill(Color.TRANSPARENT);
        this.budgetCircleProgress.setStroke(Color.web("#7c5cff"));
        this.budgetCircleProgress.setStrokeWidth(10);
        this.budgetCircleProgress.setStrokeLineCap(StrokeLineCap.ROUND);
        this.budgetCircleProgress.setRotate(-90);

        final double circumference = 2 * Math.PI * this.budgetCircleProgress.getRadius();
        this.budgetCircleProgress.getStrokeDashArray().setAll(circumference, circumference);
        this.budgetCircleProgress.setStrokeDashOffset(circumference);

        this.budgetCirclePane.setMinSize(120, 120);
        this.budgetCirclePane.setPrefSize(120, 120);
        this.budgetCirclePane.setMaxSize(120, 120);
        this.budgetCirclePane.getChildren().setAll(
                this.budgetCircleTrack,
                this.budgetCircleProgress,
                this.budgetCirclePercentageLabel
        );

        return this.budgetCirclePane;
    }

    /**
     * Creates the insights card.
     *
     * @return the configured card node
     */
    private VBox createInsightsCard() {
        final Label title = new Label("Active Wallet Insights");
        styleLabel(title, 14, true, "rgba(255,255,255,0.88)");
        final VBox wrapper = createCard(title);

        this.insightsContainer.setFillWidth(true);

        this.insightsScrollPane.setFitToWidth(true);
        this.insightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.insightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.insightsScrollPane.setPannable(true);
        this.insightsScrollPane.setMinViewportHeight(INSIGHTS_MIN_VIEWPORT_HEIGHT);
        this.insightsScrollPane.setPrefViewportHeight(INSIGHTS_VIEWPORT_HEIGHT);
        this.insightsScrollPane.setMaxHeight(INSIGHTS_VIEWPORT_HEIGHT);
        this.insightsScrollPane.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-background: transparent;"
                + "-fx-border-color: transparent;"
        );

        wrapper.getChildren().add(this.insightsScrollPane);
        return wrapper;
    }

    /**
     * Creates the transaction filter bar.
     *
     * @return the filter bar node
     */
    private Node createTransactionFiltersBar() {
        this.transactionKeywordField.setPromptText("Keyword");
        this.transactionCategoryField.setPromptText("Category");
        this.transactionCategoryTypeBox.setPromptText("Type");
        this.transactionFromDatePicker.setPromptText("From");
        this.transactionToDatePicker.setPromptText("To");
        this.transactionSortOrderBox.setPromptText("Sort by");

        this.transactionCategoryTypeBox.getItems().setAll(
                "",
                "Income",
                "Expense",
                "Transfer",
                "Friend loan"
        );

        this.transactionSortOrderBox.getItems().setAll(
                "Newest first",
                "Oldest first",
                "Highest amount"
        );

        if (this.transactionSortOrderBox.getValue() == null) {
            this.transactionSortOrderBox.setValue("Newest first");
        }

        final HBox filtersBar = new HBox(
                10,
                this.transactionKeywordField,
                this.transactionCategoryField,
                this.transactionCategoryTypeBox,
                this.transactionFromDatePicker,
                this.transactionToDatePicker,
                this.transactionSortOrderBox,
                this.clearFiltersButton
        );
        filtersBar.setAlignment(Pos.CENTER_LEFT);
        return filtersBar;
    }

    /**
     * Creates the transaction section card.
     *
     * @return the configured transaction card
     */
    private VBox createTransactionsCard() {
        final HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.exportCsvButton.setFocusTraversable(false);

        topBar.getChildren().addAll(
                this.transactionSectionTitleLabel,
                spacer,
                this.exportCsvButton
        );

        final Node filtersBar = createTransactionFiltersBar();

        final VBox card = createCard(topBar, filtersBar);
        VBox.setVgrow(this.transactionTable, Priority.ALWAYS);
        this.transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        card.getChildren().add(this.transactionTable);
        return card;
    }

    /**
     * Creates a standard dashboard card.
     *
     * @param nodes
     *            the card content
     * @return the configured card
     */
    private VBox createCard(final Node... nodes) {
        final VBox box = new VBox(10);
        box.getStyleClass().add(CARD_STYLE_CLASS);
        box.setPadding(new Insets(18));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.07);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(255,255,255,0.10);"
        );
        box.getChildren().addAll(nodes);
        return box;
    }

    /**
     * Creates the emphasized footer card used at the bottom of the sidebar.
     *
     * @param nodes
     *            the footer card content
     * @return the configured footer card
     */
    private VBox createSidebarFooterCard(final Node... nodes) {
        final VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle(
                "-fx-background-color: rgba(20,28,44,0.92);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(120,190,255,0.18);"
                + "-fx-border-width: 1.2;"
        );
        box.getChildren().addAll(nodes);
        return box;
    }

    /**
     * Configures the transaction table columns and row actions.
     */
    private void configureTable() {
        final TableColumn<TransactionRowViewState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cell
                -> new ReadOnlyStringWrapper(cell.getValue().description()));

        final TableColumn<TransactionRowViewState, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cell
                -> new ReadOnlyStringWrapper(cell.getValue().dateText()));

        final TableColumn<TransactionRowViewState, TransactionRowViewState> categoryColumn
                = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cell
                -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(final TransactionRowViewState item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                final Circle dot = new Circle(4, Color.web(item.categoryColorHex()));
                final Label label = new Label(item.categoryText());
                label.setStyle("-fx-text-fill: white;");

                final HBox box = new HBox(6, dot, label);
                box.setAlignment(Pos.CENTER_LEFT);

                setText(null);
                setGraphic(box);
            }
        });

        final TableColumn<TransactionRowViewState, TransactionRowViewState> amountColumn
                = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cell
                -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(final TransactionRowViewState item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                final Label amountLabel = new Label(item.amountText());
                amountLabel.setStyle(
                        "-fx-text-fill: " + (item.positiveAmount() ? "#5EDC90" : "#FF7B7B") + ";"
                        + "-fx-font-weight: bold;"
                );

                setText(null);
                setGraphic(amountLabel);
            }
        });

        this.transactionTable.getColumns().clear();
        this.transactionTable.getColumns().add(descriptionColumn);
        this.transactionTable.getColumns().add(dateColumn);
        this.transactionTable.getColumns().add(categoryColumn);
        this.transactionTable.getColumns().add(amountColumn);

        this.transactionTable.setRowFactory(table -> {
            final TableRow<TransactionRowViewState> row = new TableRow<>();
            final MenuItem editItem = new MenuItem("Edit transaction");
            final MenuItem deleteItem = new MenuItem("Delete transaction");
            final ContextMenu contextMenu = new ContextMenu(editItem, deleteItem);

            editItem.setOnAction(event -> {
                final TransactionRowViewState item = row.getItem();
                if (item != null) {
                    final UUID transactionId = item.transactionId();
                    if (transactionId != null && this.actions != null) {
                        this.actions.onEditTransactionRequested(transactionId);
                    }
                }
            });

            deleteItem.setOnAction(event -> {
                final TransactionRowViewState item = row.getItem();
                if (item != null) {
                    final UUID transactionId = item.transactionId();
                    if (transactionId != null && this.actions != null) {
                        this.actions.onDeleteTransactionRequested(transactionId);
                    }
                }
            });

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null || newItem.transactionId() == null) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });

            return row;
        });
    }

    /**
     * Configures action handlers for all interactive controls.
     */
    private void configureActions() {
        this.createWalletButton.setOnAction(event -> {
            if (this.actions != null) {
                this.actions.onCreateWalletRequested();
            }
        });

        this.createTransactionButton.setOnAction(event -> {
            if (this.actions != null) {
                this.actions.onCreateTransactionRequested();
            }
        });

        this.exportCsvButton.setOnAction(event -> {
            if (this.actions != null) {
                this.actions.onExportTransactionsRequested();
            }
        });

        this.editBudgetButton.setOnAction(event -> {
            if (this.actions != null) {
                this.actions.onEditBudgetRequested();
            }
        });

        this.transactionKeywordField.textProperty().addListener((obs, oldValue, newValue)
                -> notifyTransactionFiltersChanged());
        this.transactionCategoryField.textProperty().addListener((obs, oldValue, newValue)
                -> notifyTransactionFiltersChanged());
        this.transactionCategoryTypeBox.valueProperty().addListener((obs, oldValue, newValue)
                -> notifyTransactionFiltersChanged());
        this.transactionFromDatePicker.valueProperty().addListener((obs, oldValue, newValue)
                -> notifyTransactionFiltersChanged());
        this.transactionToDatePicker.valueProperty().addListener((obs, oldValue, newValue)
                -> notifyTransactionFiltersChanged());
        this.transactionSortOrderBox.valueProperty().addListener((obs, oldValue, newValue)
                -> notifyTransactionFiltersChanged());

        this.clearFiltersButton.setOnAction(event -> {
            this.updatingFilters = true;
            try {
                this.transactionKeywordField.clear();
                this.transactionCategoryField.clear();
                this.transactionCategoryTypeBox.setValue("");
                this.transactionFromDatePicker.setValue(null);
                this.transactionToDatePicker.setValue(null);
                this.transactionSortOrderBox.setValue("Newest first");
            } finally {
                this.updatingFilters = false;
            }
            notifyTransactionFiltersChanged();
        });
    }

    /**
     * Configures view lifecycle hooks that depend on scene attachment.
     */
    private void configureLifecycle() {
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    final Node walletViewport = this.walletScrollPane.lookup(".viewport");
                    if (walletViewport != null) {
                        walletViewport.setStyle("-fx-background-color: transparent;");
                    }

                    final Node insightsViewport = this.insightsScrollPane.lookup(".viewport");
                    if (insightsViewport != null) {
                        insightsViewport.setStyle("-fx-background-color: transparent;");
                    }
                });
                triggerInitialLoadIfReady();
            }
        });
    }

    /**
     * Triggers the initial view-open callback once the view is fully ready.
     */
    private void triggerInitialLoadIfReady() {
        if (!this.initialLoadTriggered && getScene() != null && this.actions != null) {
            this.initialLoadTriggered = true;
            this.actions.onViewOpened();
        }
    }

    /**
     * Notifies the controller that transaction filters have changed.
     */
    private void notifyTransactionFiltersChanged() {
        if (this.actions == null || this.updatingFilters) {
            return;
        }

        this.actions.onTransactionFiltersChanged(new TransactionFilterInput(
                emptyToNull(this.transactionCategoryField.getText()),
                emptyToNull(this.transactionCategoryTypeBox.getValue()),
                this.transactionFromDatePicker.getValue(),
                this.transactionToDatePicker.getValue(),
                emptyToNull(this.transactionKeywordField.getText()),
                emptyToNull(this.transactionSortOrderBox.getValue()) == null
                ? "Newest first"
                : this.transactionSortOrderBox.getValue()
        ));
    }

    /**
     * Renders the sidebar.
     *
     * @param state
     *            the dashboard state
     */
    private void renderSidebar(final DashboardViewState state) {
        this.walletsSectionTitle.setText(state.sidebar().walletSection().title());
        this.walletsCountLabel.setText(state.sidebar().walletSection().walletCountText());

        renderNavigationMenu(state);
        renderWalletList(state);
        renderSidebarFooter(state.sidebar().totalFooter());
    }

    /**
     * Renders the navigation menu.
     *
     * @param state
     *            the dashboard state
     */
    private void renderNavigationMenu(final DashboardViewState state) {
        this.navigationMenuButton.setText("\u2630");
        this.navigationMenuButton.getItems().clear();

        state.sidebar().navigationMenu().items().forEach(item -> {
            final MenuItem menuItem = new MenuItem(item.label());
            menuItem.setDisable(!item.enabled());
            menuItem.setOnAction(event -> {
                if (this.actions != null) {
                    this.actions.onNavigationRequested(item.destination());
                }
            });
            this.navigationMenuButton.getItems().add(menuItem);
        });
    }

    /**
     * Renders the wallet list in the sidebar.
     *
     * @param state
     *            the dashboard state
     */
    private void renderWalletList(final DashboardViewState state) {
        this.walletListBox.getChildren().clear();

        for (final SidebarWalletItemViewState wallet : state.sidebar().walletSection().wallets()) {
            final Button walletButton = new Button();
            walletButton.setMaxWidth(Double.MAX_VALUE);
            walletButton.setAlignment(Pos.CENTER_LEFT);
            walletButton.setWrapText(true);
            walletButton.setText(wallet.walletName() + "\n" + wallet.balanceText());
            walletButton.setOnAction(event -> {
                if (this.actions != null) {
                    this.actions.onWalletSelected(wallet.walletId());
                }
            });

            if (wallet.selected()) {
                walletButton.setStyle(
                        "-fx-background-color: rgba(70, 130, 220, 0.38);"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-padding: 14 16 14 16;"
                        + "-fx-font-weight: bold;"
                        + "-fx-border-color: rgba(120,190,255,0.42);"
                        + "-fx-border-radius: 14;"
                );
            } else {
                walletButton.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.07);"
                        + "-fx-text-fill: rgba(255,255,255,0.92);"
                        + "-fx-background-radius: 14;"
                        + "-fx-padding: 14 16 14 16;"
                        + "-fx-border-color: rgba(255,255,255,0.08);"
                        + "-fx-border-radius: 14;"
                );
            }

            this.walletListBox.getChildren().add(walletButton);
        }
    }

    /**
     * Renders the sidebar footer totals.
     *
     * @param footer
     *            the footer view state
     */
    private void renderSidebarFooter(final SidebarFooterTotalViewState footer) {
        this.totalFooterLabel.setText(footer.label());
        this.totalFooterAmountLabel.setText(footer.convertedTotalText());
        this.totalFooterCurrencyLabel.setText("Base currency: " + footer.baseCurrencyText());
    }

    /**
     * Renders the dashboard header.
     *
     * @param state
     *            the header state
     */
    private void renderHeader(final HeaderViewState state) {
        this.headerTitleLabel.setText(state.title());
        this.headerWalletLabel.setText(state.activeWalletText());
        this.headerSubtitleLabel.setText(state.subtitle());
    }

    /**
     * Renders the balance card.
     *
     * @param state
     *            the balance card state
     */
    private void renderBalanceCard(final BalanceCardViewState state) {
        this.balanceCardTitleLabel.setText(state.title());
        this.balanceAmountLabel.setText(state.balanceText());
        this.balanceCurrencyLabel.setText("Currency: " + state.currencyText());
    }

    /**
     * Renders the expense ripartition card.
     *
     * @param state
     *            the expense ripartition state
     */
    private void renderExpenseRipartitionCard(final ExpenseRipartitionCardViewState state) {
        this.ripartitionCardTitleLabel.setText(state.title());
        this.ripartitionTotalLabel.setText(state.totalExpenseText());

        if (state.categories().isEmpty()) {
            this.ripartitionChart.getData().clear();
            this.ripartitionChart.setVisible(false);
            this.ripartitionChart.setManaged(false);
            this.ripartitionEmptyLabel.setVisible(true);
            this.ripartitionEmptyLabel.setManaged(true);
            this.ripartitionEmptyLabel.setText(state.emptyMessage());
            styleLabel(this.ripartitionEmptyLabel, 12, false, "rgba(255,255,255,0.55)");
            return;
        }

        this.ripartitionChart.setVisible(true);
        this.ripartitionChart.setManaged(true);
        this.ripartitionEmptyLabel.setVisible(false);
        this.ripartitionEmptyLabel.setManaged(false);

        this.ripartitionChart.setData(FXCollections.observableArrayList(
                state.categories().stream()
                        .map(item -> new PieChart.Data(
                        item.categoryName(),
                        Math.max(item.percentageValue(), 0.01)
                ))
                        .toList()
        ));

        for (int i = 0; i < this.ripartitionChart.getData().size(); i++) {
            final PieChart.Data data = this.ripartitionChart.getData().get(i);
            final CategoryBreakdownItemViewState item = state.categories().get(i);

            final String tooltipText = item.categoryName()
                    + "\nAmount: " + item.amountText()
                    + "\nShare: " + item.percentageText();

            final Tooltip tooltip = new Tooltip(tooltipText);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + item.colorHex() + ";");
                    Tooltip.install(newNode, tooltip);
                }
            });

            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + item.colorHex() + ";");
                Tooltip.install(data.getNode(), tooltip);
            }
        }
    }

    /**
     * Renders the top-expense breakdown card.
     *
     * @param state
     *            the expense breakdown state
     */
    private void renderExpenseCard(final ExpenseBreakdownCardViewState state) {
        this.expenseCardTitleLabel.setText(state.title());
        this.expenseTotalLabel.setText(state.totalExpenseText());
        this.expenseBreakdownBox.getChildren().clear();

        for (final CategoryBreakdownItemViewState item : state.categories()) {
            final HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            final Label rankLabel = new Label(item.rankText());
            rankLabel.setMinWidth(24);

            final Circle dot = new Circle(4, Color.web(item.colorHex()));
            final Label categoryLabel = new Label(item.categoryName());
            final Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            final Label amountLabel = new Label(item.amountText());
            final Label percentageLabel = new Label(item.percentageText());

            final VBox rightBox = new VBox(2, amountLabel, percentageLabel);
            rightBox.setAlignment(Pos.CENTER_RIGHT);

            styleLabel(rankLabel, 12, true, "rgba(255,255,255,0.70)");
            styleLabel(categoryLabel, 13, true, "white");
            styleLabel(amountLabel, 13, true, "white");
            styleLabel(percentageLabel, 11, false, "rgba(255,255,255,0.55)");

            row.getChildren().addAll(rankLabel, dot, categoryLabel, spacer, rightBox);
            this.expenseBreakdownBox.getChildren().add(row);
        }
    }

    /**
     * Renders the budget card.
     *
     * @param state
     *            the budget card state
     */
    private void renderBudgetCard(final BudgetCardViewState state) {
        this.budgetCardTitleLabel.setText(state.title());
        this.budgetStatusLabel.setText(state.statusLabel());
        this.budgetProgressLabel.setText(state.progressText());
        this.budgetThresholdLabel.setText(state.warningThresholdText());
        this.editBudgetButton.setText(state.actionLabel());

        final double percentage = Math.max(0.0, Math.min(100.0, state.usagePercentage()));
        final double circumference = 2 * Math.PI * this.budgetCircleProgress.getRadius();
        this.budgetCircleProgress.setStrokeDashOffset(circumference * (1.0 - (percentage / 100.0)));
        this.budgetCirclePercentageLabel.setText((int) Math.round(percentage) + "%");

        if (percentage < 80.0) {
            this.budgetCircleProgress.setStroke(Color.web("#5EDC90"));
        } else if (percentage < 100.0) {
            this.budgetCircleProgress.setStroke(Color.web("#F6C85F"));
        } else {
            this.budgetCircleProgress.setStroke(Color.web("#FF7B7B"));
        }
    }

    /**
     * Renders the friend loan card.
     *
     * @param state
     *            the friend loan card state
     */
    private void renderFriendLoanCard(final FriendLoanCardViewState state) {
        this.friendLoanTitleLabel.setText(state.title());
        this.friendLoanTotalLabel.setText(state.totalOutstandingText());
        this.friendLoanMessageLabel.setText(state.message());
        this.friendLoanMessageLabel.setWrapText(true);
    }

    /**
     * Renders the insights section.
     *
     * @param state
     *            the dashboard state
     */
    private void renderInsights(final DashboardViewState state) {
        this.insightsContainer.getChildren().clear();

        for (final InsightCardViewState insight : state.insightCards()) {
            final Label title = new Label(insight.title());
            final Label message = new Label(insight.message());
            final Label trend = new Label(insight.trend());

            title.setWrapText(true);
            message.setWrapText(true);
            trend.setWrapText(true);

            message.setMinHeight(Region.USE_PREF_SIZE);

            styleLabel(title, 13, true, "white");
            styleLabel(message, 12, false, "rgba(255,255,255,0.72)");
            styleLabel(trend, 11, true, "rgba(120,190,255,0.90)");

            final VBox card = new VBox(8, title, message, trend);
            card.setPadding(new Insets(14));
            card.setFillWidth(true);
            card.setMinWidth(0);
            card.setMaxWidth(Double.MAX_VALUE);
            card.setPrefHeight(Region.USE_COMPUTED_SIZE);
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.06);"
                    + "-fx-background-radius: 14;"
                    + "-fx-border-radius: 14;"
                    + "-fx-border-color: rgba(255,255,255,0.10);"
            );

            title.maxWidthProperty().bind(card.widthProperty().subtract(INSIGHT_CARD_HORIZONTAL_PADDING));
            message.maxWidthProperty().bind(card.widthProperty().subtract(INSIGHT_CARD_HORIZONTAL_PADDING));
            trend.maxWidthProperty().bind(card.widthProperty().subtract(INSIGHT_CARD_HORIZONTAL_PADDING));

            this.insightsContainer.getChildren().add(card);
        }
    }

    /**
     * Renders the transaction section.
     *
     * @param state
     *            the transaction section state
     */
    private void renderTransactions(final TransactionSectionViewState state) {
        this.transactionSectionTitleLabel.setText(state.title());
        this.exportCsvButton.setText(state.exportButtonText());

        final TransactionFilterInput filterInput = state.table().filterInput();

        this.updatingFilters = true;
        try {
            this.transactionKeywordField.setText(filterInput.keyword() == null ? "" : filterInput.keyword());
            this.transactionCategoryField.setText(filterInput.categoryName() == null ? "" : filterInput.categoryName());
            this.transactionCategoryTypeBox.setValue(filterInput.categoryType() == null ? "" : filterInput.categoryType());
            this.transactionFromDatePicker.setValue(filterInput.fromDate());
            this.transactionToDatePicker.setValue(filterInput.toDate());
            this.transactionSortOrderBox.setValue(
                    filterInput.sortOrder() == null ? "Newest first" : filterInput.sortOrder()
            );
        } finally {
            this.updatingFilters = false;
        }

        this.transactionTable.getItems().setAll(state.table().rows());
        this.transactionTable.setPlaceholder(new Label(state.table().emptyMessage()));
    }

    /**
     * Applies the current theme to the dashboard.
     */
    private void applyTheme() {
        final Theme theme = ThemeManager.getTheme();

        final String buttonHex = toRgbaCss(theme.getButtonColor());

        final String strongText = "white";
        final String mediumText = "rgba(255,255,255,0.88)";
        final String softText = "rgba(255,255,255,0.68)";
        final String faintText = "rgba(255,255,255,0.50)";

        setStyle(
                "-fx-background-color: "
                + "linear-gradient(to bottom right, rgba(10,12,20,0.98), rgba(24,28,42,0.96)), "
                + "linear-gradient(to bottom right, " + rgba(theme.getPrimaryColor(), 0.10)
                + ", rgba(10,12,20,0.02));"
                + "-fx-font-family: '" + theme.getFontFamily() + "';"
                + "-fx-font-size: " + theme.getFontSize() + "px;"
        );

        this.sidebarRoot.setStyle(
                "-fx-background-color: rgba(4,8,16,0.96);"
                + "-fx-background-radius: 22;"
                + "-fx-border-radius: 22;"
                + "-fx-border-color: rgba(255,255,255,0.08);"
        );

        this.navigationMenuButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);"
                + "-fx-text-fill: " + strongText + ";"
                + "-fx-background-radius: 14;"
                + "-fx-padding: 10 14 10 14;"
                + "-fx-border-color: rgba(255,255,255,0.06);"
                + "-fx-border-radius: 14;"
        );

        this.createWalletButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.90);"
                + "-fx-text-fill: #111111;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 8 12 8 12;"
                + "-fx-font-weight: bold;"
        );

        this.createTransactionButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);"
                + "-fx-text-fill: #111111;"
                + "-fx-background-radius: 14;"
                + "-fx-padding: 14 22 14 22;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 14px;"
        );

        this.exportCsvButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10);"
                + "-fx-text-fill: " + strongText + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 10 14 10 14;"
                + "-fx-border-color: rgba(255,255,255,0.10);"
                + "-fx-border-radius: 12;"
        );

        this.editBudgetButton.setFocusTraversable(false);
        this.editBudgetButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10);"
                + "-fx-text-fill: " + strongText + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 10 16 10 16;"
                + "-fx-border-color: rgba(255,255,255,0.10);"
                + "-fx-border-radius: 12;"
                + "-fx-font-size: 13px;"
        );

        this.clearFiltersButton.setFocusTraversable(false);
        this.clearFiltersButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10);"
                + "-fx-text-fill: " + strongText + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 8 12 8 12;"
                + "-fx-border-color: rgba(255,255,255,0.10);"
                + "-fx-border-radius: 12;"
        );

        final String filterFieldStyle
                = "-fx-background-color: rgba(255,255,255,0.10);"
                + "-fx-text-fill: white;"
                + "-fx-prompt-text-fill: " + faintText + ";"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: rgba(255,255,255,0.10);";

        this.transactionKeywordField.setStyle(filterFieldStyle);
        this.transactionCategoryField.setStyle(filterFieldStyle);
        this.transactionCategoryTypeBox.setStyle(filterFieldStyle);
        this.transactionFromDatePicker.setStyle(filterFieldStyle);
        this.transactionToDatePicker.setStyle(filterFieldStyle);
        this.transactionSortOrderBox.setStyle(filterFieldStyle);

        styleLabel(this.headerTitleLabel, 28, true, strongText);
        styleLabel(this.headerWalletLabel, 14, true, mediumText);
        styleLabel(this.headerSubtitleLabel, 13, false, softText);

        styleLabel(this.walletsSectionTitle, 16, true, strongText);
        styleLabel(this.walletsCountLabel, 12, false, "rgba(255,255,255,0.74)");

        styleLabel(this.balanceCardTitleLabel, 14, true, mediumText);
        styleLabel(this.balanceAmountLabel, 42, true, strongText);
        styleLabel(this.balanceCurrencyLabel, 14, false, softText);

        styleLabel(this.ripartitionCardTitleLabel, 14, true, mediumText);
        styleLabel(this.ripartitionTotalLabel, 24, true, strongText);

        styleLabel(this.expenseCardTitleLabel, 14, true, mediumText);
        styleLabel(this.expenseTotalLabel, 28, true, strongText);

        styleLabel(this.budgetCardTitleLabel, 14, true, mediumText);
        styleLabel(this.budgetStatusLabel, 30, true, strongText);
        styleLabel(this.budgetProgressLabel, 16, false, strongText);
        styleLabel(this.budgetThresholdLabel, 13, false, softText);
        styleLabel(this.budgetCirclePercentageLabel, 18, true, strongText);

        styleLabel(this.friendLoanTitleLabel, 14, true, mediumText);
        styleLabel(this.friendLoanTotalLabel, 28, true, strongText);
        styleLabel(this.friendLoanMessageLabel, 13, false, softText);

        styleLabel(this.transactionSectionTitleLabel, 16, true, strongText);
        styleLabel(this.totalFooterLabel, 13, false, "rgba(255,255,255,0.78)");
        styleLabel(this.totalFooterAmountLabel, 22, true, strongText);
        styleLabel(this.totalFooterCurrencyLabel, 12, false, "rgba(255,255,255,0.70)");

        this.transactionTable.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);"
                + "-fx-control-inner-background: rgba(255,255,255,0.05);"
                + "-fx-table-cell-border-color: rgba(255,255,255,0.08);"
                + "-fx-text-background-color: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: rgba(255,255,255,0.10);"
                + "-fx-border-radius: 14;"
                + "-fx-padding: 0;"
        );

        Platform.runLater(() -> {
            final Node headerBackground = this.transactionTable.lookup(".column-header-background");
            if (headerBackground != null) {
                headerBackground.setStyle(
                        "-fx-background-color: rgba(18,22,34,0.96);"
                        + "-fx-background-radius: 14 14 0 0;"
                );
            }

            final Node filler = this.transactionTable.lookup(".filler");
            if (filler != null) {
                filler.setStyle("-fx-background-color: rgba(18,22,34,0.96);");
            }

            this.transactionTable.lookupAll(".column-header").forEach(header
                    -> header.setStyle(
                            "-fx-background-color: transparent;"
                            + "-fx-border-color: rgba(255,255,255,0.08);"
                            + "-fx-border-width: 0 0 1 0;"
                    )
            );

            this.transactionTable.lookupAll(".column-header .label").forEach(label
                    -> label.setStyle(
                            "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
                    )
            );

            final Node walletViewport = this.walletScrollPane.lookup(".viewport");
            if (walletViewport != null) {
                walletViewport.setStyle("-fx-background-color: transparent;");
            }

            final Node insightsViewport = this.insightsScrollPane.lookup(".viewport");
            if (insightsViewport != null) {
                insightsViewport.setStyle("-fx-background-color: transparent;");
            }
        });
    }

    /**
     * Applies a simple style to a label.
     *
     * @param label
     *            the label to style
     * @param size
     *            the font size in pixels
     * @param bold
     *            whether the font should be bold
     * @param color
     *            the CSS text color
     */
    private void styleLabel(final Label label, final int size, final boolean bold, final String color) {
        label.setStyle(
                "-fx-text-fill: " + color + ";"
                + "-fx-font-size: " + size + "px;"
                + "-fx-font-weight: " + (bold ? "bold" : "normal") + ";"
        );
    }

    /**
     * Converts blank strings to {@code null}.
     *
     * @param value
     *            the input string
     * @return the trimmed value, or {@code null} if blank
     */
    private String emptyToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Converts an ARGB color to an RGBA CSS string using its embedded alpha.
     *
     * @param color
     *            the color to convert
     * @return the CSS rgba string
     */
    private String toRgbaCss(final ARGBColor color) {
        final double opacity = color.alpha() / 255.0;
        return "rgba(" + color.red() + "," + color.green() + "," + color.blue() + "," + opacity + ")";
    }

    /**
     * Converts an ARGB color to an RGBA CSS string using an explicit opacity.
     *
     * @param color
     *            the source color
     * @param opacity
     *            the opacity to apply
     * @return the CSS rgba string
     */
    private String rgba(final ARGBColor color, final double opacity) {
        return "rgba(" + color.red() + "," + color.green() + "," + color.blue() + "," + opacity + ")";
    }
}