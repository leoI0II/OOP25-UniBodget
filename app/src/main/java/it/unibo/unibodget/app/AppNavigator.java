package it.unibo.unibodget.app;

import it.unibo.unibodget.view.dashboard.state.DashboardDestination;

public interface AppNavigator {
    void showDashboard();
    void showTransactions();
    void showCategories();
    void showSettings();
    void showExternal();
    void showFirstBoot();
    void showMessage(String title, String content);
    void handleDashboardNavigation(DashboardDestination destination);
}
