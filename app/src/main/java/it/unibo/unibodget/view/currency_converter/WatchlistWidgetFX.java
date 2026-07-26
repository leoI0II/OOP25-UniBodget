package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.WatchListController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.watchlist.WatchlistPair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

/**
 * JavaFX widget that displays and manages the user's currency watchlist.
 * 
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
    public WatchlistWidgetFX(final WatchListController controller, final ConverterWidgetFX converter) {
        listView.setItems(items);
        items.addAll(controller.getFavorites());

        final Label title = new Label("My Watchlist");

        // Refresh highlighting when the converter selection changes
        converter.getFromBox().valueProperty().addListener((obs, old, val) -> listView.refresh());
        converter.getToBox().valueProperty().addListener((obs, old, val) -> listView.refresh());

        // Save button: stores the current converter pair into the watchlist
        final Button saveButton = new Button("Save couple in watchlist");
        saveButton.setOnAction(e -> {
            final String from = converter.getFromBox().getValue().getCode();
            final String to = converter.getToBox().getValue().getCode();

            if (from != null && to != null && !from.equals(to)) {
                final WatchlistPair newPair = new WatchlistPair(from, to);

                if (controller.addPair(newPair)) {
                    items.add(newPair);
                } else {
                    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
                final WatchlistPair selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    converter.setCurrencies(selected.from(), selected.to());
                }
            }
        });

        // Custom cell: highlights the currently active pair
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(final WatchlistPair item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item.toString());

                final CurrencyUnit currentFrom = converter.getFromBox().getValue();
                final CurrencyUnit currentTo = converter.getToBox().getValue();

                if (currentFrom != null && currentTo != null && !currentFrom.equals(currentTo)) {
                    final WatchlistPair currentPair =
                        new WatchlistPair(currentFrom.getCode(), currentTo.getCode());

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
