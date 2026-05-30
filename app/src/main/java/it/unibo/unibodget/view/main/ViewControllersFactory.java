package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.view.investments.AddTransactionDialogViewController;
import it.unibo.unibodget.view.investments.InvestmentsViewController;

import java.util.Objects;

/**
 * Factory for JavaFX view controllers.
 *
 * <p>Used as a custom {@code Callback} with {@code FXMLLoader.setControllerFactory} so that
 * controllers requiring constructor injection receive their dependencies automatically.
 * Controllers not explicitly handled here are instantiated via their no-arg constructor
 * through reflection.</p>
 */
public class ViewControllersFactory {

    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;

    /**
     * Creates the factory with the shared services injected into every controller that needs them.
     *
     * @param investmentController the investment controller shared across investment views
     * @param snapshotService      the snapshot service used to persist portfolio balance history
     */
    public ViewControllersFactory(
            final InvestmentController investmentController,
            final InvestmentsSnapshotService snapshotService
    ) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
    }

    /**
     * Creates and returns an instance of the requested controller class.
     *
     * <p>Known controller types are constructed with their required dependencies.
     * Any other class is instantiated reflectively via its no-arg constructor.</p>
     *
     * @param controllerClass the class of the controller to create
     * @return a fully initialised controller instance
     * @throws RuntimeException if the class has no accessible no-arg constructor
     */
    public Object create(final Class<?> controllerClass) {
        if (controllerClass == InvestmentsViewController.class) {
            return new InvestmentsViewController(investmentController, snapshotService, this);
        }
        if (controllerClass == SideBarViewController.class) {
            return new SideBarViewController();
        }
        if (controllerClass == AddTransactionDialogViewController.class) {
            return new AddTransactionDialogViewController(investmentController);
        }
        if (controllerClass == MainViewController.class) {
            return new MainViewController(investmentController,
                    snapshotService,
                    null,
                    this);
        }

        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create controller: " + controllerClass, e);
        }
    }
}
