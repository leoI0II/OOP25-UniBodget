package it.unibo.unibodget.model.dashboard.impl;

import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Default implementation of {@link BudgetMonitor}.
 * This implementation delegates the evaluation of the budget state
 * to one of three strategies according to the ratio between the
 * current value and the configured limit.
 */
public final class DefaultBudgetMonitor implements BudgetMonitor {

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
        this.safeStrategy = Objects.requireNonNull(safeStrategy);
        this.warningStrategy = Objects.requireNonNull(warningStrategy);
        this.criticalStrategy = Objects.requireNonNull(criticalStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BudgetStatus getBudgetStatus(final double currentValue, final BudgetSettings settings) {
        Objects.requireNonNull(settings);
        final double limitValue = settings.getLimitValue();
        if (limitValue <= 0) {
            return safeStrategy.evaluate(currentValue, limitValue);
        }
        final double ratio = currentValue / limitValue;
        if (ratio >= CRITICAL_THRESHOLD) {
            return criticalStrategy.evaluate(currentValue, limitValue);
        }
        if (ratio >= settings.getWarningThreshold()) {
            return warningStrategy.evaluate(currentValue, limitValue);
        }
        return safeStrategy.evaluate(currentValue, limitValue);
    }
}