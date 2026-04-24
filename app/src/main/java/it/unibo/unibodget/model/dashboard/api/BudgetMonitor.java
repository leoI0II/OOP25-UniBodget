package it.unibo.unibodget.model.dashboard.api;

/**
 * Provides budget monitoring facilities for the dashboard subsystem.
 * The monitor coordinates one or more alert strategies in order to
 * determine the current budget status shown in the dashboard.
 */
public interface BudgetMonitor {

    /**
     * Computes the current budget status for the given values.
     *
     * @param currentValue the current amount reached by the tracked budget
     * @param limitValue the configured budget limit
     * @return the current budget status
     */
    String getBudgetStatus(double currentValue, double limitValue);
}