package it.unibo.unibodget.view.dashboard.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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

    private static final String STYLE_BUTTON_SECONDARY = "button-secondary";
    private static final String STYLE_TRANSPARENT_BG = "-fx-background-color: transparent;";
    private static final String ORDER_NEWEST_FIRST = "Newest first";
    private static final String COLOR_WHITE = "white";
    private static final String COLOR_CHARCOAL_TEXT = "#1a1e28";

    /**
     * Preferred visible height for the scrollable insights area.
     */
    private static final double INSIGHTS_VIEWPORT_HEIGHT = 220.0;

    /**
     * Minimum visible height for the scrollable insights area.
     */
    private static final double INSIGHTS_MIN_VIEWPORT_HEIGHT = 180.0;
    private static final double BUDGET_CIRCLE_RADIUS = 64.0;
    private static final double BUDGET_CIRCLE_STROKE_WIDTH = 12.0;
    private static final int INSIGHTS_GAP = 12;
    private static final int INSIGHTS_TILE_GAP = 6;
    private static final int ROOT_PADDING = 18;
    private static final double PREF_WIDTH = 1440.0;
    private static final double PREF_HEIGHT = 900.0;
    private static final double COL_PERCENT_FIRST = 46.0;
    private static final double COL_PERCENT_SECOND = 27.0;
    private static final double COL_PERCENT_THIRD = 27.0;
    private static final double BUDGET_CONTENT_MIN_HEIGHT = 160.0;
    private static final double CHART_HEIGHT = 170.0;
    private static final double CIRCLE_ROTATE_ANGLE = -90.0;
    private static final double BUDGET_CIRCLE_BOX_SIZE = 150.0;
    private static final int CARD_PADDING = 18;
    private static final double MIN_PIE_SLICE_PERCENTAGE = 0.01;
    private static final double RANK_LABEL_MIN_WIDTH = 24.0;
    private static final double BUDGET_WARN_PERCENT = 80.0;

    private static final int FONT_SIZE_SMALL = 11;
    private static final int FONT_SIZE_MUTED = 12;
    private static final int FONT_SIZE_CAPTION = 13;
    private static final int FONT_SIZE_BODY = 14;
    private static final int FONT_SIZE_SUBTITLE = 16;
    private static final int FONT_SIZE_STAT = 24;
    private static final int FONT_SIZE_HEADER = 28;
    private static final int FONT_SIZE_STATUS = 30;
    private static final int FONT_SIZE_HERO = 42;

    private DashboardViewActions actions;
    private boolean initialLoadTriggered;
    private final AtomicBoolean updatingFilters = new AtomicBoolean();

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
    private final Label insightsCardTitleLabel;
    private List<InsightCardViewState> currentInsights = List.of();
    private ExpenseBreakdownCardViewState currentExpenseBreakdown;

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
        this.budgetCircleTrack = new Circle(BUDGET_CIRCLE_RADIUS);
        this.budgetCircleProgress = new Circle(BUDGET_CIRCLE_RADIUS);
        this.budgetCirclePercentageLabel = new Label();

        this.friendLoanTitleLabel = new Label();
        this.friendLoanTotalLabel = new Label();
        this.friendLoanMessageLabel = new Label();

        this.insightsContainer = new VBox(INSIGHTS_GAP);
        this.insightsScrollPane = new ScrollPane(this.insightsContainer);
        this.insightsCardTitleLabel = new Label("Active Wallet Insights");

        this.transactionSectionTitleLabel = new Label();
        this.createTransactionButton = new Button("+ New Transaction");
        this.createTransactionButton.getStyleClass().addAll("button-primary", "hero-transaction-button");
        this.createTransactionButton.setFocusTraversable(false);
        this.exportCsvButton = new Button();
        this.exportCsvButton.getStyleClass().add(STYLE_BUTTON_SECONDARY);
        this.transactionTable = new TableView<>();

        this.transactionKeywordField = new TextField();
        this.transactionCategoryField = new TextField();
        this.transactionCategoryTypeBox = new ComboBox<>();
        this.transactionFromDatePicker = new DatePicker();
        this.transactionToDatePicker = new DatePicker();
        this.transactionSortOrderBox = new ComboBox<>();
        this.clearFiltersButton = new Button("Clear filters");
        this.clearFiltersButton.getStyleClass().add(STYLE_BUTTON_SECONDARY);
        this.editBudgetButton.getStyleClass().add(STYLE_BUTTON_SECONDARY);

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
    public void bindActions(final DashboardViewActions viewActions) {
        this.actions = Objects.requireNonNull(viewActions);
        triggerInitialLoadIfReady();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final DashboardViewState state) {
        Objects.requireNonNull(state);

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
        setPadding(new Insets(ROOT_PADDING));
        setPrefSize(PREF_WIDTH, PREF_HEIGHT);

        final VBox centerContent = new VBox(ROOT_PADDING);
        centerContent.setPadding(new Insets(4, 0, 0, 0));

        final VBox headerBox = new VBox(4, this.headerTitleLabel, this.headerWalletLabel, this.headerSubtitleLabel);

        final GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(16);
        cardsGrid.setVgap(16);

        final ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setPercentWidth(COL_PERCENT_FIRST);

        final ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setPercentWidth(COL_PERCENT_SECOND);

        final ColumnConstraints thirdColumn = new ColumnConstraints();
        thirdColumn.setPercentWidth(COL_PERCENT_THIRD);

        cardsGrid.getColumnConstraints().addAll(firstColumn, secondColumn, thirdColumn);

        this.balanceCardTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.balanceCardTitleLabel.setAlignment(Pos.CENTER);

        this.ripartitionCardTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.ripartitionCardTitleLabel.setAlignment(Pos.CENTER);
        this.ripartitionTotalLabel.setMaxWidth(Double.MAX_VALUE);
        this.ripartitionTotalLabel.setAlignment(Pos.CENTER);

        this.expenseCardTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.expenseCardTitleLabel.setAlignment(Pos.CENTER);
        this.expenseTotalLabel.setMaxWidth(Double.MAX_VALUE);
        this.expenseTotalLabel.setAlignment(Pos.CENTER);

        this.budgetCardTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.budgetCardTitleLabel.setAlignment(Pos.CENTER);

        this.friendLoanTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.friendLoanTitleLabel.setAlignment(Pos.CENTER);
        this.friendLoanTotalLabel.setMaxWidth(Double.MAX_VALUE);
        this.friendLoanTotalLabel.setAlignment(Pos.CENTER);
        this.friendLoanMessageLabel.setMaxWidth(Double.MAX_VALUE);
        this.friendLoanMessageLabel.setAlignment(Pos.CENTER);

        this.insightsCardTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.insightsCardTitleLabel.setAlignment(Pos.CENTER);

        this.transactionSectionTitleLabel.setMaxWidth(Double.MAX_VALUE);
        this.transactionSectionTitleLabel.setAlignment(Pos.CENTER);

        VBox.setMargin(this.createTransactionButton, new Insets(8, 0, 0, 0));

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

        final HBox budgetContent = new HBox(32, createBudgetCircle(), budgetTextBox);
        budgetContent.setAlignment(Pos.CENTER);
        budgetContent.setMinHeight(BUDGET_CONTENT_MIN_HEIGHT);
        VBox.setVgrow(budgetContent, Priority.ALWAYS);

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
        this.ripartitionChart.setMinHeight(CHART_HEIGHT);
        this.ripartitionChart.setPrefHeight(CHART_HEIGHT);
        this.ripartitionChart.setMaxHeight(CHART_HEIGHT);
        this.ripartitionChart.setStyle(STYLE_TRANSPARENT_BG + " -fx-padding: 0;");

        this.ripartitionEmptyLabel.setWrapText(true);
        this.ripartitionEmptyLabel.setVisible(false);
        this.ripartitionEmptyLabel.setManaged(false);

        final StackPane chartPane = new StackPane(this.ripartitionChart, this.ripartitionEmptyLabel);
        chartPane.setMinHeight(CHART_HEIGHT);
        chartPane.setPrefHeight(CHART_HEIGHT);
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
        this.budgetCircleTrack.setStroke(Color.web("rgba(255, 255, 255, 0.14)"));
        this.budgetCircleTrack.setStrokeWidth(BUDGET_CIRCLE_STROKE_WIDTH);

        this.budgetCircleProgress.setFill(Color.TRANSPARENT);
        this.budgetCircleProgress.setStroke(Color.web("#7c5cff"));
        this.budgetCircleProgress.setStrokeWidth(BUDGET_CIRCLE_STROKE_WIDTH);
        this.budgetCircleProgress.setStrokeLineCap(StrokeLineCap.ROUND);
        this.budgetCircleProgress.setRotate(CIRCLE_ROTATE_ANGLE);

        final double circumference = 2 * Math.PI * this.budgetCircleProgress.getRadius();
        this.budgetCircleProgress.getStrokeDashArray().setAll(circumference, circumference);
        this.budgetCircleProgress.setStrokeDashOffset(circumference);

        this.budgetCirclePane.setMinSize(BUDGET_CIRCLE_BOX_SIZE, BUDGET_CIRCLE_BOX_SIZE);
        this.budgetCirclePane.setPrefSize(BUDGET_CIRCLE_BOX_SIZE, BUDGET_CIRCLE_BOX_SIZE);
        this.budgetCirclePane.setMaxSize(BUDGET_CIRCLE_BOX_SIZE, BUDGET_CIRCLE_BOX_SIZE);
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
        final VBox wrapper = createCard(this.insightsCardTitleLabel);

        this.insightsContainer.setFillWidth(true);

        this.insightsScrollPane.setFitToWidth(true);
        this.insightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.insightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.insightsScrollPane.setPannable(true);
        this.insightsScrollPane.setMinViewportHeight(INSIGHTS_MIN_VIEWPORT_HEIGHT);
        this.insightsScrollPane.setPrefViewportHeight(INSIGHTS_VIEWPORT_HEIGHT);
        this.insightsScrollPane.setMaxHeight(INSIGHTS_VIEWPORT_HEIGHT);
        this.insightsScrollPane.setStyle(
                STYLE_TRANSPARENT_BG
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
                ORDER_NEWEST_FIRST,
                "Oldest first",
                "Highest amount"
        );

        if (this.transactionSortOrderBox.getValue() == null) {
            this.transactionSortOrderBox.setValue(ORDER_NEWEST_FIRST);
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
        this.exportCsvButton.setFocusTraversable(false);

        final StackPane topBar = new StackPane(this.transactionSectionTitleLabel, this.exportCsvButton);
        StackPane.setAlignment(this.transactionSectionTitleLabel, Pos.CENTER);
        StackPane.setAlignment(this.exportCsvButton, Pos.CENTER_RIGHT);

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
        box.setPadding(new Insets(CARD_PADDING));
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
                label.getStyleClass().add("category-cell-label");

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
            runWithFilterUpdatesSuspended(() -> {
                this.transactionKeywordField.clear();
                this.transactionCategoryField.clear();
                this.transactionCategoryTypeBox.setValue("");
                this.transactionFromDatePicker.setValue(null);
                this.transactionToDatePicker.setValue(null);
                this.transactionSortOrderBox.setValue(ORDER_NEWEST_FIRST);
            });
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
                    final Node insightsViewport = this.insightsScrollPane.lookup(".viewport");
                    if (insightsViewport != null) {
                        insightsViewport.setStyle(STYLE_TRANSPARENT_BG);
                    }
                });
                triggerInitialLoadIfReady();
            }
        });
        ThemeManager.addListener(theme -> Platform.runLater(this::applyTheme));
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
     * Executes an action while temporarily suspending filter notification events.
     *
     * @param action
     *            the action to execute
     */
    private void runWithFilterUpdatesSuspended(final Runnable action) {
        this.updatingFilters.set(true);
        try {
            action.run();
        } finally {
            this.updatingFilters.set(false);
        }
    }

    /**
     * Notifies the controller that transaction filters have changed.
     */
    private void notifyTransactionFiltersChanged() {
        if (this.actions == null || this.updatingFilters.get()) {
            return;
        }

        this.actions.onTransactionFiltersChanged(new TransactionFilterInput(
                emptyToNull(this.transactionCategoryField.getText()),
                emptyToNull(this.transactionCategoryTypeBox.getValue()),
                this.transactionFromDatePicker.getValue(),
                this.transactionToDatePicker.getValue(),
                emptyToNull(this.transactionKeywordField.getText()),
                emptyToNull(this.transactionSortOrderBox.getValue()) == null
                ? ORDER_NEWEST_FIRST
                : this.transactionSortOrderBox.getValue()
        ));
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
            final boolean dark = ThemeManager.isDarkMode();
            final String emptyTextColor = dark ? "rgba(255,255,255,0.55)" : "rgba(26,30,40,0.55)";
            styleLabel(this.ripartitionEmptyLabel, FONT_SIZE_MUTED, false, emptyTextColor);
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
                        Math.max(item.percentageValue(), MIN_PIE_SLICE_PERCENTAGE)
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
        this.currentExpenseBreakdown = state;
        this.expenseCardTitleLabel.setText(state.title());
        this.expenseTotalLabel.setText(state.totalExpenseText());
        buildExpenseRows(state);
    }

    /**
     * Builds and styles the category breakdown rows for top expenses.
     *
     * @param state the expense breakdown card view state
     */
    private void buildExpenseRows(final ExpenseBreakdownCardViewState state) {
        this.expenseBreakdownBox.getChildren().clear();

        final boolean dark = ThemeManager.isDarkMode();
        final String strongText = dark ? COLOR_WHITE : COLOR_CHARCOAL_TEXT;
        final String mediumText = dark ? "rgba(255,255,255,0.70)" : "rgba(26,30,40,0.70)";
        final String softText = dark ? "rgba(255,255,255,0.55)" : "rgba(26,30,40,0.55)";

        for (final CategoryBreakdownItemViewState item : state.categories()) {
            final HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            final Label rankLabel = new Label(item.rankText());
            rankLabel.setMinWidth(RANK_LABEL_MIN_WIDTH);

            final Circle dot = new Circle(4, Color.web(item.colorHex()));
            final Label categoryLabel = new Label(item.categoryName());
            final Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            final Label amountLabel = new Label(item.amountText());
            final Label percentageLabel = new Label(item.percentageText());

            final VBox rightBox = new VBox(2, amountLabel, percentageLabel);
            rightBox.setAlignment(Pos.CENTER_RIGHT);

            styleLabel(rankLabel, FONT_SIZE_MUTED, true, mediumText);
            styleLabel(categoryLabel, FONT_SIZE_CAPTION, true, strongText);
            styleLabel(amountLabel, FONT_SIZE_CAPTION, true, strongText);
            styleLabel(percentageLabel, FONT_SIZE_SMALL, false, softText);

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

        if (percentage < BUDGET_WARN_PERCENT) {
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
        if (Objects.equals(this.currentInsights, state.insightCards()) && !this.insightsContainer.getChildren().isEmpty()) {
            return;
        }
        buildInsightTiles(state.insightCards());
    }

    /**
     * Builds and styles individual insight cards inside the insights container.
     *
     * @param cards the list of insight card view states to render
     */
    private void buildInsightTiles(final List<InsightCardViewState> cards) {
        this.currentInsights = List.copyOf(cards);
        this.insightsContainer.getChildren().clear();

        final boolean dark = ThemeManager.isDarkMode();
        final String titleColor = dark ? COLOR_WHITE : COLOR_CHARCOAL_TEXT;
        final String messageColor = dark ? "rgba(255,255,255,0.85)" : "#334155";

        for (final InsightCardViewState insight : cards) {
            final Label title = new Label(insight.title());
            final Label message = new Label(insight.message());
            final Label trend = new Label(insight.trend());

            title.setWrapText(true);
            message.setWrapText(true);
            trend.setWrapText(true);

            title.setMaxWidth(Double.MAX_VALUE);
            message.setMaxWidth(Double.MAX_VALUE);
            trend.setMaxWidth(Double.MAX_VALUE);

            styleLabel(title, FONT_SIZE_CAPTION, true, titleColor);
            styleLabel(message, FONT_SIZE_MUTED, false, messageColor);

            trend.getStyleClass().add("insight-trend");
            if ("POSITIVE".equalsIgnoreCase(insight.trend())) {
                trend.getStyleClass().add("insight-trend-positive");
            } else if ("NEGATIVE".equalsIgnoreCase(insight.trend())) {
                trend.getStyleClass().add("insight-trend-negative");
            } else {
                trend.getStyleClass().add("insight-trend-neutral");
            }

            final VBox tile = new VBox(INSIGHTS_TILE_GAP, title, message, trend);
            tile.getStyleClass().add("insight-tile");
            tile.setFillWidth(true);

            this.insightsContainer.getChildren().add(tile);
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

        runWithFilterUpdatesSuspended(() -> {
            this.transactionKeywordField.setText(filterInput.keyword() == null ? "" : filterInput.keyword());
            this.transactionCategoryField.setText(filterInput.categoryName() == null ? "" : filterInput.categoryName());
            this.transactionCategoryTypeBox.setValue(filterInput.categoryType() == null ? "" : filterInput.categoryType());
            this.transactionFromDatePicker.setValue(filterInput.fromDate());
            this.transactionToDatePicker.setValue(filterInput.toDate());
            this.transactionSortOrderBox.setValue(
                    filterInput.sortOrder() == null ? ORDER_NEWEST_FIRST : filterInput.sortOrder()
            );
        });

        this.transactionTable.getItems().setAll(state.table().rows());
        this.transactionTable.setPlaceholder(new Label(state.table().emptyMessage()));
    }

    /**
     * Applies the current theme to the dashboard.
     */
    private void applyTheme() {
        final Theme theme = ThemeManager.getTheme();
        final boolean dark = theme.isDarkMode();

        final String strongText = dark ? COLOR_WHITE : COLOR_CHARCOAL_TEXT;
        final String mediumText = dark ? "rgba(255,255,255,0.88)" : "rgba(26,30,40,0.85)";
        final String softText = dark ? "rgba(255,255,255,0.68)" : "rgba(26,30,40,0.65)";

        setStyle(
                STYLE_TRANSPARENT_BG
                + "-fx-font-family: '" + theme.getFontFamily() + "';"
                + "-fx-font-size: " + theme.getFontSize() + "px;"
        );

        styleLabel(this.headerTitleLabel, FONT_SIZE_HEADER, true, strongText);
        styleLabel(this.headerWalletLabel, FONT_SIZE_BODY, true, mediumText);
        styleLabel(this.headerSubtitleLabel, FONT_SIZE_CAPTION, false, softText);

        styleLabel(this.balanceCardTitleLabel, FONT_SIZE_BODY, true, mediumText);
        styleLabel(this.balanceAmountLabel, FONT_SIZE_HERO, true, strongText);
        styleLabel(this.balanceCurrencyLabel, FONT_SIZE_BODY, false, softText);

        styleLabel(this.ripartitionCardTitleLabel, FONT_SIZE_BODY, true, mediumText);
        styleLabel(this.ripartitionTotalLabel, FONT_SIZE_STAT, true, strongText);

        styleLabel(this.expenseCardTitleLabel, FONT_SIZE_BODY, true, mediumText);
        styleLabel(this.expenseTotalLabel, FONT_SIZE_HEADER, true, strongText);

        styleLabel(this.budgetCardTitleLabel, FONT_SIZE_BODY, true, mediumText);
        styleLabel(this.budgetStatusLabel, FONT_SIZE_STATUS, true, strongText);
        styleLabel(this.budgetProgressLabel, FONT_SIZE_SUBTITLE, false, strongText);
        styleLabel(this.budgetThresholdLabel, FONT_SIZE_CAPTION, false, softText);
        styleLabel(this.budgetCirclePercentageLabel, FONT_SIZE_STAT, true, strongText);
        this.budgetCircleTrack.setStroke(dark ? Color.web("rgba(255, 255, 255, 0.14)") : Color.web("rgba(0, 0, 0, 0.08)"));

        styleLabel(this.friendLoanTitleLabel, FONT_SIZE_BODY, true, mediumText);
        styleLabel(this.friendLoanTotalLabel, FONT_SIZE_HEADER, true, strongText);
        styleLabel(this.friendLoanMessageLabel, FONT_SIZE_CAPTION, false, softText);

        styleLabel(this.insightsCardTitleLabel, FONT_SIZE_BODY, true, mediumText);

        styleLabel(this.transactionSectionTitleLabel, FONT_SIZE_SUBTITLE, true, strongText);

        styleLabel(this.ripartitionEmptyLabel, FONT_SIZE_MUTED, false, softText);

        if (this.currentExpenseBreakdown != null) {
            buildExpenseRows(this.currentExpenseBreakdown);
        }

        if (!this.currentInsights.isEmpty()) {
            buildInsightTiles(this.currentInsights);
        }

        this.transactionTable.refresh();
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
}
