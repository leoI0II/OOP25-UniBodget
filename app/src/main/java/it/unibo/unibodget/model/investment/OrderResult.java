package it.unibo.unibodget.model.investment;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

public sealed interface OrderResult 
    permits OrderResult.BuyWithCashSuccess,
            OrderResult.BuyWithStablesSuccess,
            OrderResult.BuyNoPaymentSuccess,
            OrderResult.SellWithCashSuccess,
            OrderResult.SellWithStablesSuccess,
            OrderResult.SellNoPaymentSuccess,
            OrderResult.TransferSuccess, 
            OrderResult.InsufficientFunds, 
            OrderResult.InsufficientAssets, 
            OrderResult.CurrencyConversionError {

    record BuyWithCashSuccess(CashTransaction cashTransaction, InvestmentTransaction investmentTransaction) implements OrderResult {}
    record BuyWithStablesSuccess(InvestmentTransaction stablesAccount, InvestmentTransaction targetInvestmentAccount) implements OrderResult {}
    record BuyNoPaymentSuccess(InvestmentTransaction investmentTransaction) implements OrderResult {}
    record TransferSuccess(InvestmentTransaction srcInvestmentTransaction, InvestmentTransaction dstInvestmentTransaction) implements OrderResult {}
    record SellWithCashSuccess(InvestmentTransaction investmentTransaction, CashTransaction cashTransaction) implements OrderResult {}
    record SellWithStablesSuccess(InvestmentTransaction investmentTransaction, InvestmentTransaction stablesAccount) implements OrderResult {}
    record SellNoPaymentSuccess(InvestmentTransaction investmentTransaction) implements OrderResult {}
    record InsufficientFunds(Asset required, Asset available) implements OrderResult {}
    record InsufficientAssets(BigDecimal requested, BigDecimal available) implements OrderResult {}
    record CurrencyConversionError(String message) implements OrderResult {}
}
