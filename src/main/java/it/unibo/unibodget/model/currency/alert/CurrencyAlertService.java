package it.unibo.unibodget.model.currency.alert;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for managing and evaluating currency threshold alerts.
 *
 * <p>A {@code CurrencyAlertService} stores multiple {@link CurrencyAlert} instances and checks them
 * against the latest exchange rate retrieved from an external API.
 *
 * <p>The service does not fetch exchange rates itself; it only evaluates alerts based on the rate
 * and currency pair provided by the caller.
 */
public final class CurrencyAlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyAlertService.class);

    private final List<CurrencyAlert> activeAlerts = new ArrayList<>();

    /**
     * Registers a new alert to be monitored.
     *
     * <p>Alerts are stored in insertion order. No duplicate prevention is performed, so callers
     * should ensure they do not add redundant alerts.
     *
     * @param alert the alert to register; must not be {@code null}
     */
    public void addAlert(final CurrencyAlert alert) {
        activeAlerts.add(alert);
    }

    /**
     * Evaluates all registered alerts against the given exchange rate and currency pair.
     *
     * <p>Behavior:
     *
     * <ul>
     *   <li>Only alerts whose currency pair matches {@code from → to} are evaluated.
     *   <li>Each alert's {@link CurrencyAlert#check(double, CurrencyUnit, CurrencyUnit)} method
     *       determines whether the threshold condition is met.
     *   <li>All triggered alerts are collected and returned.
     * </ul>
     *
     * <p>This method does not modify the alert list; it only reports which alerts should trigger
     * based on the provided rate.
     *
     * @param currentRate the latest exchange rate to evaluate
     * @param from the base currency of the rate being checked
     * @param to the target currency of the rate being checked
     * @return a list containing all alerts that triggered; may be empty
     */
    public List<CurrencyAlert> checkAlerts(
            final double currentRate, final CurrencyUnit from, final CurrencyUnit to) {
        LOGGER.debug("Numb alert registered: {}", activeAlerts.size());
        LOGGER.debug("Check alert for: {} -> {} a tasso {}", from.getCode(), to.getCode(), currentRate);

        final List<CurrencyAlert> triggered = new ArrayList<>();

        for (final CurrencyAlert alert : activeAlerts) {
            final boolean match = alert.check(currentRate, from, to);
            LOGGER.debug("Alert find in list, match: {}", match);
            if (match) {
                triggered.add(alert);
            }
        }

        return triggered;
    }
}
