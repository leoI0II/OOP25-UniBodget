package it.unibo.unibodget.view.main;

import java.util.Objects;
import java.util.function.Consumer;

import it.unibo.unibodget.model.settings.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

/**
 * JavaFX controller for the sidebar view.
 *
 * <p>Renders the list of wallet tiles and the aggregated total balance label.
 * Delegates data retrieval and selection callbacks to a {@link SideBarDelegate}.</p>
 */
public class SideBarViewController extends BaseViewController {

    private static final int ADD_WALLET_TILE_HEIGHT = 50;

    @FXML private VBox walletList;
    @FXML private Label totalBalanceLabel;
    @FXML private MenuItem navDashboardItem;
    @FXML private MenuItem navConverterItem;
    @FXML private MenuItem navPortfolioItem;
    @FXML private MenuItem navSettingsItem;
    private SideBarDelegate delegate;
    private Consumer<AppContext> navigationHandler = context -> { };
    private final ViewControllersFactory viewControllersFactory;

    /**
     * Constructs a new SideBarViewController with the given factory.
     *
     * @param viewControllersFactory the factory used to create view controllers
     */
    public SideBarViewController(final ViewControllersFactory viewControllersFactory) {
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);
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
    }

    /**
     * Closes the application. {@link Platform#exit()} (unlike {@code System.exit})
     * lets JavaFX call {@code App.stop()}, so wallets and window preferences are saved.
     */
    @FXML
    private void handleCloseApp() {
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
        totalBalanceLabel.setText(delegate.getTotalAggregatedBalance());
    }

    private Node createTile(final SideBarItem item) {
        final var tile = new VBox(4);
        tile.getStyleClass().add("wallet-tile");
        tile.setAlignment(Pos.CENTER_LEFT);
        final var nameLabel = new Label(item.name());
        nameLabel.getStyleClass().add("wallet-tile-name");
        final var balanceLabel = new Label(item.balance());
        balanceLabel.getStyleClass().add("wallet-tile-balance");
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
        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.applyThemeToDialog(confirm);
        confirm.initOwner(walletList.getScene().getWindow());
        confirm.setTitle("Delete wallet");
        confirm.setHeaderText("Delete \"" + item.name() + "\"?");
        confirm.setContentText("The wallet and its whole transaction history will be permanently removed.");
        confirm.showAndWait()
                .filter(ButtonType.OK::equals)
                .ifPresent(ok -> {
                    delegate.onDeleteRequested(item.id());
                    refresh();
                });
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
        return btn;
    }
}
