package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.view.investments.InvestmentsViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class MainViewController {

    @FXML private SideBarViewController sideBarViewController;
    @FXML private StackPane contentArea;
    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final DashboardFacade dashboardFacade;

    public MainViewController(
            InvestmentController investmentController,
            InvestmentsSnapshotService snapshotService,
            DashboardFacade dashboardFacade
    ) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.dashboardFacade = Objects.requireNonNull(dashboardFacade);
    }

    @FXML
    public void initialize() {
        showDashboard();
    }

//    public void setInvestmentController(final InvestmentController investmentController) {
//        this.investmentController = investmentController;
//    }

    @FXML
    public void showDashboard() {
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
        dvc.setDashboardFacade(dashboardFacade);
        sideBarViewController.setDelegate(dvc);  // ← connette sidebar a dashboard
        sideBarViewController.refresh();

        contentArea.getChildren().setAll(view);
    }

    @FXML
    public void showInvestments() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/investments/InvestmentsView.fxml")
        );
        var factory = new ViewControllersFactory(
                investmentController,
                snapshotService
        );
        fxmlLoader.setControllerFactory(factory::create);
        Node view = null;
        try {
            view = fxmlLoader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load InvestmentsView", e);
        }
        InvestmentsViewController ivc = fxmlLoader.getController();
        ivc.setSideBarViewController(sideBarViewController);
        sideBarViewController.setDelegate(ivc);
        sideBarViewController.refresh();

        contentArea.getChildren().setAll(view);
    }

}
