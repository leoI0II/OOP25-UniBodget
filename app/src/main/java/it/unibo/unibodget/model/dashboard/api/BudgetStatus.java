package it.unibo.unibodget.model.dashboard.api;

/**
 * Represents the budget state shown in the dashboard.
 * The status is derived by comparing the current aggregated value of the
 * tracked transactions with the budget configuration defined by the user.
 */
public enum BudgetStatus {
    /**
     * Indicates that the current value is comfortably below the configured budget limit.
     */
    SAFE,

    /**
     * Indicates that the current value is approaching the configured budget limit.
     */
    WARNING,

    /**
     * Indicates that the current value has reached or exceeded the configured budget limit.
     */
    CRITICAL
}
