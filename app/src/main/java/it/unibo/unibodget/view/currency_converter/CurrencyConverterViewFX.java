package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.BankConversionController;
import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.controller.currency_converter.WatchListController;
import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIClient;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.currency.watchlist.WatchList;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dashboard containing the converter, watchlist and bank conversion widgets.
 *
 * <p>Font sizes are local to this page. Font family and weight follow the active theme.
 */
public final class CurrencyConverterViewFX {

    private static final double ROOT_PADDING = 35;
    private static final double CARD_PADDING = 20;
    private static final double CARD_WIDTH = 350;
    private static final double CONTENT_SPACING = 25;
    private static final double PAGE_SPACING = 30;
    private static final double TOP_BAR_SPACING = 20;

    private static final int FONT_SIZE = 14;
    private static final int SECTION_TITLE_FONT_SIZE = 18;
    private static final int TITLE_FONT_SIZE = 24;
    private static final int RESULT_FONT_SIZE = 24;

    private static final int CHART_WIDTH = 900;
    private static final int CHART_HEIGHT = 600;

    private static final WindowPreferences PREFS = new WindowPreferences();

    /** Prevents instantiation. */
    private CurrencyConverterViewFX() { }

    /**
     * Opens the dashboard in a standalone window.
     *
     * @param controller the main currency converter controller
     */
    public static void launchWith(final CurrencyConverterController controller) {
        if (controller == null) {
            throw new IllegalStateException("Controller not initialized.");
        }

        final Stage stage = new Stage();
        final Scene scene = new Scene(buildContent(controller));

        stage.setScene(scene);
        ThemeManager.applyThemeToScene(scene);
        stage.setTitle("UniBodget - Currency Dashboard");

        stage.setWidth(PREFS.getWidth());
        stage.setHeight(PREFS.getHeight());
        stage.setMaximized(PREFS.isMaximized());

        stage.widthProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!stage.isMaximized()) {
                                PREFS.setWidth(newValue.doubleValue());
                            }
                        });

        stage.heightProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!stage.isMaximized()) {
                                PREFS.setHeight(newValue.doubleValue());
                            }
                        });

        stage.maximizedProperty()
                .addListener((observable, oldValue, newValue) -> PREFS.setMaximized(newValue));

        stage.show();
    }

    /**
     * Builds reusable dashboard content.
     *
     * @param controller the shared currency converter controller
     * @return the dashboard root
     */
    public static Parent buildContent(final CurrencyConverterController controller) {
        final Label pageTitle = new Label("Currencies Converter Dashboard");

        final CurrencyAlertService alertService = new CurrencyAlertService();
        final ConverterWidgetFX converterWidget = new ConverterWidgetFX(controller, alertService);

        final WatchList watchlistModel = new WatchList();
        final WatchListController watchlistController = new WatchListController(watchlistModel);

        final WatchlistWidgetFX watchlistWidget =
                new WatchlistWidgetFX(watchlistController, converterWidget);

        final BankConversionController bankController = new BankConversionController();

        final BankConverterWidgetFX bankConverterWidget =
                new BankConverterWidgetFX(
                        bankController,
                        converterWidget.getAmountField(),
                        converterWidget.getFromBox(),
                        converterWidget.getToBox(),
                        (BasicCurrencyConverter) controller.getConverter());

        final Button showChartButton = new Button("Show Chart");

        final CurrencyConverterController historyController =
                new CurrencyConverterController(
                        new ExchangeRateAPIClient(), controller.getConverter());

        showChartButton.setOnAction(
                event -> {
                    final CurrencyHistoryChartView chartView =
                            new CurrencyHistoryChartView(historyController);

                    final Stage popup = new Stage();
                    popup.initOwner(showChartButton.getScene().getWindow());
                    popup.initModality(Modality.WINDOW_MODAL);
                    popup.setTitle("Currency History");

                    final Scene popupScene = new Scene(chartView, CHART_WIDTH, CHART_HEIGHT);

                    popup.setScene(popupScene);
                    ThemeManager.applyThemeToScene(popupScene);
                    popup.showAndWait();
                });

        final HBox topBar = new HBox(TOP_BAR_SPACING, pageTitle, showChartButton);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Removes the converter's original card decoration.
        // The wrapper below provides the shared appearance.
        converterWidget.getView().setBackground(null);
        converterWidget.getView().setBorder(null);
        converterWidget.getView().setPadding(Insets.EMPTY);

        final VBox converterCard = createCard(converterWidget.getView());
        final VBox watchlistCard = createCard(watchlistWidget.getView());
        final VBox bankCard = createCard(bankConverterWidget.getView());

        final HBox contentRow = new HBox(CONTENT_SPACING, converterCard, watchlistCard, bankCard);
        contentRow.setAlignment(Pos.TOP_LEFT);
        contentRow.setFillHeight(true);

        HBox.setHgrow(converterCard, Priority.ALWAYS);
        HBox.setHgrow(watchlistCard, Priority.ALWAYS);
        HBox.setHgrow(bankCard, Priority.ALWAYS);

        final VBox root = new VBox(PAGE_SPACING, topBar, contentRow);
        root.setPadding(new Insets(ROOT_PADDING));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("theme-background");

        VBox.setVgrow(contentRow, Priority.ALWAYS);

        // Provides the inherited size for dynamically created cells.
        setFontSize(root, FONT_SIZE);
        applyPageFontSizes(root, pageTitle);

        root.sceneProperty()
                .addListener(
                        (observable, oldScene, newScene) -> {
                            if (newScene != null) {
                                ThemeManager.applyThemeToScene(newScene);
                            }
                        });

        return root;
    }

    /**
     * Wraps a widget in a rounded card.
     *
     * @param content the widget content
     * @return the card container
     */
    private static VBox createCard(final Node content) {
        final VBox card = new VBox(content);
        card.setPadding(new Insets(CARD_PADDING));
        card.setMinWidth(0);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
                "-fx-background-color: rgba(128,128,128,0.15);"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: rgba(128,128,128,0.30);"
                        + "-fx-border-radius: 18;"
                        + "-fx-border-width: 1;");

        VBox.setVgrow(content, Priority.ALWAYS);
        return card;
    }

    /**
     * Applies local font sizes without changing theme colors or typography.
     *
     * @param node the current node
     * @param pageTitle the dashboard title
     */
    private static void applyPageFontSizes(final Node node, final Label pageTitle) {
        if (node instanceof Control) {
            final int size;

            if (node == pageTitle) {
                size = TITLE_FONT_SIZE;
            } else if (Boolean.TRUE.equals(node.getProperties().get("converter-section-title"))) {
                size = SECTION_TITLE_FONT_SIZE;
            } else if (Boolean.TRUE.equals(node.getProperties().get("converter-result"))) {
                size = RESULT_FONT_SIZE;
            } else {
                size = FONT_SIZE;
            }

            setFontSize(node, size);
            ThemeManager.applyFont(node);

            // Internal skin nodes inherit the control's font size.
            return;
        }

        if (node instanceof Parent parent) {
            for (final Node child : parent.getChildrenUnmodifiable()) {
                applyPageFontSizes(child, pageTitle);
            }
        }
    }

    /**
     * Replaces the inline font size while preserving other declarations.
     *
     * @param node the node to style
     * @param size the font size in pixels
     */
    private static void setFontSize(final Node node, final int size) {
        final String existingStyle =
                node.getStyle().replaceAll("(?i)-fx-font-size\\s*:[^;]*(?:;|$)", "");

        node.setStyle(existingStyle + ";-fx-font-size: " + size + "px;");
    }
}
