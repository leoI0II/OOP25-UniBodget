package it.unibo.unibodget.view.main;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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

    public SideBarViewController(ViewControllersFactory viewControllersFactory) {
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
        final var tile = new VBox();
        tile.getChildren().addAll(
                new Label(item.name()),
                new Label(item.balance())
        );
        if (item.selected()) {
            tile.getStyleClass().add("wallet-panel-selected");
        }
        tile.setOnMouseClicked(event -> {
            delegate.onItemSelected(item.id());
            refresh();
        });
        return tile;
    }

    private void handleAddWallet() {
        delegate.onAddWalletRequested();
    }

    private Node createAddWalletTile() {
        final var btn = new Button("+ Add wallet");
        btn.setMaxWidth(Double.MAX_VALUE);  // occupa tutta la larghezza
        btn.setPrefHeight(ADD_WALLET_TILE_HEIGHT);
        btn.setOnAction(e -> handleAddWallet());
        return btn;
    }
}
