package it.unibo.unibodget.model.investment;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * Represents the outcome of attempting to execute an investment order.
 * This sealed interface encompasses all possible success states and specific
 * failure reasons
 * for operations such as buying, selling, and transferring assets.
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
     * Indicates whether the order was executed successfully.
     *
     * @return {@code true} if the order succeeded, {@code false} if it failed
     */
    boolean isSuccess();

    /**
     * Represents a successful buy order paid for using a cash account.
     *
     * @param cashTransaction       the transaction debiting the cash account
     * @param investmentTransaction the transaction crediting the investment account
     */
    record BuyWithCashSuccess(
            CashTransaction cashTransaction, InvestmentTransaction investmentTransaction) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a successful buy order paid for using stablecoins within the
     * investment account.
     *
     * @param stablesAccount          the transaction debiting the stablecoin
     *                                balance
     * @param targetInvestmentAccount the transaction crediting the purchased asset
     *                                balance
     */
    record BuyWithStablesSuccess(
            InvestmentTransaction stablesAccount,
            InvestmentTransaction targetInvestmentAccount) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a successful buy order with no specified payment source (e.g., an
     * external deposit).
     *
     * @param investmentTransaction the transaction crediting the investment account
     */
    record BuyNoPaymentSuccess(InvestmentTransaction investmentTransaction) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a successful transfer of assets between investment accounts.
     *
     * @param srcInvestmentTransaction the transaction debiting the source account
     * @param dstInvestmentTransaction the transaction crediting the destination
     *                                 account
     */
    record TransferSuccess(
            InvestmentTransaction srcInvestmentTransaction,
            InvestmentTransaction dstInvestmentTransaction) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a successful sell order where the proceeds are deposited into a
     * cash account.
     *
     * @param investmentTransaction the transaction debiting the sold asset from the
     *                              investment account
     * @param cashTransaction       the transaction crediting the cash account
     */
    record SellWithCashSuccess(
            InvestmentTransaction investmentTransaction,
            CashTransaction cashTransaction) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a successful sell order where the proceeds are converted to
     * stablecoins.
     *
     * @param investmentTransaction the transaction debiting the sold asset
     * @param stablesAccount        the transaction crediting the stablecoin balance
     */
    record SellWithStablesSuccess(
            InvestmentTransaction investmentTransaction,
            InvestmentTransaction stablesAccount) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a successful sell order with no specified target for the proceeds
     * (e.g., an external withdrawal).
     *
     * @param investmentTransaction the transaction debiting the sold asset
     */
    record SellNoPaymentSuccess(InvestmentTransaction investmentTransaction) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Represents a failed order due to insufficient funds in the payment source.
     *
     * @param required  the amount required to complete the order
     * @param available the amount currently available in the payment source
     */
    record InsufficientFunds(Asset required, Asset available) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    /**
     * Represents a failed order due to insufficient assets available to sell or
     * transfer.
     *
     * @param requested the quantity of the asset requested for the operation
     * @param available the quantity of the asset currently held
     */
    record InsufficientAssets(BigDecimal requested, BigDecimal available) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    /**
     * Represents a failed order due to an error during currency conversion.
     *
     * @param message a descriptive error message detailing the conversion failure
     */
    record CurrencyConversionError(String message) implements OrderResult {
        /** {@inheritDoc} */
        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}
