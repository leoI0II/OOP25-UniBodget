package it.unibo.unibodget.view.main;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.controller.investments.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.view.investments.AddNewWalletDialogViewController;
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
public final class ViewControllersFactory {

    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final DashboardModule dashboardModule;
    private final CurrencyConverterModule converterModule;
    private final SettingsController settingsController;

    /**
     * Creates the factory with the shared services injected into every controller that needs them.
     *
     * @param investmentController the investment controller shared across investment views
     * @param snapshotService      the snapshot service used to persist portfolio balance history
     * @param dashboardModule      the dashboard MVC graph embedded as content in the shell
     * @param converterModule      the currency-converter view embedded as content in the shell
     * @param settingsController   the settings controller used to manage user preferences
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of controllers and services")
    public ViewControllersFactory(
            final InvestmentController investmentController,
            final InvestmentsSnapshotService snapshotService,
            final DashboardModule dashboardModule,
            final CurrencyConverterModule converterModule,
            final SettingsController settingsController
    ) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.dashboardModule = Objects.requireNonNull(dashboardModule);
        this.converterModule = Objects.requireNonNull(converterModule);
        this.settingsController = Objects.requireNonNull(settingsController);
    }

    /**
     * Creates and returns an instance of the requested controller class.
     *
     * <p>Known controller types are constructed with their required dependencies.
     * Any other class is instantiated reflectively via its no-arg constructor.</p>
     *
     * @param controllerClass the class of the controller to create
     * @return a fully initialised controller instance
     * @throws IllegalStateException if the class has no accessible no-arg constructor
     */
    public Object create(final Class<?> controllerClass) {
        if (controllerClass == InvestmentsViewController.class) {
            return new InvestmentsViewController(investmentController, snapshotService, this);
        }
        if (controllerClass == SideBarViewController.class) {
            return new SideBarViewController(this);
        }
        if (controllerClass == AddTransactionDialogViewController.class) {
            return new AddTransactionDialogViewController(investmentController);
        }
        if (controllerClass == MainViewController.class) {
            return new MainViewController(investmentController,
                    snapshotService,
                    dashboardModule,
                    converterModule,
                    this,
                    settingsController);
        }
        if (controllerClass == AddNewWalletDialogViewController.class) {
            return new AddNewWalletDialogViewController(investmentController);
        }

        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create controller: " + controllerClass, e);
        }
    }
}
