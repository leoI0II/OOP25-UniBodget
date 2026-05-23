package it.unibo.unibodget.view.dashboard.impl;

import java.util.Objects;

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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

public final class DefaultDashboardView extends BorderPane implements DashboardView {

    private static final String CARD_STYLE_CLASS = "dashboard-card";

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
    private final HBox balanceCardHeaderBox;

    private final Label ripartitionCardTitleLabel;
    private final Label ripartitionTotalLabel;
    private final PieChart ripartitionChart;
    private final VBox ripartitionLegendBox;
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

    private final HBox insightsContainer;

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

    public DefaultDashboardView() {
        this.sidebarRoot = new VBox(16);
        this.navigationMenuButton = new MenuButton("\u2630");
        this.walletsSectionTitle = new Label();
        this.walletsCountLabel = new Label();
        this.createWalletButton = new Button("+ New Wallet");
        this.walletListBox = new VBox(10);
        this.walletScrollPane = new ScrollPane(walletListBox);
        this.totalFooterLabel = new Label();
        this.totalFooterAmountLabel = new Label();
        this.totalFooterCurrencyLabel = new Label();

        this.headerTitleLabel = new Label();
        this.headerWalletLabel = new Label();
        this.headerSubtitleLabel = new Label();

        this.balanceCardTitleLabel = new Label();
        this.balanceAmountLabel = new Label();
        this.balanceCurrencyLabel = new Label();
        this.balanceCardHeaderBox = new HBox(12);

        this.ripartitionCardTitleLabel = new Label();
        this.ripartitionTotalLabel = new Label();
        this.ripartitionChart = new PieChart();
        this.ripartitionLegendBox = new VBox(8);
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
        this.budgetCircleTrack = new Circle(28);
        this.budgetCircleProgress = new Circle(28);
        this.budgetCirclePercentageLabel = new Label();

        this.friendLoanTitleLabel = new Label();
        this.friendLoanTotalLabel = new Label();
        this.friendLoanMessageLabel = new Label();

        this.insightsContainer = new HBox(16);

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

    @Override
    public void bindActions(final DashboardViewActions actions) {
        this.actions = Objects.requireNonNull(actions);
        triggerInitialLoadIfReady();
    }

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

    @Override
    public void showError(final String message) {
        this.headerSubtitleLabel.setText(message);
    }

    private void buildLayout() {
        setPadding(new Insets(18));
        setPrefSize(1440, 900);

        buildSidebar();
        setLeft(sidebarRoot);

        final VBox centerContent = new VBox(18);
        centerContent.setPadding(new Insets(4, 0, 0, 18));

        final VBox headerBox = new VBox(4, headerTitleLabel, headerWalletLabel, headerSubtitleLabel);

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

        final Region balanceHeaderSpacer = new Region();
        HBox.setHgrow(balanceHeaderSpacer, Priority.ALWAYS);
        balanceCardHeaderBox.getChildren().setAll(balanceCardTitleLabel, balanceHeaderSpacer, createTransactionButton);
        balanceCardHeaderBox.setAlignment(Pos.CENTER_LEFT);

        final VBox balanceCard = createCard(balanceCardHeaderBox, balanceAmountLabel, balanceCurrencyLabel);
        final VBox ripartitionCard = createRipartitionCard();
        final VBox expenseCard = createExpenseCard();

        final HBox budgetContent = new HBox(14, createBudgetCircle(), new VBox(
                6,
                budgetStatusLabel,
                budgetProgressLabel,
                budgetThresholdLabel,
                editBudgetButton
        ));
        budgetContent.setAlignment(Pos.CENTER_LEFT);

        final VBox budgetCard = createCard(
                budgetCardTitleLabel,
                budgetContent
        );

        final VBox friendLoanCard = createCard(
                friendLoanTitleLabel,
                friendLoanTotalLabel,
                friendLoanMessageLabel
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

    private void buildSidebar() {
        sidebarRoot.setPadding(new Insets(18));
        sidebarRoot.setPrefWidth(320);
        sidebarRoot.setMinWidth(290);
        sidebarRoot.setMaxWidth(360);

        navigationMenuButton.setFocusTraversable(false);
        navigationMenuButton.setMaxWidth(Double.MAX_VALUE);

        final Region walletHeaderSpacer = new Region();
        HBox.setHgrow(walletHeaderSpacer, Priority.ALWAYS);

        final VBox walletTextBox = new VBox(4, walletsSectionTitle, walletsCountLabel);
        final HBox walletSectionHeader = new HBox(10, walletTextBox, walletHeaderSpacer, createWalletButton);
        walletSectionHeader.setAlignment(Pos.CENTER_LEFT);

        walletListBox.setFillWidth(true);
        walletScrollPane.setFitToWidth(true);
        walletScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        walletScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        walletScrollPane.setPrefViewportHeight(420);
        VBox.setVgrow(walletScrollPane, Priority.ALWAYS);

        final VBox totalFooterBox = createCard(
                totalFooterLabel,
                totalFooterAmountLabel,
                totalFooterCurrencyLabel
        );

        final Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebarRoot.getChildren().addAll(
                navigationMenuButton,
                walletSectionHeader,
                walletScrollPane,
                spacer,
                new Separator(),
                totalFooterBox
        );
    }

    private VBox createRipartitionCard() {
        ripartitionChart.setLegendVisible(false);
        ripartitionChart.setLabelsVisible(false);
        ripartitionChart.setClockwise(true);
        ripartitionChart.setStartAngle(90);
        ripartitionChart.setMinHeight(170);
        ripartitionChart.setPrefHeight(170);
        ripartitionChart.setMaxHeight(170);
        ripartitionChart.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        ripartitionEmptyLabel.setWrapText(true);
        ripartitionEmptyLabel.setVisible(false);
        ripartitionEmptyLabel.setManaged(false);

        final StackPane chartPane = new StackPane(ripartitionChart, ripartitionEmptyLabel);
        chartPane.setMinHeight(170);
        chartPane.setPrefHeight(170);
        chartPane.setAlignment(Pos.CENTER);

        final VBox card = createCard(ripartitionCardTitleLabel, ripartitionTotalLabel, chartPane, ripartitionLegendBox);
        return card;
    }

    private VBox createExpenseCard() {
        final VBox card = createCard(expenseCardTitleLabel, expenseTotalLabel);
        card.getChildren().add(expenseBreakdownBox);
        return card;
    }

    private Node createBudgetCircle() {
        budgetCircleTrack.setFill(Color.TRANSPARENT);
        budgetCircleTrack.setStroke(Color.rgb(255, 255, 255, 0.12));
        budgetCircleTrack.setStrokeWidth(8);

        budgetCircleProgress.setFill(Color.TRANSPARENT);
        budgetCircleProgress.setStroke(Color.web("#7c5cff"));
        budgetCircleProgress.setStrokeWidth(8);
        budgetCircleProgress.setStrokeLineCap(StrokeLineCap.ROUND);
        budgetCircleProgress.setRotate(-90);

        final double circumference = 2 * Math.PI * budgetCircleProgress.getRadius();
        budgetCircleProgress.getStrokeDashArray().setAll(circumference, circumference);
        budgetCircleProgress.setStrokeDashOffset(circumference);

        budgetCirclePercentageLabel.setStyle(
                "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
        );

        budgetCirclePane.setMinSize(76, 76);
        budgetCirclePane.setPrefSize(76, 76);
        budgetCirclePane.setMaxSize(76, 76);
        budgetCirclePane.getChildren().setAll(
                budgetCircleTrack,
                budgetCircleProgress,
                budgetCirclePercentageLabel
        );

        return budgetCirclePane;
    }

    private VBox createInsightsCard() {
        final Label title = new Label("Active Wallet Insights");
        final VBox wrapper = createCard(title);
        insightsContainer.setFillHeight(true);
        insightsContainer.setSpacing(12);
        wrapper.getChildren().add(insightsContainer);
        return wrapper;
    }

    private Node createTransactionFiltersBar() {
        transactionKeywordField.setPromptText("Keyword");
        transactionCategoryField.setPromptText("Category");
        transactionCategoryTypeBox.setPromptText("Type");
        transactionFromDatePicker.setPromptText("From");
        transactionToDatePicker.setPromptText("To");
        transactionSortOrderBox.setPromptText("Sort by");

        transactionCategoryTypeBox.getItems().setAll(
                "",
                "Income",
                "Expense",
                "Transfer",
                "Friend loan"
        );

        transactionSortOrderBox.getItems().setAll(
                "Newest first",
                "Oldest first",
                "Highest amount"
        );

        if (transactionSortOrderBox.getValue() == null) {
            transactionSortOrderBox.setValue("Newest first");
        }

        final HBox filtersBar = new HBox(
                10,
                transactionKeywordField,
                transactionCategoryField,
                transactionCategoryTypeBox,
                transactionFromDatePicker,
                transactionToDatePicker,
                transactionSortOrderBox,
                clearFiltersButton
        );
        filtersBar.setAlignment(Pos.CENTER_LEFT);
        return filtersBar;
    }

    private VBox createTransactionsCard() {
        final HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        exportCsvButton.setFocusTraversable(false);

        topBar.getChildren().addAll(
                transactionSectionTitleLabel,
                spacer,
                exportCsvButton
        );

        final Node filtersBar = createTransactionFiltersBar();

        final VBox card = createCard(topBar, filtersBar);
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        card.getChildren().add(transactionTable);
        return card;
    }

    private VBox createCard(final Node... nodes) {
        final VBox box = new VBox(10);
        box.getStyleClass().add(CARD_STYLE_CLASS);
        box.setPadding(new Insets(18));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.045);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(255,255,255,0.07);"
        );
        box.getChildren().addAll(nodes);
        return box;
    }

    private void configureTable() {
        final TableColumn<TransactionRowViewState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().description()));

        final TableColumn<TransactionRowViewState, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().dateText()));

        final TableColumn<TransactionRowViewState, TransactionRowViewState> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue()));
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

        final TableColumn<TransactionRowViewState, TransactionRowViewState> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue()));
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

        transactionTable.getColumns().clear();
        transactionTable.getColumns().add(descriptionColumn);
        transactionTable.getColumns().add(dateColumn);
        transactionTable.getColumns().add(categoryColumn);
        transactionTable.getColumns().add(amountColumn);
    }

    private void configureActions() {
        createWalletButton.setOnAction(event -> {
            if (actions != null) {
                actions.onCreateWalletRequested();
            }
        });

        createTransactionButton.setOnAction(event -> {
            if (actions != null) {
                actions.onCreateTransactionRequested();
            }
        });

        exportCsvButton.setOnAction(event -> {
            if (actions != null) {
                actions.onExportTransactionsRequested();
            }
        });

        editBudgetButton.setOnAction(event -> {
            if (actions != null) {
                actions.onEditBudgetRequested();
            }
        });

        transactionKeywordField.textProperty().addListener((obs, oldValue, newValue) -> notifyTransactionFiltersChanged());
        transactionCategoryField.textProperty().addListener((obs, oldValue, newValue) -> notifyTransactionFiltersChanged());
        transactionCategoryTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> notifyTransactionFiltersChanged());
        transactionFromDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> notifyTransactionFiltersChanged());
        transactionToDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> notifyTransactionFiltersChanged());
        transactionSortOrderBox.valueProperty().addListener((obs, oldValue, newValue) -> notifyTransactionFiltersChanged());

        clearFiltersButton.setOnAction(event -> {
            updatingFilters = true;
            try {
                transactionKeywordField.clear();
                transactionCategoryField.clear();
                transactionCategoryTypeBox.setValue("");
                transactionFromDatePicker.setValue(null);
                transactionToDatePicker.setValue(null);
                transactionSortOrderBox.setValue("Newest first");
            } finally {
                updatingFilters = false;
            }
            notifyTransactionFiltersChanged();
        });
    }

    private void configureLifecycle() {
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                triggerInitialLoadIfReady();
            }
        });
    }

    private void triggerInitialLoadIfReady() {
        if (!initialLoadTriggered && getScene() != null && actions != null) {
            initialLoadTriggered = true;
            actions.onViewOpened();
        }
    }

    private void notifyTransactionFiltersChanged() {
        if (actions == null || updatingFilters) {
            return;
        }

        actions.onTransactionFiltersChanged(new TransactionFilterInput(
                emptyToNull(transactionCategoryField.getText()),
                emptyToNull(transactionCategoryTypeBox.getValue()),
                transactionFromDatePicker.getValue(),
                transactionToDatePicker.getValue(),
                emptyToNull(transactionKeywordField.getText()),
                emptyToNull(transactionSortOrderBox.getValue()) == null
                        ? "Newest first"
                        : transactionSortOrderBox.getValue()
        ));
    }

    private void renderSidebar(final DashboardViewState state) {
        walletsSectionTitle.setText(state.sidebar().walletSection().title());
        walletsCountLabel.setText(state.sidebar().walletSection().walletCountText());

        renderNavigationMenu(state);
        renderWalletList(state);
        renderSidebarFooter(state.sidebar().totalFooter());
    }

    private void renderNavigationMenu(final DashboardViewState state) {
        navigationMenuButton.setText("\u2630");
        navigationMenuButton.getItems().clear();

        state.sidebar().navigationMenu().items().forEach(item -> {
            final MenuItem menuItem = new MenuItem(item.label());
            menuItem.setDisable(!item.enabled());
            menuItem.setOnAction(event -> {
                if (actions != null) {
                    actions.onNavigationRequested(item.destination());
                }
            });
            navigationMenuButton.getItems().add(menuItem);
        });
    }

    private void renderWalletList(final DashboardViewState state) {
        walletListBox.getChildren().clear();

        for (final SidebarWalletItemViewState wallet : state.sidebar().walletSection().wallets()) {
            final Button walletButton = new Button();
            walletButton.setMaxWidth(Double.MAX_VALUE);
            walletButton.setAlignment(Pos.CENTER_LEFT);
            walletButton.setWrapText(true);
            walletButton.setText(wallet.walletName() + "\n" + wallet.balanceText());
            walletButton.setOnAction(event -> {
                if (actions != null) {
                    actions.onWalletSelected(wallet.walletId());
                }
            });

            if (wallet.selected()) {
                walletButton.setStyle(
                        "-fx-background-color: rgba(90, 168, 255, 0.24);"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-padding: 14 16 14 16;"
                        + "-fx-font-weight: bold;"
                        + "-fx-border-color: rgba(120,190,255,0.35);"
                        + "-fx-border-radius: 14;"
                );
            } else {
                walletButton.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.05);"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-padding: 14 16 14 16;"
                        + "-fx-border-color: rgba(255,255,255,0.06);"
                        + "-fx-border-radius: 14;"
                );
            }

            walletListBox.getChildren().add(walletButton);
        }
    }

    private void renderSidebarFooter(final SidebarFooterTotalViewState footer) {
        totalFooterLabel.setText(footer.label());
        totalFooterAmountLabel.setText(footer.convertedTotalText());
        totalFooterCurrencyLabel.setText("Base currency: " + footer.baseCurrencyText());
    }

    private void renderHeader(final HeaderViewState state) {
        headerTitleLabel.setText(state.title());
        headerWalletLabel.setText(state.activeWalletText());
        headerSubtitleLabel.setText(state.subtitle());
    }

    private void renderBalanceCard(final BalanceCardViewState state) {
        balanceCardTitleLabel.setText(state.title());
        balanceAmountLabel.setText(state.balanceText());
        balanceCurrencyLabel.setText("Currency: " + state.currencyText());
    }

    private void renderExpenseRipartitionCard(final ExpenseRipartitionCardViewState state) {
        ripartitionCardTitleLabel.setText(state.title());
        ripartitionTotalLabel.setText(state.totalExpenseText());

        ripartitionChart.getData().clear();
        ripartitionLegendBox.getChildren().clear();

        if (state.categories().isEmpty()) {
            ripartitionChart.setVisible(false);
            ripartitionChart.setManaged(false);
            ripartitionEmptyLabel.setVisible(true);
            ripartitionEmptyLabel.setManaged(true);
            ripartitionEmptyLabel.setText(state.emptyMessage());
            styleLabel(ripartitionEmptyLabel, 12, false, "rgba(255,255,255,0.55)");
            return;
        }

        ripartitionChart.setVisible(true);
        ripartitionChart.setManaged(true);
        ripartitionEmptyLabel.setVisible(false);
        ripartitionEmptyLabel.setManaged(false);

        ripartitionChart.setData(FXCollections.observableArrayList(
                state.categories().stream()
                        .map(item -> new PieChart.Data(item.categoryName(),
                                Math.max(item.percentageValue(), 0.01)))
                        .toList()
        ));

        for (int i = 0; i < ripartitionChart.getData().size(); i++) {
            final PieChart.Data data = ripartitionChart.getData().get(i);
            final CategoryBreakdownItemViewState item = state.categories().get(i);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + item.colorHex() + ";");
                }
            });

            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + item.colorHex() + ";");
            }

            final Circle dot = new Circle(5, Color.web(item.colorHex()));
            final Label categoryLabel = new Label(item.categoryName());
            final Label valueLabel = new Label(item.percentageText());
            final Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            styleLabel(categoryLabel, 12, true, "white");
            styleLabel(valueLabel, 11, false, "rgba(255,255,255,0.55)");

            final HBox row = new HBox(8, dot, categoryLabel, spacer, valueLabel);
            row.setAlignment(Pos.CENTER_LEFT);
            ripartitionLegendBox.getChildren().add(row);
        }
    }

    private void renderExpenseCard(final ExpenseBreakdownCardViewState state) {
        expenseCardTitleLabel.setText(state.title());
        expenseTotalLabel.setText(state.totalExpenseText());
        expenseBreakdownBox.getChildren().clear();

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
            expenseBreakdownBox.getChildren().add(row);
        }
    }

    private void renderBudgetCard(final BudgetCardViewState state) {
        budgetCardTitleLabel.setText(state.title());
        budgetStatusLabel.setText(state.statusLabel());
        budgetProgressLabel.setText(state.progressText());
        budgetThresholdLabel.setText(state.warningThresholdText());
        editBudgetButton.setText(state.actionLabel());

        final double percentage = Math.max(0.0, Math.min(100.0, state.usagePercentage()));
        final double circumference = 2 * Math.PI * budgetCircleProgress.getRadius();
        budgetCircleProgress.setStrokeDashOffset(circumference * (1.0 - (percentage / 100.0)));
        budgetCirclePercentageLabel.setText((int) Math.round(percentage) + "%");

        if (percentage < 80.0) {
            budgetCircleProgress.setStroke(Color.web("#5EDC90"));
        } else if (percentage < 100.0) {
            budgetCircleProgress.setStroke(Color.web("#F6C85F"));
        } else {
            budgetCircleProgress.setStroke(Color.web("#FF7B7B"));
        }
    }

    private void renderFriendLoanCard(final FriendLoanCardViewState state) {
        friendLoanTitleLabel.setText(state.title());
        friendLoanTotalLabel.setText(state.totalOutstandingText());
        friendLoanMessageLabel.setText(state.message());
        friendLoanMessageLabel.setWrapText(true);
    }

    private void renderInsights(final DashboardViewState state) {
        insightsContainer.getChildren().clear();

        for (final InsightCardViewState insight : state.insightCards()) {
            final Label title = new Label(insight.title());
            final Label message = new Label(insight.message());
            message.setWrapText(true);
            message.setMaxWidth(260);
            final Label trend = new Label(insight.trend());

            styleLabel(title, 13, true, "white");
            styleLabel(message, 12, false, "rgba(255,255,255,0.72)");
            styleLabel(trend, 11, true, "rgba(120,190,255,0.90)");

            final VBox card = new VBox(8, title, message, trend);
            card.setPadding(new Insets(14));
            card.setPrefWidth(260);
            card.setMinHeight(140);
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05);"
                    + "-fx-background-radius: 14;"
                    + "-fx-border-radius: 14;"
                    + "-fx-border-color: rgba(255,255,255,0.08);"
            );

            insightsContainer.getChildren().add(card);
        }
    }

    private void renderTransactions(final TransactionSectionViewState state) {
        transactionSectionTitleLabel.setText(state.title());
        exportCsvButton.setText(state.exportButtonText());

        final TransactionFilterInput filterInput = state.table().filterInput();

        updatingFilters = true;
        try {
            transactionKeywordField.setText(filterInput.keyword() == null ? "" : filterInput.keyword());
            transactionCategoryField.setText(filterInput.categoryName() == null ? "" : filterInput.categoryName());
            transactionCategoryTypeBox.setValue(filterInput.categoryType() == null ? "" : filterInput.categoryType());
            transactionFromDatePicker.setValue(filterInput.fromDate());
            transactionToDatePicker.setValue(filterInput.toDate());
            transactionSortOrderBox.setValue(filterInput.sortOrder() == null ? "Newest first" : filterInput.sortOrder());
        } finally {
            updatingFilters = false;
        }

        transactionTable.getItems().setAll(state.table().rows());
        transactionTable.setPlaceholder(new Label(state.table().emptyMessage()));
    }

    private void applyTheme() {
        final Theme theme = ThemeManager.getTheme();

        final String backgroundHex = toRgbaCss(theme.getPrimaryColor());
        final String buttonHex = toRgbaCss(theme.getButtonColor());
        final String textHex = toRgbaCss(theme.getTextColor());

        setStyle(
                "-fx-background-color: linear-gradient(to bottom right, "
                + backgroundHex + ", rgba(16,18,27,0.98));"
                + "-fx-font-family: '" + theme.getFontFamily() + "';"
                + "-fx-font-size: " + theme.getFontSize() + "px;"
        );

        sidebarRoot.setStyle(
                "-fx-background-color: rgba(9,12,20,0.88);"
                + "-fx-background-radius: 22;"
                + "-fx-border-radius: 22;"
                + "-fx-border-color: rgba(255,255,255,0.08);"
        );

        navigationMenuButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 14;"
                + "-fx-padding: 10 14 10 14;"
                + "-fx-border-color: rgba(255,255,255,0.06);"
                + "-fx-border-radius: 14;"
        );

        createWalletButton.setStyle(
                "-fx-background-color: " + buttonHex + ";"
                + "-fx-text-fill: " + textHex + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 8 12 8 12;"
                + "-fx-font-weight: bold;"
        );

        createTransactionButton.setStyle(
                "-fx-background-color: " + buttonHex + ";"
                + "-fx-text-fill: " + textHex + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 10 14 10 14;"
                + "-fx-font-weight: bold;"
        );

        exportCsvButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.09);"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 10 14 10 14;"
                + "-fx-border-color: rgba(255,255,255,0.08);"
                + "-fx-border-radius: 12;"
        );

        editBudgetButton.setFocusTraversable(false);
        editBudgetButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.09);"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 10 14 10 14;"
                + "-fx-border-color: rgba(255,255,255,0.08);"
                + "-fx-border-radius: 12;"
        );

        clearFiltersButton.setFocusTraversable(false);
        clearFiltersButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.09);"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 8 12 8 12;"
                + "-fx-border-color: rgba(255,255,255,0.08);"
                + "-fx-border-radius: 12;"
        );

        final String filterFieldStyle =
                "-fx-background-color: rgba(255,255,255,0.08);"
                + "-fx-text-fill: white;"
                + "-fx-prompt-text-fill: rgba(255,255,255,0.45);"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: rgba(255,255,255,0.08);";

        transactionKeywordField.setStyle(filterFieldStyle);
        transactionCategoryField.setStyle(filterFieldStyle);
        transactionCategoryTypeBox.setStyle(filterFieldStyle);
        transactionFromDatePicker.setStyle(filterFieldStyle);
        transactionToDatePicker.setStyle(filterFieldStyle);
        transactionSortOrderBox.setStyle(filterFieldStyle);

        styleLabel(headerTitleLabel, 28, true, "white");
        styleLabel(headerWalletLabel, 14, true, "rgba(255,255,255,0.88)");
        styleLabel(headerSubtitleLabel, 13, false, "rgba(255,255,255,0.62)");

        styleLabel(walletsSectionTitle, 16, true, "white");
        styleLabel(walletsCountLabel, 12, false, "rgba(255,255,255,0.60)");

        styleLabel(balanceCardTitleLabel, 14, true, "rgba(255,255,255,0.82)");
        styleLabel(balanceAmountLabel, 34, true, "white");
        styleLabel(balanceCurrencyLabel, 13, false, "rgba(255,255,255,0.68)");

        styleLabel(ripartitionCardTitleLabel, 14, true, "rgba(255,255,255,0.82)");
        styleLabel(ripartitionTotalLabel, 24, true, "white");

        styleLabel(expenseCardTitleLabel, 14, true, "rgba(255,255,255,0.82)");
        styleLabel(expenseTotalLabel, 28, true, "white");

        styleLabel(budgetCardTitleLabel, 14, true, "rgba(255,255,255,0.82)");
        styleLabel(budgetStatusLabel, 22, true, "white");
        styleLabel(budgetProgressLabel, 15, false, "white");
        styleLabel(budgetThresholdLabel, 13, false, "rgba(255,255,255,0.68)");

        styleLabel(friendLoanTitleLabel, 14, true, "rgba(255,255,255,0.82)");
        styleLabel(friendLoanTotalLabel, 28, true, "white");
        styleLabel(friendLoanMessageLabel, 13, false, "rgba(255,255,255,0.68)");

        styleLabel(transactionSectionTitleLabel, 16, true, "white");
        styleLabel(totalFooterLabel, 13, false, "rgba(255,255,255,0.70)");
        styleLabel(totalFooterAmountLabel, 22, true, "white");
        styleLabel(totalFooterCurrencyLabel, 12, false, "rgba(255,255,255,0.60)");

        transactionTable.setStyle(
                "-fx-background-color: rgba(255,255,255,0.035);"
                + "-fx-control-inner-background: rgba(255,255,255,0.035);"
                + "-fx-table-cell-border-color: rgba(255,255,255,0.08);"
                + "-fx-text-background-color: white;"
                + "-fx-background-radius: 14;"
        );
    }

    private void styleLabel(final Label label, final int size, final boolean bold, final String color) {
        label.setStyle(
                "-fx-text-fill: " + color + ";"
                + "-fx-font-size: " + size + "px;"
                + "-fx-font-weight: " + (bold ? "bold" : "normal") + ";"
        );
    }

    private String emptyToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String toRgbaCss(final ARGBColor color) {
        final double opacity = color.alpha() / 255.0;
        return "rgba(" + color.red() + "," + color.green() + "," + color.blue() + "," + opacity + ")";
    }
}