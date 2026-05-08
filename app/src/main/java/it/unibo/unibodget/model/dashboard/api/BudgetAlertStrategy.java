package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;

/**
 * Defines a strategy used to evaluate the current budget state.
 * Different implementations represent different alert policies,
 * such as safe, warning, or critical budget conditions.
 */
public interface BudgetAlertStrategy {

    /**
     * Evaluates the budget state for the provided values.
     *
     * @param currentValue the current amount reached by the tracked budget
     * @param limitValue the configured budget limit
     * @return a textual representation of the evaluated state
     */
    BudgetStatus evaluate(BigDecimal currentValue, BigDecimal limitValue);
}
