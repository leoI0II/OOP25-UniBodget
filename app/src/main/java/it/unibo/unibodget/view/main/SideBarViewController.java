package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.utils.event.MessageEvent;
import it.unibo.unibodget.model.utils.event.NewWalletAddedEvent;
import it.unibo.unibodget.view.utils.ToastNotification;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

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
    private SideBarDelegate delegate;
    private final ViewControllersFactory viewControllersFactory;

    public SideBarViewController(ViewControllersFactory viewControllersFactory) {
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);
    }

    /**
     * Called automatically by {@code FXMLLoader} after the FXML is loaded.
     */
    @FXML
    public void initialize() { }

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
