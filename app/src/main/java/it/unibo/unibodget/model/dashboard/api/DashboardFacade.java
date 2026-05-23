package it.unibo.unibodget.model.dashboard.api;

import it.unibo.unibodget.model.categories.CategoryCatalog;

/**
 * Facade exposing the dashboard read model and shared category data required by
 * dashboard-related user flows.
 */
public interface DashboardFacade {

    /**
     * Loads the current dashboard snapshot.
     *
     * @return the immutable snapshot representing the current dashboard state
     */
    DashboardSnapshot loadDashboard();

    /**
     * Returns the shared category catalog used by dashboard flows such as
     * transaction creation.
     *
     * @return the shared category catalog
     */
    CategoryCatalog getCategoryCatalog();
} 