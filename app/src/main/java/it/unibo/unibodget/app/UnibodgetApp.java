package it.unibo.unibodget.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class UnibodgetApp extends Application {
    @Override
    public void start(final Stage primaryStage) {
        final MainAppShell shell = InMemoryDashboardBootstrap.createShell();
        final Scene scene = new Scene(shell, 1440, 900);
        primaryStage.setTitle("UniBoDget");
        primaryStage.setScene(scene);
        primaryStage.show();
        shell.onShown();
    }
}
