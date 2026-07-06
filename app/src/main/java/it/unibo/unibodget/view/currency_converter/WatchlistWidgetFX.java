package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.WatchListController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.watchlist.WatchlistPair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * JavaFX widget that displays and manages the user's currency watchlist.
 * <p>
 * Allows saving the currently selected currency pair from the converter,
 * highlighting the active pair, and restoring a saved pair via double‑click.
 * The widget delegates all modifications to the {@link WatchListController}
 * to keep the MVC separation clean.
 */
public final class WatchlistWidgetFX {

    private final VBox view = new VBox(10);
    private final ListView<WatchlistPair> listView = new ListView<>();
    private final ObservableList<WatchlistPair> items = FXCollections.observableArrayList();

    /**
     * Creates the watchlist widget and binds it to the given controller and converter UI.
     *
     * @param controller the controller handling add/remove operations on the watchlist
     * @param converter  the converter widget used to read and set the selected currencies
     */
    public WatchlistWidgetFX(WatchListController controller, ConverterWidgetFX converter) {
        listView.setItems(items);
        final Label title = new Label("My Watchlist");

        // Refresh highlighting when the converter selection changes
        converter.fromBox.valueProperty().addListener((obs, old, val) -> listView.refresh());
        converter.toBox.valueProperty().addListener((obs, old, val) -> listView.refresh());

        // Save button: stores the current converter pair into the watchlist
        Button saveButton = new Button("Save couple in watchlist");
        saveButton.setOnAction(e -> {
            CurrencyUnit from = converter.fromBox.getValue();
            CurrencyUnit to = converter.toBox.getValue();

            if (from != null && to != null && !from.equals(to)) {
                WatchlistPair newPair = new WatchlistPair(from, to);

                if (controller.addPair(newPair)) {
                    items.add(newPair);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Watchlist");
                    alert.setHeaderText(null);
                    alert.setContentText("Already saved!");
                    alert.showAndWait();
                }
                listView.refresh();
            }
        });

        // Double‑click on a saved pair: load it into the converter
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                WatchlistPair selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    converter.setCurrencies(selected.from(), selected.to());
                }
            }
        });

        // Custom cell: highlights the currently active pair
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(WatchlistPair item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item.toString());

                CurrencyUnit currentFrom = converter.fromBox.getValue();
                CurrencyUnit currentTo = converter.toBox.getValue();

                if (currentFrom != null && currentTo != null && !currentFrom.equals(currentTo)) {
                    WatchlistPair currentPair = new WatchlistPair(currentFrom, currentTo);

                    if (item.equals(currentPair)) {
                        setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }
            }
        });

        view.getChildren().addAll(title, saveButton, listView);
    }

    /**
     * Returns the root JavaFX node representing this widget.
     *
     * @return the VBox containing the watchlist UI
     */
    public VBox getView() {
        return view;
    }
}
