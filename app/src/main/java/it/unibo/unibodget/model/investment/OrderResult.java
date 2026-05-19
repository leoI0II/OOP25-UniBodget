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
            OrderResult.CurrencyConversionError
{
    boolean isSuccess();

    record BuyWithCashSuccess(CashTransaction cashTransaction, InvestmentTransaction investmentTransaction) implements OrderResult {
        public boolean isSuccess() { return true; }
    }
    record BuyWithStablesSuccess(InvestmentTransaction stablesAccount, InvestmentTransaction targetInvestmentAccount) implements OrderResult {
        public boolean isSuccess() {return true; }
    }
    record BuyNoPaymentSuccess(InvestmentTransaction investmentTransaction) implements OrderResult {
        public boolean isSuccess() { return true; }
    }
    record TransferSuccess(InvestmentTransaction srcInvestmentTransaction, InvestmentTransaction dstInvestmentTransaction) implements OrderResult {
        public boolean isSuccess() { return true; }
    }
    record SellWithCashSuccess(InvestmentTransaction investmentTransaction, CashTransaction cashTransaction) implements OrderResult {
        public boolean isSuccess() { return true; }
    }
    record SellWithStablesSuccess(InvestmentTransaction investmentTransaction, InvestmentTransaction stablesAccount) implements OrderResult {
        public boolean isSuccess() { return true; }
    }
    record SellNoPaymentSuccess(InvestmentTransaction investmentTransaction) implements OrderResult {
        public boolean isSuccess() { return true; }
    }
    record InsufficientFunds(Asset required, Asset available) implements OrderResult {
        public boolean isSuccess() { return false; }
    }
    record InsufficientAssets(BigDecimal requested, BigDecimal available) implements OrderResult {
        public boolean isSuccess() { return false; }
    }
    record CurrencyConversionError(String message) implements OrderResult {
        public boolean isSuccess() { return false; }
    }
}
