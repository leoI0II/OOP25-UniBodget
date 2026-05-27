package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
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

public class MainViewController extends BaseViewController {

    @FXML private SideBarViewController sideBarController;
    @FXML private StackPane contentArea;
    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final DashboardFacade dashboardFacade;
    private final ViewControllersFactory viewControllersFactory;
    private BaseViewController currentVC;           // salvo la ref al view controller corrente per pulire i dati con dispose() alla fine dell utilizzo

    public MainViewController(
            InvestmentController investmentController,
            InvestmentsSnapshotService snapshotService,
            DashboardFacade dashboardFacade,
            ViewControllersFactory viewControllersFactory
    ) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.dashboardFacade = dashboardFacade;
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);

        subscribe(OrderResultEvent.class, this::onOrderResultEvent);
    }

    @FXML
    public void initialize() {
        showInvestments();
    }

//    public void setInvestmentController(final InvestmentController investmentController) {
//        this.investmentController = investmentController;
//    }

    @FXML
    public void showDashboard() {
        if (currentVC != null) {
            currentVC.dispose();
        }
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/main/DashboardView.fxml")
        );
        Node view = null;
        try {
            view = fxmlLoader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load InvestmentsView", e);
        }
        DashboardViewController dvc = fxmlLoader.getController();
        currentVC = dvc;
        dvc.setDashboardFacade(dashboardFacade);
        sideBarController.setDelegate(dvc);  // ← connette sidebar a dashboard
        sideBarController.refresh();

        contentArea.getChildren().setAll(view);
    }

    @FXML
    public void showInvestments() {
        if (currentVC != null) {
            currentVC.dispose();
        }
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/investments/InvestmentsView.fxml")
        );
        fxmlLoader.setControllerFactory(viewControllersFactory::create);
        Node view = null;
        try {
            view = fxmlLoader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load InvestmentsView", e);
        }
        InvestmentsViewController ivc = fxmlLoader.getController();
        currentVC = ivc;
        ivc.setSideBarViewController(sideBarController);
        sideBarController.setDelegate(ivc);
        sideBarController.refresh();
        if (!investmentController.getAllInvestmentAccounts().isEmpty()) {
            ivc.onItemSelected(investmentController.getAllInvestmentAccounts().getFirst().getId());
        }

        contentArea.getChildren().setAll(view);
    }

    private void onOrderResultEvent(final OrderResultEvent event) {
        final var window = contentArea.getScene().getWindow();
        switch (event.result) {
            case OrderResult.InsufficientFunds f ->
                    ToastNotification.showError(window,
                            "Insufficient funds. Required: "
                            + AssetFormatter.ofAsset(f.required()) + ", available: " + AssetFormatter.ofAsset(f.available()));
            case OrderResult.InsufficientAssets a ->
                    ToastNotification.showError(window,
                            "Insufficient assets. Required: "
                            + a.requested() + ", available: " + a.available());
            default -> {
                if (event.result.isSuccess()) {
                    ToastNotification.showSuccess(window,
                            "Transaction executed successfully!");
                }
            }
        }
    }

}
