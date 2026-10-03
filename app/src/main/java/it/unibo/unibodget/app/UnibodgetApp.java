package it.unibo.unibodget.app;

import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.provider.AlphaPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.CoinGeckoPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.settings.Settings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX launcher that initializes the application using an in-memory bootstrap.
 *
 * <p>Creates the settings, currency conversion service and main application
 * shell, then displays the primary window.</p>
 */
public final class UnibodgetApp extends Application {

    /**
     * Initializes and displays the main application window.
     *
     * <p>Configures fiat, cryptocurrency and stock price providers, creates
     * the conversion engine and notifies the shell after the window is shown.</p>
     *
     * @param primaryStage the main application window
     */
    @Override
    public void start(final Stage primaryStage) {
        final Settings settings = new Settings();
        final UniversalPriceService priceService = new UniversalPriceService(
                new ExchangeRateAPIImpl(),
                new CoinGeckoPriceProvider(),
                new AlphaPriceProvider("B56AX33NF4F29HHZ")
        );

        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(priceService);

        final MainAppShell shell = InMemoryDashboardBootstrap.createShell(converter, settings);
        final Scene scene = new Scene(shell, 1440, 900);
        primaryStage.setTitle("UniBoDget");
        primaryStage.setScene(scene);
        primaryStage.show();
        shell.onShown();
    }
    
}
