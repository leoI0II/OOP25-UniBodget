package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;

/**
 * Defines a strategy used to evaluate the current budget state. Different implementations represent
 * different alert policies, such as safe, warning, or critical budget conditions.
 */
public interface BudgetAlertStrategy {

    /**
     * Determines whether this alert strategy applies to the given current amount and budget settings.
     *
     * @param currentValue the current amount reached by the tracked budget
     * @param settings the configured budget settings
     * @return {@code true} if this strategy's condition is met, {@code false} otherwise
     */
    boolean applies(BigDecimal currentValue, BudgetSettings settings);

    /**
     * Evaluates the budget status for the provided current amount and budget settings.
     *
     * @param currentValue the current amount reached by the tracked budget
     * @param settings the configured budget settings
     * @return the evaluated {@link BudgetStatus}
     */
    BudgetStatus evaluate(BigDecimal currentValue, BudgetSettings settings);

    /**
     * Evaluates the budget state for the provided values.
     *
     * @param currentValue the current amount reached by the tracked budget
     * @param limitValue the configured budget limit
     * @return a textual representation of the evaluated state
     */
    BudgetStatus evaluate(BigDecimal currentValue, BigDecimal limitValue);
}
