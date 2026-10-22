package it.unibo.unibodget.view.main;

import java.util.Objects;
import java.util.function.Consumer;

import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.view.utils.DialogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * JavaFX controller for the sidebar view.
 *
 * <p>Renders the list of wallet tiles and the aggregated total balance label.
 * Delegates data retrieval and selection callbacks to a {@link SideBarDelegate}.</p>
 */
public class SideBarViewController extends BaseViewController {

    private static final int ADD_WALLET_TILE_HEIGHT = 50;
    protected static final double MIN_SIDEBAR_WIDTH = 200.0;
    protected static final double MAX_SIDEBAR_WIDTH = 400.0;
    private static final double TOTAL_BALANCE_HORIZONTAL_PADDING = 36.0;
    private static final double TOTAL_FONT_SIZE = 22.0;

    @FXML private Button closeButton;
    @FXML private MenuButton menuButton;
    @FXML private VBox walletList;
    @FXML private Label totalTitleLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private MenuItem navDashboardItem;
    @FXML private MenuItem navConverterItem;
    @FXML private MenuItem navPortfolioItem;
    @FXML private MenuItem navSettingsItem;
    private SideBarDelegate delegate;
    private Consumer<AppContext> navigationHandler = context -> { };
    private Consumer<Double> widthAdjustmentHandler = w -> { };
    private final ViewControllersFactory viewControllersFactory;

    /**
     * Default constructor for testing and standalone loading.
     */
    public SideBarViewController() {
        this(null);
    }

    /**
     * Constructs a new SideBarViewController with the given factory.
     *
     * @param viewControllersFactory the factory used to create view controllers
     */
    public SideBarViewController(final ViewControllersFactory viewControllersFactory) {
        this.viewControllersFactory = viewControllersFactory;
    }

    /**
     * Returns the view controllers factory associated with this sidebar.
     *
     * @return the view controllers factory
     */
    public ViewControllersFactory getViewControllersFactory() {
        return this.viewControllersFactory;
    }

    /**
     * Called automatically by {@code FXMLLoader} after the FXML is loaded.
     * Wires the navigation menu items to the top-level navigation handler.
     */
    @FXML
    public void initialize() {
        navDashboardItem.setOnAction(e -> navigationHandler.accept(AppContext.DASHBOARD));
        navConverterItem.setOnAction(e -> navigationHandler.accept(AppContext.CONVERTER));
        navPortfolioItem.setOnAction(e -> navigationHandler.accept(AppContext.INVESTMENTS));
        navSettingsItem.setOnAction(e -> navigationHandler.accept(AppContext.SETTINGS));
        if (totalBalanceLabel != null) {
            totalBalanceLabel.textProperty().addListener((obs, oldVal, newVal) -> updateWidthForTotal(newVal));
        }
        applyThemeAndRefresh();
        ThemeManager.addListener(theme -> Platform.runLater(this::applyThemeAndRefresh));
    }

    private void applyThemeAndRefresh() {
        if (closeButton != null) {
            ThemeManager.applyFont(closeButton);
        }
        if (menuButton != null) {
            ThemeManager.applyFont(menuButton);
        }
        if (totalTitleLabel != null) {
            ThemeManager.applyFont(totalTitleLabel);
        }
        if (totalBalanceLabel != null) {
            ThemeManager.applyFont(totalBalanceLabel);
        }
        refresh();
        if (totalBalanceLabel != null) {
            updateWidthForTotal(totalBalanceLabel.getText());
        }
    }

    /**
     * Closes the application. {@link Platform#exit()} (unlike {@code System.exit})
     * lets JavaFX call {@code App.stop()}, so wallets and window preferences are saved.
     */
    @FXML
    void handleCloseApp() {
        Platform.exit();
    }

    /**
     * Sets the top-level navigation handler invoked when a menu item is chosen.
     *
     * @param navigationHandler consumer receiving the selected {@link AppContext}
     */
    public void setNavigationHandler(final Consumer<AppContext> navigationHandler) {
        this.navigationHandler = Objects.requireNonNull(navigationHandler);
    }

    /**
     * Sets the delegate and immediately refreshes the sidebar.
     *
     * @param delegate the object supplying wallet items and handling selection events
     */
    public void setDelegate(final SideBarDelegate delegate) {
        this.delegate = delegate;
        refresh();
    }

    /**
     * Rebuilds the wallet tile list and updates the total balance label from the delegate.
     * Does nothing if the delegate has not been set.
     */
    public void refresh() {
        if (delegate == null) {
            return;
        }
        walletList.getChildren().clear();
        for (final var item : delegate.getItems()) {
            walletList.getChildren().add(createTile(item));
        }
        walletList.getChildren().add(createAddWalletTile());
        final String balanceText = delegate.getTotalAggregatedBalance();
        totalBalanceLabel.setText(balanceText);
        updateWidthForTotal(balanceText);
    }

    private Node createTile(final SideBarItem item) {
        final var tile = new VBox(4);
        tile.getStyleClass().add("wallet-tile");
        tile.setAlignment(Pos.CENTER_LEFT);
        final var nameLabel = new Label(item.name());
        nameLabel.getStyleClass().add("wallet-tile-name");
        final var balanceLabel = new Label(item.balance());
        balanceLabel.getStyleClass().add("wallet-tile-balance");
        ThemeManager.applyFont(nameLabel);
        ThemeManager.applyFont(balanceLabel);
        tile.getChildren().addAll(nameLabel, balanceLabel);
        if (item.selected()) {
            tile.getStyleClass().add("wallet-panel-selected");
        }
        tile.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                delegate.onItemSelected(item.id());
                refresh();
            }
        });
        final ContextMenu contextMenu = createTileContextMenu(item);
        tile.setOnContextMenuRequested(event -> {
            contextMenu.show(tile, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        return tile;
    }

    /**
     * Builds the right-click menu of a wallet tile (Edit / Delete).
     * The dialogs live here so that every context (dashboard, portfolio) gets the same UI;
     * the actual model change is delegated to the active {@link SideBarDelegate}.
     *
     * @param item the wallet tile the menu belongs to
     * @return the context menu
     */
    private ContextMenu createTileContextMenu(final SideBarItem item) {
        final MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> requestRename(item));
        final MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> requestDelete(item));
        return new ContextMenu(editItem, deleteItem);
    }

    /**
     * Asks the user for a new wallet name and forwards it to the delegate.
     * If the delegate rejects the name, the error is shown and the dialog reopens.
     *
     * @param item the wallet to rename
     */
    private void requestRename(final SideBarItem item) {
        final TextInputDialog dialog = new TextInputDialog(item.name());
        ThemeManager.applyThemeToDialog(dialog);
        dialog.initOwner(walletList.getScene().getWindow());
        dialog.setTitle("Rename wallet");
        dialog.setHeaderText("Rename \"" + item.name() + "\"");
        dialog.setContentText("New name:");
        WalletRenamePrompt.ask(dialog, name -> {
            if (!name.equals(item.name())) {
                delegate.onRenameRequested(item.id(), name);
                refresh();
            }
        });
    }

    /**
     * Asks for confirmation and forwards the deletion of a wallet to the delegate.
     *
     * @param item the wallet to delete
     */
    private void requestDelete(final SideBarItem item) {
        final boolean confirmed = DialogHelper.showConfirmation(
                walletList,
                "Delete wallet",
                "Delete \"" + item.name() + "\"?",
                "The wallet and its whole transaction history will be permanently removed.");
        if (confirmed) {
            delegate.onDeleteRequested(item.id());
            refresh();
        }
    }

    private void handleAddWallet() {
        delegate.onAddWalletRequested();
    }

    private Node createAddWalletTile() {
        final var btn = new Button("+ Add wallet");
        btn.getStyleClass().add("button-secondary");
        btn.setMaxWidth(Double.MAX_VALUE);  // occupa tutta la larghezza
        btn.setPrefHeight(ADD_WALLET_TILE_HEIGHT);
        btn.setOnAction(e -> handleAddWallet());
        ThemeManager.applyFont(btn);
        return btn;
    }

    /**
     * Sets the width adjustment handler invoked when the required sidebar width changes.
     *
     * @param handler consumer receiving the new target width in pixels
     */
    public void setWidthAdjustmentHandler(final Consumer<Double> handler) {
        this.widthAdjustmentHandler = Objects.requireNonNull(handler);
        if (totalBalanceLabel != null) {
            updateWidthForTotal(totalBalanceLabel.getText());
        }
    }

    /**
     * Computes the required width for the sidebar according to the total balance string,
     * maintaining at least the default minimum width, and informs the width adjustment handler.
     *
     * @param totalText the total balance text
     */
    public void updateWidthForTotal(final String totalText) {
        final double requiredWidth = computeRequiredWidth(totalText);
        widthAdjustmentHandler.accept(requiredWidth);
    }

    /**
     * Computes the required sidebar width based on the total balance text and minimum width.
     *
     * @param totalText the balance string
     * @return the width to set for the sidebar
     */
    public double computeRequiredWidth(final String totalText) {
        if (totalText == null || totalText.isBlank()) {
            return MIN_SIDEBAR_WIDTH;
        }
        final Text textMeasure = new Text(totalText);
        final String fontFamily = ThemeManager.getTheme() != null
                ? ThemeManager.getTheme().getFontFamily()
                : "System";
        textMeasure.setFont(Font.font(fontFamily, FontWeight.BOLD, TOTAL_FONT_SIZE));
        final double textWidth = textMeasure.getLayoutBounds().getWidth();
        final double needed = textWidth + TOTAL_BALANCE_HORIZONTAL_PADDING;
        return Math.min(MAX_SIDEBAR_WIDTH, Math.max(MIN_SIDEBAR_WIDTH, needed));
    }

}
