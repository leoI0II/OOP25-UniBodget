package it.unibo.unibodget.app;

import it.unibo.unibodget.view.dashboard.state.DashboardDestination;

/**
 * Navigator interface for switching between application views.
 */
public interface AppNavigator {

    /** Displays the dashboard view. */
    void showDashboard();

    /** Displays the transactions view. */
    void showTransactions();

    /** Displays the categories view. */
    void showCategories();

    /** Displays the settings view. */
    void showSettings();

    /** Displays the external view. */
    void showExternal();

    /** Displays the first boot view. */
    void showFirstBoot();

    /**
     * Displays a message dialog.
     *
     * @param title the message title
     * @param content the message content
     */
    void showMessage(String title, String content);

    /**
     * Handles navigation from the dashboard.
     *
     * @param destination the destination to navigate to
     */
    void handleDashboardNavigation(DashboardDestination destination);
}
