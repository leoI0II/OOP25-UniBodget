package it.unibo.unibodget.model.currency.alert;

import javafx.scene.control.Alert;
import java.util.List;

import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * High-level component responsible for checking currency exchange alerts
 * and displaying warning dialogs when threshold conditions are met.
 * <p>
 * A {@code MarketAlert} delegates alert evaluation to a
 * {@link CurrencyAlertService} and handles the UI side-effects (JavaFX dialogs)
 * when alerts are triggered. This class does not perform any threshold logic
 * itself; it only reacts to alerts already validated by the service.
 */
public class MarketAlert {

    private final CurrencyAlertService alertService;

    /**
     * Creates a new {@code MarketAlert} instance.
     *
     * @param service the alert service used to evaluate threshold conditions;
     *                must not be {@code null}
     */
    public MarketAlert(CurrencyAlertService service) {
        this.alertService = service;
    }

    /**
     * Checks the current exchange rate against all registered alerts and displays
     * a warning dialog for each triggered alert.
     * <p>
     * Behavior:
     * <ul>
     *     <li>Delegates alert evaluation to
     *         {@link CurrencyAlertService#checkAlerts(double, CurrencyUnit, CurrencyUnit)}.</li>
     *     <li>Iterates through all triggered alerts and shows a JavaFX
     *         {@link Alert.AlertType#WARNING} dialog for each.</li>
     *     <li>Contains an optional conditional check (currently hardcoded to {@code rate > 0.5})
     *         that can be used to skip certain alerts.</li>
     * </ul>
     * <p>
     * This method produces UI side-effects and should be called from the JavaFX
     * application thread.
     *
     * @param rate the current exchange rate to evaluate
     * @param from the base currency of the rate being checked
     * @param to   the target currency of the rate being checked
     */
    public void checkAndShowAlerts(double rate, CurrencyUnit from, CurrencyUnit to) {
        List<CurrencyAlert> triggered = alertService.checkAlerts(rate, from, to);

        for (CurrencyAlert alert : triggered) {

            // Optional skip condition
            if (rate > 0.5) {
                System.out.println("Rate is above 0.5, skipping alert for: "
                        + alert.getTargetCurrency().getCode());
                // continue; // Uncomment to skip alerts above threshold
            }

            System.out.println("Alert triggered for: "
                    + alert.getTargetCurrency().getCode()
                    + " at rate: " + rate);

            Alert dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setTitle("Alert currency exchange");
            dialog.setHeaderText("Threshold exceeded for: "
                    + alert.getTargetCurrency().getCode());
            dialog.setContentText("The current exchange rate "
                    + rate
                    + " has triggered an alert.");
            dialog.showAndWait();
        }
    }
}
