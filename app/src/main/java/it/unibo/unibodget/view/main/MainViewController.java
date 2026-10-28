package it.unibo.unibodget.view.main;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.MainInfoNotificationEvent;
import it.unibo.unibodget.model.utils.event.OrderResultEvent;
import it.unibo.unibodget.view.investments.InvestmentsViewController;
import it.unibo.unibodget.view.settings.SettingsPopupFX;
import it.unibo.unibodget.view.utils.AssetFormatter;
import it.unibo.unibodget.view.utils.ToastNotification;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Root JavaFX controller for the main application window. Owns the {@link SideBarViewController}
 * and a {@link StackPane} content area, into which it swaps different sub-views (investments,
 * dashboard, …). Also handles application-level toast notifications via the message bus.
 */
public final class MainViewController extends BaseViewController {

    @FXML private SplitPane mainSplitPane;
    @FXML private Region sideBar;
    @FXML private SideBarViewController sideBarController;
    @FXML private StackPane contentArea;
    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final DashboardModule dashboardModule;
    private final CurrencyConverterModule converterModule;
    private final ViewControllersFactory viewControllersFactory;
    private final SettingsController settingsController;

    /**
     * Ref to the currently shown sub-controller, used to call {@link BaseViewController#dispose()}
     * on navigation.
     */
    private BaseViewController currentVC;

    /**
     * Last wallet-based context shown (DASHBOARD or INVESTMENTS); used to restore it from the
     * converter.
     */
    private AppContext lastWalletContext = AppContext.DASHBOARD;

    /**
     * Creates the main controller wiring together all required services.
     *
     * @param investmentController handles investment business logic
     * @param snapshotService provides historical balance snapshots
     * @param dashboardModule the dashboard MVC graph embedded as content
     * @param converterModule the currency-converter view embedded as content
     * @param viewControllersFactory factory used to build sub-view controllers
     * @param settingsController controller for application settings
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of controllers and services")
    public MainViewController(
            final InvestmentController investmentController,
            final InvestmentsSnapshotService snapshotService,
            final DashboardModule dashboardModule,
            final CurrencyConverterModule converterModule,
            final ViewControllersFactory viewControllersFactory,
            final SettingsController settingsController) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.dashboardModule = Objects.requireNonNull(dashboardModule);
        this.converterModule = Objects.requireNonNull(converterModule);
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);
        this.settingsController = Objects.requireNonNull(settingsController);

        subscribe(OrderResultEvent.class, this::onOrderResultEvent);
        subscribe(MainErrorNotificationEvent.class, this::onMainErrorNotificationEvent);
        subscribe(MainInfoNotificationEvent.class, this::onMainInfoNotificationEvent);
    }

    /**
     * Returns the investments snapshot service.
     *
     * @return the snapshot service
     */
    public InvestmentsSnapshotService getSnapshotService() {
        return snapshotService;
    }

    /**
     * Called automatically by {@link javafx.fxml.FXMLLoader} after the FXML is loaded. Shows the
     * dashboard as the default landing content.
     */
    @FXML
    public void initialize() {
        sideBarController.setNavigationHandler(this::navigateTo);
        sideBarController.setWidthAdjustmentHandler(width -> {
            if (width != null) {
                adjustSideBarWidth(width);
            }
        });
        setupResizableSideBar();
        showDashboard();
    }

    /**
     * Adjusts the sidebar width dynamically based on content requirements.
     *
     * @param targetWidth the desired width in pixels
     */
    private void adjustSideBarWidth(final double targetWidth) {
        if (sideBar == null || mainSplitPane == null) {
            return;
        }
        final double clampedWidth = Math.min(
                SideBarViewController.MAX_SIDEBAR_WIDTH,
                Math.max(SideBarViewController.MIN_SIDEBAR_WIDTH, targetWidth));
        sideBar.setPrefWidth(clampedWidth);
        sideBar.setMinWidth(clampedWidth);
        Platform.runLater(() -> {
            final double splitWidth = mainSplitPane.getWidth();
            if (splitWidth > 0) {
                mainSplitPane.setDividerPositions(clampedWidth / splitWidth);
            }
        });
    }

    /**
     * Lets the user resize the sidebar by dragging the divider, between its min and
     * max width (set in {@code SideBarView.fxml}). When the window changes size, only
     * the content area grows, the sidebar keeps its width.
     */
    private void setupResizableSideBar() {
        // when the window is resized, the sidebar dont change its width
        SplitPane.setResizableWithParent(sideBar, false);
        // the divider position is a % of the total width (0.0 - 1.0), but the width is known
        // only after the first layout, so we wait for it, put the sidebar at its pref width
        // (pref / total, e.g. 200 / 1000 = 0.2) and then remove the listener, we need it only once
        mainSplitPane.widthProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(final ObservableValue<? extends Number> obs,
                                final Number oldWidth, final Number newWidth) {
                if (newWidth.doubleValue() > 0) {
                    mainSplitPane.setDividerPositions(sideBar.getPrefWidth() / newWidth.doubleValue());
                    mainSplitPane.widthProperty().removeListener(this);
                }
            }
        });
    }

    /**
     * Handles a top-level navigation request coming from the shared sidebar menu.
     * Loads the corresponding context into the content area.
     *
     * @param context the selected application context
     */
    private void navigateTo(final AppContext context) {
        switch (context) {
            case DASHBOARD -> showDashboard();
            case INVESTMENTS -> showInvestments();
            case CONVERTER -> showConverter();
            case SETTINGS -> showSettings();
            default -> { }
        }
    }

    /**
     * Loads and shows the dashboard view in the content area. Disposes the previously active
     * sub-controller if present.
     */
    @FXML
    public void showDashboard() {
        lastWalletContext = AppContext.DASHBOARD;
        if (currentVC != null) {
            currentVC.dispose();
        }
        final DashboardViewController delegate =
                new DashboardViewController(
                        dashboardModule.getFacade(),
                        dashboardModule.getController(),
                        sideBarController);
        currentVC = delegate;
        sideBarController.setDelegate(delegate);
        // Keep the shared sidebar in sync with any dashboard mutation (transactions, budget, ...).
        dashboardModule.getController().setOnDashboardRefreshed(sideBarController::refresh);

        contentArea.getChildren().setAll(dashboardModule.getView());
        // Render the dashboard center with fresh data for this navigation.
        dashboardModule.getController().onViewOpened();
        if (contentArea.getScene() != null) {
            ThemeManager.applyThemeToScene(contentArea.getScene());
        }
        contentArea.applyCss();
        contentArea.layout();
    }

    /**
     * Loads and shows the investments view in the content area. Disposes the previously active
     * sub-controller if present, then selects the first account.
     */
    @FXML
    public void showInvestments() {
        lastWalletContext = AppContext.INVESTMENTS;
        if (currentVC != null) {
            currentVC.dispose();
        }
        final FXMLLoader fxmlLoader =
                new FXMLLoader(
                        getClass()
                                .getResource(
                                        "/it/unibo/unibodget/view/jfx/fxml/investments/InvestmentsView.fxml"));
        fxmlLoader.setControllerFactory(viewControllersFactory::create);
        final Node view;
        try {
            view = fxmlLoader.load();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to load InvestmentsView", e);
        }
        final InvestmentsViewController ivc = fxmlLoader.getController();
        currentVC = ivc;
        ivc.setSideBarViewController(sideBarController);
        sideBarController.setDelegate(ivc);

        // Attach the view to the scene BEFORE selecting a wallet: the initial
        // selection triggers a full refresh that expects the view to be live.
        contentArea.getChildren().setAll(view);
        if (!investmentController.getAllInvestmentAccounts().isEmpty()) {
            ivc.onItemSelected(investmentController.getAllInvestmentAccounts().getFirst().getId());
        }
        ThemeManager.applyThemeToScene(contentArea.getScene());
        contentArea.applyCss();
        contentArea.layout();
    }

    /**
     * Shows the currency-converter dashboard in the content area.
     *
     * <p>The converter is not wallet-based, so the shared sidebar keeps showing the wallets of the
     * last active wallet context ({@link #lastWalletContext}). Selecting a wallet (or adding one)
     * returns to that context and applies the action there.
     */
    @FXML
    public void showConverter() {
        final SideBarDelegate walletDelegate = currentVC instanceof SideBarDelegate d ? d : null;
        final AppContext restoreContext = lastWalletContext;

        sideBarController.setDelegate(
                new SideBarDelegate() {
                    @Override
                    public List<SideBarItem> getItems() {
                        return walletDelegate == null ? List.of() : walletDelegate.getItems();
                    }

                    @Override
                    public String getTotalAggregatedBalance() {
                        return walletDelegate == null
                                ? ""
                                : walletDelegate.getTotalAggregatedBalance();
                    }

                    @Override
                    public void onItemSelected(final UUID id) {
                        restoreWalletContext(restoreContext);
                        if (currentVC instanceof SideBarDelegate d) {
                            d.onItemSelected(id);
                        }
                    }

                    @Override
                    public void onAddWalletRequested() {
                        restoreWalletContext(restoreContext);
                        if (currentVC instanceof SideBarDelegate d) {
                            d.onAddWalletRequested();
                        }
                    }

                    @Override
                    public void onRenameRequested(final UUID id, final String newName) {
                        restoreWalletContext(restoreContext);
                        if (currentVC instanceof SideBarDelegate d) {
                            d.onRenameRequested(id, newName);
                        }
                    }

                    @Override
                    public void onDeleteRequested(final UUID id) {
                        restoreWalletContext(restoreContext);
                        if (currentVC instanceof SideBarDelegate d) {
                            d.onDeleteRequested(id);
                        }
                    }
                });

        contentArea.getChildren().setAll(converterModule.getView());
        if (contentArea.getScene() != null) {
            ThemeManager.applyThemeToScene(contentArea.getScene());
        }
        contentArea.applyCss();
        contentArea.layout();
    }

    /**
     * Shows the settings popup dialog, allowing the user to change application preferences.
     *
     * <p>The dialog is modal and blocks interaction with the main window until closed. Changes are
     * applied through the {@link SettingsController}, which handles persistence and theme updates.
     * Opens settings and refreshes the active view after a currency change.
     */
    private void showSettings() {
        if (contentArea.getScene() == null) {
            return;
        }
        final Window window = contentArea.getScene().getWindow();
        if (!(window instanceof Stage owner)) {
            return;
        }
        final String previousCurrency = settingsController.getSettings().getBaseCurrency();

        new SettingsPopupFX(settingsController).show(owner);

        final String currentCurrency = settingsController.getSettings().getBaseCurrency();

        if (!previousCurrency.equals(currentCurrency)) {
            if (currentVC instanceof InvestmentsViewController investmentsView) {
                investmentsView.refreshData();
            } else {
                dashboardModule.getController().onViewOpened();
            }

            sideBarController.refresh();
        }

        ThemeManager.applyThemeToScene(owner.getScene());
        contentArea.applyCss();
        contentArea.layout();
    }

    /**
     * Restores a wallet-based context (dashboard or investments) into the content area.
     *
     * @param context the wallet context to restore
     */
    private void restoreWalletContext(final AppContext context) {
        if (context == AppContext.INVESTMENTS) {
            showInvestments();
        } else {
            showDashboard();
        }
    }

    /**
     * Shows a toast notification describing the outcome of an investment order.
     *
     * @param event the order-result event received from the message bus
     */
    private void onOrderResultEvent(final OrderResultEvent event) {
        final var window = contentArea.getScene().getWindow();
        switch (event.result()) {
            case OrderResult.InsufficientFunds f ->
                    ToastNotification.showError(
                            window,
                            "Insufficient funds. Required: "
                                    + AssetFormatter.ofAsset(f.required())
                                    + ", available: "
                                    + AssetFormatter.ofAsset(f.available()));
            case OrderResult.InsufficientAssets a ->
                    ToastNotification.showError(
                            window,
                            "Insufficient assets. Required: "
                                    + a.requested()
                                    + ", available: "
                                    + a.available());
            default -> {
                if (event.result().isSuccess()) {
                    ToastNotification.showSuccess(window, "Transaction executed successfully!");
                }
            }
        }
    }

    /**
     * Shows an error toast triggered by a {@link MainErrorNotificationEvent} on the message bus.
     *
     * @param event the error notification event
     */
    private void onMainErrorNotificationEvent(final MainErrorNotificationEvent event) {
        final var window = contentArea.getScene().getWindow();
        ToastNotification.showError(window, event.msg());
    }

    /**
     * Shows an info toast triggered by a {@link MainInfoNotificationEvent} on the message bus.
     *
     * @param event the info notification event
     */
    private void onMainInfoNotificationEvent(final MainInfoNotificationEvent event) {
        final var window = contentArea.getScene().getWindow();
        ToastNotification.showInfo(window, event.msg());
    }
}
