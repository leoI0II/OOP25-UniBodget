package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.wallet.Wallet;
import it.unibo.unibodget.view.main.SideBarDelegate;
import it.unibo.unibodget.view.main.SideBarItem;
import it.unibo.unibodget.view.main.SideBarViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class InvestmentsViewController implements SideBarDelegate {

    InvestmentController investmentController;
    @FXML private VBox walletList;
    @FXML private SideBarViewController sideBarViewController;

    @FXML
    public void initialize() {
        // chiamato automaticamente da FXMLLoader dopo il caricamento
        sideBarViewController.setDelegate(this);
        sideBarViewController.refresh();
    }

    public void refreshData() {

    }

    @Override
    public List<SideBarItem> getItems() {
        return investmentController.getAllInvestmentAccounts().stream()
                .map(w -> new SideBarItem(
                        w.getId(),
                        w.getName(),
                        String.format("%s %.2f", w.getBalance().currency().getSymbol(), w.getBalance().amount()),
                        w.getId().equals(investmentController.getCurrentInvestmentAccount()
                                .map(Wallet::getId).orElse(null))
                ))
                .toList();
    }

    @Override
    public String getTotalAggregatedBalance() {
        return String.format(
                "%s %.2f",
                investmentController.getAggregatedBalance().currency().getSymbol(),
                investmentController.getAggregatedBalance().amount()
        );
    }

    @Override
    public void onItemSelected(UUID id) {
        investmentController.selectWallet(id);
        refreshMainPanel();
    }
}
