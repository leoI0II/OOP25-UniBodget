package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Strategy representing a safe budget condition.
 * This strategy is selected when the current value is clearly below the warning threshold.
 */

public final class SafeBudgetStrategy implements BudgetAlertStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BigDecimal limitValue) {
        return BudgetStatus.SAFE;
    }
}
