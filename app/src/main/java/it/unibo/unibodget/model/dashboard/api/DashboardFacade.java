
package it.unibo.unibodget.model.dashboard.api;

/**
 * Represents the main access point to dashboard data.
 * The facade hides the internal collaboration among the dashboard
 * services and returns a unified snapshot ready to be consumed by
 * presentation components.
 */
public interface DashboardFacade {

    /**
     * Loads the current dashboard state as an aggregated snapshot.
     *
     * @return the current dashboard snapshot
     */
    DashboardSnapshot loadDashboard();
}
