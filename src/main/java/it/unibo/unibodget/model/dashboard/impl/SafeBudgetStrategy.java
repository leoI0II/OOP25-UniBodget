package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;

import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Strategy representing a safe budget condition.
 * Serves as the default baseline fallback when neither critical nor warning thresholds are met.
 */
public final class SafeBudgetStrategy extends AbstractBudgetAlertStrategy {

    @Override
    public boolean applies(final BigDecimal currentValue, final BudgetSettings settings) {
        return true;
    }

    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BudgetSettings settings) {
        return BudgetStatus.SAFE;
    }

    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BigDecimal limitValue) {
        return BudgetStatus.SAFE;
    }
}
