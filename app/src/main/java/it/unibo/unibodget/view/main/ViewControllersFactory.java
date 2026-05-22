package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.view.investments.InvestmentsViewController;

import java.util.Objects;

public class ViewControllersFactory {

    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;

    public ViewControllersFactory(
            InvestmentController investmentController,
            InvestmentsSnapshotService snapshotService
    ) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
    }

    public Object create(Class<?> controllerClass) {
        if (controllerClass == InvestmentsViewController.class) {
            return new InvestmentsViewController(investmentController, snapshotService);
        }
        if (controllerClass == SideBarViewController.class) {
            return new SideBarViewController();
        }

        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create controller: " + controllerClass, e);
        }
    }
}
