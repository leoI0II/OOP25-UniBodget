package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;

/**
 * Default implementation of {@link BudgetMonitor}.
 * <p>
 * This implementation delegates the evaluation of the budget state
 * to one of three strategies according to the ratio between the
 * current value and the configured limit.
 */
public final class DefaultBudgetMonitor implements BudgetMonitor {

    private static final double WARNING_THRESHOLD = 0.8;
    private static final double CRITICAL_THRESHOLD = 1.0;

    private final BudgetAlertStrategy safeStrategy;
    private final BudgetAlertStrategy warningStrategy;
    private final BudgetAlertStrategy criticalStrategy;

    /**
     * Creates a monitor with default safe, warning, and critical strategies.
     */
    public DefaultBudgetMonitor() {
        this(new SafeBudgetStrategy(), new WarningBudgetStrategy(), new CriticalBudgetStrategy());
    }

    /**
     * Creates a monitor with custom strategies.
     *
     * @param safeStrategy the strategy used for safe conditions
     * @param warningStrategy the strategy used for warning conditions
     * @param criticalStrategy the strategy used for critical conditions
     */
    public DefaultBudgetMonitor(
            final BudgetAlertStrategy safeStrategy,
            final BudgetAlertStrategy warningStrategy,
            final BudgetAlertStrategy criticalStrategy) {
        this.safeStrategy = safeStrategy;
        this.warningStrategy = warningStrategy;
        this.criticalStrategy = criticalStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBudgetStatus(final double currentValue, final double limitValue) {
        if (limitValue <= 0) {
            return safeStrategy.evaluate(currentValue, limitValue);
        }
        final double ratio = currentValue / limitValue;
        if (ratio >= CRITICAL_THRESHOLD) {
            return criticalStrategy.evaluate(currentValue, limitValue);
        }
        if (ratio >= WARNING_THRESHOLD) {
            return warningStrategy.evaluate(currentValue, limitValue);
        }
        return safeStrategy.evaluate(currentValue, limitValue);
    }
}