package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Strategy representing a critical budget condition.
 */
public final class CriticalBudgetStrategy implements BudgetAlertStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BigDecimal limitValue) {
        return BudgetStatus.CRITICAL;
    }
}