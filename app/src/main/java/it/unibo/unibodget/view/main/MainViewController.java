package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.MainInfoNotificationEvent;
import it.unibo.unibodget.model.utils.event.OrderResultEvent;
import it.unibo.unibodget.view.investments.InvestmentsViewController;
import it.unibo.unibodget.view.utils.AssetFormatter;
import it.unibo.unibodget.view.utils.ToastNotification;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

/**
 * Root JavaFX controller for the main application window.
 * Owns the {@link SideBarViewController} and a {@link StackPane} content area,
 * into which it swaps different sub-views (investments, dashboard, …).
 * Also handles application-level toast notifications via the message bus.
 */
public class MainViewController extends BaseViewController {

    @FXML private SideBarViewController sideBarController;
    @FXML private StackPane contentArea;
    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final DashboardFacade dashboardFacade;
    private final ViewControllersFactory viewControllersFactory;
    /** Ref to the currently shown sub-controller, used to call {@link BaseViewController#dispose()} on navigation. */
    private BaseViewController currentVC;

    /**
     * Creates the main controller wiring together all required services.
     *
     * @param investmentController   handles investment business logic
     * @param snapshotService        provides historical balance snapshots
     * @param dashboardFacade        facade for the dashboard module (nullable until wired)
     * @param viewControllersFactory factory used to build sub-view controllers
     */
    public MainViewController(
            final InvestmentController investmentController,
            final InvestmentsSnapshotService snapshotService,
            final DashboardFacade dashboardFacade,
            final ViewControllersFactory viewControllersFactory
    ) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.dashboardFacade = dashboardFacade;
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);

        subscribe(OrderResultEvent.class, this::onOrderResultEvent);
        subscribe(MainErrorNotificationEvent.class, this::onMainErrorNotificationEvent);
        subscribe(MainInfoNotificationEvent.class, this::onMainInfoNotificationEvent);
    }

    /**
     * Called automatically by {@link javafx.fxml.FXMLLoader} after the FXML is loaded.
     * Shows the investments view as the default content.
     */
    @FXML
    public void initialize() {
        showInvestments();
    }

    /**
     * Loads and shows the dashboard view in the content area.
     * Disposes the previously active sub-controller if present.
     */
    @FXML
    public void showDashboard() {
        if (currentVC != null) {
            currentVC.dispose();
        }
        final FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/main/DashboardView.fxml")
        );
        Node view = null;
        try {
            view = fxmlLoader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load DashboardView", e);
        }
        final DashboardViewController dvc = fxmlLoader.getController();
        currentVC = dvc;
        dvc.setDashboardFacade(dashboardFacade);
        sideBarController.setDelegate(dvc);
        sideBarController.refresh();

        contentArea.getChildren().setAll(view);
    }

    /**
     * Loads and shows the investments view in the content area.
     * Disposes the previously active sub-controller if present, then selects the first account.
     */
    @FXML
    public void showInvestments() {
        if (currentVC != null) {
            currentVC.dispose();
        }
        final FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/investments/InvestmentsView.fxml")
        );
        fxmlLoader.setControllerFactory(viewControllersFactory::create);
        Node view = null;
        try {
            view = fxmlLoader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load InvestmentsView", e);
        }
        final InvestmentsViewController ivc = fxmlLoader.getController();
        currentVC = ivc;
        ivc.setSideBarViewController(sideBarController);
        sideBarController.setDelegate(ivc);
        sideBarController.refresh();
        if (!investmentController.getAllInvestmentAccounts().isEmpty()) {
            ivc.onItemSelected(investmentController.getAllInvestmentAccounts().getFirst().getId());
        }

        contentArea.getChildren().setAll(view);
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
                    ToastNotification.showError(window,
                            "Insufficient funds. Required: "
                            + AssetFormatter.ofAsset(f.required()) + ", available: " + AssetFormatter.ofAsset(f.available()));
            case OrderResult.InsufficientAssets a ->
                    ToastNotification.showError(window,
                            "Insufficient assets. Required: "
                            + a.requested() + ", available: " + a.available());
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
