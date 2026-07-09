package it.unibo.unibodget.model.investment;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

public sealed interface PaymentSource 
    permits PaymentSource.CashAccountChannel, 
            PaymentSource.StableCoinPositionChannel, 
            PaymentSource.NoPaymentChannel {

    record CashAccountChannel(CashAccount account) implements PaymentSource {}
    record StableCoinPositionChannel(InvestmentAccount account, CurrencyUnit stableCoin) implements PaymentSource {}
    record NoPaymentChannel() implements PaymentSource {}    // if the checkbox of src is not selected
}
