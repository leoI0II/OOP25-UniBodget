package it.unibo.unibodget.app;

import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.settings.Settings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class UnibodgetApp extends Application {
    @Override
    public void start(final Stage primaryStage) {
        final Settings settings = new Settings();
        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(new ExchangeRateAPIImpl(), settings.getBaseCurrencyUnit());
        final MainAppShell shell = InMemoryDashboardBootstrap.createShell(converter, settings);
        final Scene scene = new Scene(shell, 1440, 900);
        primaryStage.setTitle("UniBoDget");
        primaryStage.setScene(scene);
        primaryStage.show();
        shell.onShown();
    }
}
