package it.unibo.unibodget.app;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

final class PlaceholderScreen extends BorderPane {
    PlaceholderScreen(final String title, final String message, final Runnable backAction) {
        final VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        final Label t = new Label(title);
        final Label m = new Label(message);
        final Button back = new Button("Back to dashboard");
        back.setOnAction(e -> backAction.run());
        box.getChildren().addAll(t, m, back);
        setCenter(box);
    }
}
