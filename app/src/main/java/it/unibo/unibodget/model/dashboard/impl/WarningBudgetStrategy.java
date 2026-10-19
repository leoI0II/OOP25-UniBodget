package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import java.math.BigDecimal;

/** Strategy representing an approaching-limit budget condition. */
public final class WarningBudgetStrategy implements BudgetAlertStrategy {

    /** {@inheritDoc} */
    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BigDecimal limitValue) {
        return BudgetStatus.WARNING;
    }
}
