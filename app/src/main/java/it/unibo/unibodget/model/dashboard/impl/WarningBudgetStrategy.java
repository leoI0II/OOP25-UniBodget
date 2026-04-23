package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;

/**
 * Strategy representing an approaching-limit budget condition.
 */
public final class WarningBudgetStrategy implements BudgetAlertStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String evaluate(final double currentValue, final double limitValue) {
        return "WARNING";
    }
}