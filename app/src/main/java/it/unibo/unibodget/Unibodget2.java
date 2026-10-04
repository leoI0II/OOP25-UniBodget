package it.unibo.unibodget;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.controller.settings.SettingsController;

import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.provider.AlphaPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.CoinGeckoPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.settings.ThemeManager;

import it.unibo.unibodget.view.currency_converter.CurrencyConverterViewFX;
import it.unibo.unibodget.view.settings.SettingsPopupFX;
import it.unibo.unibodget.view.settings.SettingsTabFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Minimal Home launcher for UniBodget.
 *
 * This class is the ONLY JavaFX Application.
 * All other FX views (converter, settings, watchlist) are launched from here.
 */
public final class Unibodget2 extends Application {

    @Override
    public void start(final Stage stage) {

        /* -------------------- CONTROLLERS -------------------- */
        final ExchangeRateAPI api = new ExchangeRateAPIImpl();

        String alphaVantageApiKey = "B56AX33NF4F29HHZ";
        System.getenv("ALPHA_VANTAGE_API_KEY");

        final UniversalPriceService priceService = new UniversalPriceService(
                api,
                new CoinGeckoPriceProvider(),
                new AlphaPriceProvider(alphaVantageApiKey)
        );

        final BasicCurrencyConverter converter =
                new BasicCurrencyConverter(priceService);

        final CurrencyConverterController currencyController =
                new CurrencyConverterController(api, converter);

        final SettingsController settingsController =
                new SettingsController();
                
        /* -------------------- BUTTONS -------------------- */

        final Button openConverter = new Button("Open Currency Converter");
        openConverter.setOnAction(e ->
                CurrencyConverterViewFX.launchWith(currencyController)
        );

        final Button openSettingsPopup = new Button("Open Settings Popup");
        openSettingsPopup.setOnAction(e ->
                new SettingsPopupFX(settingsController).show(stage)
        );

        /* -------------------- LAYOUT -------------------- */

        final VBox root = new VBox(20,
                openConverter,
                openSettingsPopup
                //openSettingsTab
        );
        root.setStyle("-fx-padding: 25;");

        //stage.setScene(new Scene(root, 350, 250));
        final Scene scene = new Scene(root, 350, 250);
        ThemeManager.applyThemeToScene(scene);
        stage.setScene(scene);

        stage.setTitle("UniBodget - Home");
        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }

}
