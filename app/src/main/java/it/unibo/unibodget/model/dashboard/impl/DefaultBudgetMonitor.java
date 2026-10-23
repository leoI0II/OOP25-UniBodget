package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Default implementation of {@link BudgetMonitor}.
 *
 * <p>
 * This implementation delegates the evaluation of the budget state polymorphically
 * to an ordered list of {@link BudgetAlertStrategy} instances (by default: critical,
 * warning, and safe fallback).
 * </p>
 */
public final class DefaultBudgetMonitor implements BudgetMonitor {

    private final List<BudgetAlertStrategy> strategies;

    /**
     * Creates a monitor with default critical, warning, and safe strategies in evaluation order.
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
        Objects.requireNonNull(safeStrategy, "safeStrategy must not be null");
        Objects.requireNonNull(warningStrategy, "warningStrategy must not be null");
        Objects.requireNonNull(criticalStrategy, "criticalStrategy must not be null");
        this.strategies = List.of(criticalStrategy, warningStrategy, safeStrategy);
    }

    /**
     * Creates a monitor with an explicit ordered list of strategies.
     *
     * @param strategies the ordered alert strategies
     */
    public DefaultBudgetMonitor(final List<BudgetAlertStrategy> strategies) {
        Objects.requireNonNull(strategies, "strategies must not be null");
        this.strategies = List.copyOf(strategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BudgetStatus getBudgetStatus(final BigDecimal currentValue, final BudgetSettings settings) {
        Objects.requireNonNull(currentValue, "currentValue must not be null");
        Objects.requireNonNull(settings, "settings must not be null");

        for (final BudgetAlertStrategy strategy : strategies) {
            if (strategy.applies(currentValue, settings)) {
                return strategy.evaluate(currentValue, settings);
            }
        }

        return BudgetStatus.SAFE;
    }
}
