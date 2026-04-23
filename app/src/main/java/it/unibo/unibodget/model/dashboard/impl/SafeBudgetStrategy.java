package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;

/**
 * Strategy representing a safe budget condition.
 */
public final class SafeBudgetStrategy implements BudgetAlertStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String evaluate(final double currentValue, final double limitValue) {
        return "SAFE";
    }
}