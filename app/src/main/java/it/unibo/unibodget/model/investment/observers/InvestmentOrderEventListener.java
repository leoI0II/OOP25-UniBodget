package it.unibo.unibodget.model.investment.observers;

import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

public interface InvestmentOrderEventListener {
    void onOrderExecuted(InvestmentAccount account, OrderResult result);
}
