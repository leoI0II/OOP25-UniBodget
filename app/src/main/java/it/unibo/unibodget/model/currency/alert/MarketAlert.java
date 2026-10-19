package it.unibo.unibodget.model.currency.alert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import java.util.List;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level component responsible for checking currency exchange alerts and displaying warning
 * dialogs when threshold conditions are met.
 *
 * <p>A {@code MarketAlert} delegates alert evaluation to a {@link CurrencyAlertService} and handles
 * the UI side-effects (JavaFX dialogs) when alerts are triggered. This class does not perform any
 * threshold logic itself; it only reacts to alerts already validated by the service.
 */
public final class MarketAlert {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketAlert.class);

    private final CurrencyAlertService alertService;

    /**
     * Creates a new {@code MarketAlert} instance.
     *
     * @param service the alert service used to evaluate threshold conditions; must not be {@code
     *     null}
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of currency alert service")
    public MarketAlert(final CurrencyAlertService service) {
        this.alertService = service;
    }

    /**
     * Checks the current exchange rate against all registered alerts and displays a warning dialog
     * for each triggered alert.
     *
     * <p>Behavior:
     *
     * <ul>
     *   <li>Delegates alert evaluation to {@link CurrencyAlertService#checkAlerts(double,
     *       CurrencyUnit, CurrencyUnit)}.
     *   <li>Iterates through all triggered alerts and shows a JavaFX {@link
     *       Alert.AlertType#WARNING} dialog for each.
     *   <li>Contains an optional conditional check (currently hardcoded to {@code rate > 0.5}) that
     *       can be used to skip certain alerts.
     * </ul>
     *
     * <p>This method produces UI side-effects and should be called from the JavaFX application
     * thread.
     *
     * @param rate the current exchange rate to evaluate
     * @param from the base currency of the rate being checked
     * @param to the target currency of the rate being checked
     */
    public void checkAndShowAlerts(
            final double rate, final CurrencyUnit from, final CurrencyUnit to) {
        final List<CurrencyAlert> triggered = alertService.checkAlerts(rate, from, to);

        for (final CurrencyAlert alert : triggered) {

            // Optional skip condition
            if (rate > 0.5) {
                LOGGER.debug(
                        "Rate is above 0.5, skipping alert for: {}",
                        alert.getTargetCurrency().getCode());
                // continue; // Uncomment to skip alerts above threshold
            }

            LOGGER.debug(
                    "Alert triggered for: {} at rate: {}",
                    alert.getTargetCurrency().getCode(),
                    rate);

            final Alert dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setTitle("Alert currency exchange");
            dialog.setHeaderText("Threshold exceeded for: " + alert.getTargetCurrency().getCode());
            dialog.setContentText("The current exchange rate " + rate + " has triggered an alert.");
            dialog.showAndWait();
        }
    }
}
