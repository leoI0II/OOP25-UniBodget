package it.unibo.unibodget.model.investment;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

public sealed interface PaymentSource 
    permits PaymentSource.FromCashAccount, 
            PaymentSource.FromStableCoinPosition, 
            PaymentSource.NoPaymentSource {

    record FromCashAccount(CashAccount account) implements PaymentSource {}
    record FromStableCoinPosition(InvestmentAccount account, CurrencyUnit stableCoin) implements PaymentSource {}
    record NoPaymentSource() implements PaymentSource {}    // if the checkbox of src is not selected
}
