package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;

/**
 * Strategy representing a critical budget condition.
 */
public final class CriticalBudgetStrategy implements BudgetAlertStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String evaluate(final double currentValue, final double limitValue) {
        return "CRITICAL";
    }
}