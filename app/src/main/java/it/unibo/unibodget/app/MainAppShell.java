package it.unibo.unibodget.app;

import javafx.scene.layout.BorderPane;

final class MainAppShell extends BorderPane implements AppNavigator {
    private final InMemoryDashboardBootstrap bootstrap;

    MainAppShell(final InMemoryDashboardBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    void onShown() {
        bootstrap.showFirstBootIfNeeded(this);
    }

    @Override public void showDashboard() { setCenter(bootstrap.dashboardView()); }
    @Override public void showTransactions() { setCenter(new PlaceholderScreen("Transactions", "Transactions view coming soon.", this::showDashboard)); }
    @Override public void showCategories() { setCenter(new PlaceholderScreen("Categories", "Categories view coming soon.", this::showDashboard)); }
    @Override public void showSettings() { setCenter(new PlaceholderScreen("Settings", "Settings view coming soon.", this::showDashboard)); }
    @Override public void showExternal() { setCenter(new PlaceholderScreen("External", "Other module coming soon.", this::showDashboard)); }
    @Override public void showFirstBoot() { bootstrap.runFirstBoot(this); }
    @Override public void showMessage(final String title, final String content) { setCenter(new PlaceholderScreen(title, content, this::showDashboard)); }
    @Override public void handleDashboardNavigation(final it.unibo.unibodget.view.dashboard.state.DashboardDestination destination) {
        switch (destination) {
            case DASHBOARD -> showDashboard();
            case TRANSACTIONS -> showTransactions();
            case CATEGORIES -> showCategories();
            case SETTINGS -> showSettings();
            case EXTERNAL_VIEW -> showExternal();
        }
    }
}
