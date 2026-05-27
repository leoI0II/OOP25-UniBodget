package it.unibo.unibodget.view.main;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SideBarViewController {

    @FXML private VBox walletList;
    @FXML private Label totalBalanceLabel;
    private SideBarDelegate delegate;

    @FXML
    public void initialize() {
        // chiamato automaticamente da FXMLLoader dopo il caricamento
    }

    public void setDelegate(final SideBarDelegate delegate) {
        this.delegate = delegate;
        refresh();
    }

    public void refresh() {
        if (delegate == null) return;
        walletList.getChildren().clear();
        for (var item : delegate.getItems()) {
            walletList.getChildren().add(createTile(item));
        }
        walletList.getChildren().add(createAddWalletTile()); // sempre in fondo
        totalBalanceLabel.setText(delegate.getTotalAggregatedBalance());
    }

    private Node createTile(final SideBarItem item) {
        var tile = new VBox();
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

    }

    private Node createAddWalletTile() {
        var btn = new Button("+ Add wallet");
        btn.setMaxWidth(Double.MAX_VALUE);  // occupa tutta la larghezza
        btn.setPrefHeight(50);
        btn.setOnAction(e -> handleAddWallet());
        return btn;
    }

}
