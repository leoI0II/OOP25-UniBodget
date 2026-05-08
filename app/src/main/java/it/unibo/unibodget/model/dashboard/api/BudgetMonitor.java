package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;

/**
 * Service responsible for evaluating the current budget condition.
 * This component compares the current aggregated dashboard value with the
 * budget settings defined by the user and returns a corresponding budget status.
 */
public interface BudgetMonitor {

    /**
     * Computes the budget status associated with the provided current value.
     * @param currentValue the current aggregated amount considered by the monitor
     * @param settings the user-defined budget settings used during the evaluation
     * @return the resulting budget status
     */
    BudgetStatus getBudgetStatus(BigDecimal currentValue, BudgetSettings settings);
}
