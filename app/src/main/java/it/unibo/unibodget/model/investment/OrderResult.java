package it.unibo.unibodget.model.investment;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * 
 *
 */
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

    /**
     * 
     * 
     * @param cashTransaction
     * @param investmentTransaction
     */
    record BuyWithCashSuccess
        (CashTransaction cashTransaction, InvestmentTransaction investmentTransaction) 
        implements OrderResult {

        }

    /**
     * 
     * BuyWithStablesSuccess
     * @param stablesAccount
     * @param targetInvestmentAccount
     */
    record BuyWithStablesSuccess
        (InvestmentTransaction stablesAccount, InvestmentTransaction targetInvestmentAccount) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param investmentTransaction
     */
    record BuyNoPaymentSuccess
        (InvestmentTransaction investmentTransaction) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param srcInvestmentTransaction
     * @param dstInvestmentTransaction
     */
    record TransferSuccess
        (InvestmentTransaction srcInvestmentTransaction, InvestmentTransaction dstInvestmentTransaction) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param investmentTransaction
     * @param cashTransaction
     */
    record SellWithCashSuccess
        (InvestmentTransaction investmentTransaction, CashTransaction cashTransaction) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param investmentTransaction
     * @param stablesAccount
     */
    record SellWithStablesSuccess
        (InvestmentTransaction investmentTransaction, InvestmentTransaction stablesAccount) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param investmentTransaction
     */
    record SellNoPaymentSuccess
        (InvestmentTransaction investmentTransaction) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param required
     * @param available
     */
    record InsufficientFunds
        (Asset required, Asset available) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param requested
     * @param available
     */
    record InsufficientAssets
        (BigDecimal requested, BigDecimal available) 
        implements OrderResult {

        }

    /**
     * 
     *
     * @param message
     */
    record CurrencyConversionError
        (String message) 
        implements OrderResult {

        }
}
