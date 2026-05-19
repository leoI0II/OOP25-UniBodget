package it.unibo.unibodget.model.investment.controllers;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.ExportResult;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

/**
 * Controller interface for managing investment accounts, positions, and orders.
 *
 * <p>Provides read access to portfolio state (balances, positions, history) and
 * write operations for executing buy, sell, and transfer orders.
 */
public interface InvestmentController {

    /**
     * Returns all investment accounts available to the user.
     *
     * @return an unmodifiable list of {@link InvestmentAccount} instances
     */
    List<InvestmentAccount> getAllInvestmentAccounts();

    /**
     * Returns the currently selected investment account, if any.
     *
     * @return an {@link Optional} containing the current account, or empty if none is selected
     */
    Optional<InvestmentAccount> getCurrentInvestmentAccount();

    /**
     * Selects the investment account identified by the given wallet ID.
     *
     * @param walletId the UUID of the wallet to select
     * @return {@code true} if the wallet was found and selected, {@code false} otherwise
     */
    boolean selectWallet(UUID walletId);

    /**
     * Returns the aggregated balances across all investment accounts, grouped by currency.
     *
     * @return a map from {@link CurrencyUnit} to the total {@link Asset} balance in that currency
     */
    Asset getAggregatedBalance();

    /**
     * Returns the total current market value of the currently selected account.
     *
     * @return the current balance as an {@link Asset} in the base currency
     */
    Asset getCurrentBalance();

    /**
     * Returns the all-time profit/loss (realized + unrealized) of the current account.
     *
     * @return the total P/L as an {@link Asset} in the base currency
     */
    Asset getCurrentAllTimeProfitLoss();

    /**
     * Returns the all-time profit/loss of the current account as a percentage of the total cost basis.
     *
     * @return the P/L percentage (e.g. 12.5 means +12.5%)
     */
    BigDecimal getCurrentAllTimeProfitLossPercentage();

    /**
     * Returns the total cost basis (amount invested) of the current account.
     *
     * @return the total cost basis as an {@link Asset} in the base currency
     */
    Asset getCurrentTotalCostBasis();

    /**
     * Returns all open positions in the currently selected investment account.
     *
     * @return a list of {@link Position} instances for each held asset
     */
    List<Position> getPositions();

    /**
     * Returns the full transaction history of the currently selected account.
     *
     * @return a list of {@link InvestmentTransaction} in chronological order
     */
    List<InvestmentTransaction> getTransactionHistory();

    /**
     * Returns the list of all assets that can be traded (including those not currently owned).
     *
     * @return a list of tradeable {@link CurrencyUnit} instances
     */
    List<CurrencyUnit> getAllTradeableAssets();

    /**
     * Returns the list of all assets currently held in the selected account.
     * 
     * @return a list of {@link CurrencyUnit} instances representing owned assets
     */
    List<CurrencyUnit> getAllOwnedAssets();

    /**
     * Returns the list of stablecoins currently held in the selected account.
     * 
     * @return a list of {@link CurrencyUnit} instances representing owned stablecoins
     */
    List<CurrencyUnit> getOwnedStableCoins();

    /**
     * Returns the current market price of the given asset in the base currency.
     *
     * @param asset the asset to price
     * @return the current market price as an {@link Asset}
     */
    Asset getCurrentMarketPrice(CurrencyUnit asset);

    /**
     * Returns the cash accounts available as payment sources for buy orders.
     *
     * @return a list of {@link CashAccount} instances
     */
    List<CashAccount> getAvailableCashAccounts();

    /**
     * Estimates the total cost of an order without executing it.
     *
     * @param orderType     the type of order (buy or sell)
     * @param asset         the asset involved
     * @param quantity      the number of units
     * @param unitPrice     the price per unit
     * @param fee           the transaction fee
     * @param paymentSource the source of funds for the order
     * @return the estimated total cost as an {@link Asset}
     */
    Asset estimateOrderCost(
        OrderType orderType,
        CurrencyUnit asset,
        BigDecimal quantity,
        Asset unitPrice,
        Asset fee,
        PaymentSource paymentSource
    );

    /**
     * Checks whether the given payment source has sufficient funds to execute a buy order.
     *
     * @param paymentSource the source of funds
     * @param asset         the asset to buy
     * @param quantity      the number of units to buy
     * @param unitPrice     the price per unit
     * @param fee           the transaction fee
     * @return {@code true} if the buy can be executed, {@code false} if funds are insufficient
     */
    boolean canBuy(
        PaymentSource paymentSource,
        CurrencyUnit asset,
        BigDecimal quantity,
        Asset unitPrice,
        Asset fee
    );

    /**
     * Checks whether the source account holds enough of the given asset to sell the requested quantity.
     *
     * @param src      the account to sell from
     * @param asset    the asset to sell
     * @param quantity the number of units to sell
     * @return {@code true} if the sell can be executed, {@code false} if the position is insufficient
     */
    boolean canSell(
        InvestmentAccount src,
        CurrencyUnit asset,
        BigDecimal quantity
    );

    /**
     * Checks whether the source account holds enough of the given asset to transfer the requested quantity.
     *
     * @param src      the account to transfer from
     * @param dst      the account to transfer to
     * @param asset    the asset to transfer
     * @param quantity the number of units to transfer
     * @return {@code true} if the transfer can be executed, {@code false} otherwise
     */
    boolean canTransfer(
        InvestmentAccount src,
        InvestmentAccount dst,
        CurrencyUnit asset,
        BigDecimal quantity
    );

    /**
     * Executes a buy order, recording the transaction in the target account.
     *
     * @param targetAccount the account to credit with the purchased asset
     * @param paymentSource the source of funds for the purchase
     * @param asset         the asset to buy
     * @param quantity      the number of units to buy
     * @param unitPrice     the price per unit at execution time
     * @param fee           the transaction fee
     * @param date          the date of the order
     * @param notes         optional notes
     * @return an {@link OrderResult} describing the outcome
     */
    OrderResult executeBuyOrder(
        InvestmentAccount targetAccount,
        PaymentSource paymentSource,
        CurrencyUnit asset,
        BigDecimal quantity,
        Asset unitPrice,
        Asset fee,
        LocalDate date,
        String notes
    );

    /**
     * Executes a sell order, recording the transaction in the source account.
     *
     * @param sourceAccount     the account to debit the sold asset from
     * @param cashFlowTarget    the target of the cash flow
     * @param asset             the asset to sell
     * @param quantity          the number of units to sell
     * @param unitPrice         the price per unit at execution time
     * @param fee               the transaction fee
     * @param date              the date of the order
     * @param notes             optional notes
     * @return an {@link OrderResult} describing the outcome
     */
    OrderResult executeSellOrder(
        InvestmentAccount sourceAccount,
        PaymentSource cashFlowTarget,
        CurrencyUnit asset,
        BigDecimal quantity,
        Asset unitPrice,
        Asset fee,
        LocalDate date,
        String notes
    );

    /**
     * Executes a transfer of an asset between two investment accounts.
     *
     * @param sourceAccount the account to transfer from
     * @param targetAccount the account to transfer to
     * @param asset         the asset to transfer
     * @param quantity      the number of units to transfer
     * @param date          the date of the transfer
     * @param notes         optional notes
     * @return an {@link OrderResult} describing the outcome
     */
    OrderResult executeTransferOrder(
        InvestmentAccount sourceAccount,
        InvestmentAccount targetAccount,
        CurrencyUnit asset,
        BigDecimal quantity,
        LocalDate date,
        String notes
    );

    /**
     * Exports the current account's transaction data to an external format (e.g. CSV).
     */
    ExportResult exportCurrentAccountData(final File file);

    Optional<Position> getBestPerformer();

    Optional<Position> getWorstPerformer();
}
