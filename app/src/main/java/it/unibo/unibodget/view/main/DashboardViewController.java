package it.unibo.unibodget.view.main;

import it.unibo.unibodget.controller.dashboard.impl.DefaultDashboardController;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.view.utils.AssetFormatter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Bridge that lets the shared sidebar drive the dashboard context.
 *
 * <p>It is a plain {@link SideBarDelegate} (not an FXML controller): the dashboard
 * center is rendered by {@link DefaultDashboardView} inside the content area, while
 * this delegate feeds the <em>shared</em> {@link SideBarViewController}.</p>
 *
 * <ul>
 *   <li>Reads (wallet list, aggregated total) go through the {@link DashboardFacade}.</li>
 *   <li>Actions (select wallet, add wallet) go through the {@link DefaultDashboardController}
 *       so the dashboard center is re-rendered.</li>
 * </ul>
 */
public final class DashboardViewController extends BaseViewController implements SideBarDelegate {

    private final DashboardFacade dashboardFacade;
    private final DefaultDashboardController dashboardController;
    private final SideBarViewController sideBar;

    /**
     * @param dashboardFacade     facade used to read sidebar data
     * @param dashboardController presenter used to handle sidebar actions
     * @param sideBar             the shared sidebar to refresh after an action
     */
    public DashboardViewController(
            final DashboardFacade dashboardFacade,
            final DefaultDashboardController dashboardController,
            final SideBarViewController sideBar
    ) {
        this.dashboardFacade = Objects.requireNonNull(dashboardFacade);
        this.dashboardController = Objects.requireNonNull(dashboardController);
        this.sideBar = Objects.requireNonNull(sideBar);
    }

    @Override
    public List<SideBarItem> getItems() {
        final var selected = dashboardFacade.getCurrentSelectedCashAccount();
        return dashboardFacade.getAllCashAccounts().stream()
                .map(w -> new SideBarItem(
                        w.getId(),
                        w.getName(),
                        AssetFormatter.ofAsset(w.getBalance()),
                        selected.map(s -> s.getId().equals(w.getId())).orElse(false)
                ))
                .toList();
    }

    @Override
    public String getTotalAggregatedBalance() {
        if (dashboardFacade.getAllCashAccounts().isEmpty()) {
            return "";
        }
        return AssetFormatter.ofAsset(dashboardFacade.getAggregatedBalance());
    }

    @Override
    public void onItemSelected(final UUID id) {
        dashboardController.onWalletSelected(id);
        sideBar.refresh();
    }

    @Override
    public void onAddWalletRequested() {
        dashboardController.onCreateWalletRequested();
        sideBar.refresh();
    }
}
