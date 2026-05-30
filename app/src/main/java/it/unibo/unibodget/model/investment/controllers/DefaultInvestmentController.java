package it.unibo.unibodget.model.investment.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.ExportResult;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.model.settings.Settings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.converter.provider.ExchangeRateProvider;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

/**
 * Default implementation of {@link InvestmentController}.
 * Coordinates buy/sell/transfer orders across {@link InvestmentAccount}s and
 * {@link it.unibo.unibodget.model.wallet.CashAccount}s via the injected services.
 */
public class DefaultInvestmentController implements InvestmentController {

    private final InvestmentAccountService investmentAccountService;
    private final CashAccountService cashAccountService;
    private final ExchangeRateProvider exchangeRateProvider;
    private final Settings settings;
    private final InvestmentsSnapshotService snapshotService;

    /**
     * Creates a new controller wiring together all required services.
     *
     * @param investmentAccountService manages investment wallets
     * @param cashAccountService       manages cash wallets
     * @param exchangeRateProvider     converts between currencies
     * @param settings                 user preferences (base currency, …)
     * @param snapshotService          persists balance snapshots after each order
     */
    public DefaultInvestmentController(
            final InvestmentAccountService investmentAccountService,
            final CashAccountService cashAccountService,
            final ExchangeRateProvider exchangeRateProvider,
            final Settings settings,
            final InvestmentsSnapshotService snapshotService
    ) {
        this.investmentAccountService = Objects.requireNonNull(investmentAccountService);
        this.cashAccountService = Objects.requireNonNull(cashAccountService);
        this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider);
        this.settings = Objects.requireNonNull(settings);
        this.snapshotService = Objects.requireNonNull(snapshotService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvestmentAccount> getAllInvestmentAccounts() {
        return investmentAccountService.getWallets();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<InvestmentAccount> getCurrentInvestmentAccount() {
        return investmentAccountService.getCurrentWallet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean selectWallet(final UUID walletId) {
        return investmentAccountService.selectWallet(walletId);
    }

    /**
     * Helper method to retrieve the currently selected investment account or throw an exception if none is selected.
     *
     * @return the currently selected InvestmentAccount
     * @throws IllegalStateException if no investment account is currently selected
     */
    private InvestmentAccount getCurrentAccountOrThrow() {
        return getCurrentInvestmentAccount()
                .orElseThrow(() -> new IllegalStateException("No current investment account selected"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset getAggregatedBalance() {
        return getAllInvestmentAccounts().stream()
                .map(InvestmentAccount::getBalance)
                .map(balance -> exchangeRateProvider.convert(balance, settings.getBaseCurrency()))
                .reduce(Asset.zero(settings.getBaseCurrency()), Asset::add);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset getCurrentBalance() {
        return getCurrentAccountOrThrow().getBalance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset getCurrentAllTimeProfitLoss() {
        return getCurrentAccountOrThrow().getTotalProfitLoss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCurrentAllTimeProfitLossPercentage() {
        return getCurrentAccountOrThrow().getTotalProfitLossPercentage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset getCurrentTotalCostBasis() {
        return getCurrentAccountOrThrow().getTotalCostBasis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Position> getPositions() {
        return getCurrentAccountOrThrow().getPositions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvestmentTransaction> getTransactionHistory() {
        return getCurrentAccountOrThrow().getHistory().getTransactions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CurrencyUnit> getAllTradeableAssets() {
        return Stream.<CurrencyUnit>concat(
                Arrays.stream(StockMarketCurrency.values()),
                Arrays.stream(CryptoCurrency.values())
        ).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CurrencyUnit> getAllOwnedAssets() {
        return getPositions().stream()
                .map(Position::asset)
                .distinct()
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CurrencyUnit> getOwnedStableCoins() {
        return getAllOwnedAssets().stream()
                .filter(asset -> asset instanceof CryptoCurrency && ((CryptoCurrency) asset).isStableCoin())
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset getCurrentMarketPrice(final CurrencyUnit asset) {
        return exchangeRateProvider.convert(
                Asset.of(asset, BigDecimal.ONE),
                getCurrentAccountOrThrow().getBaseCurrency()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CashAccount> getAvailableCashAccounts() {
        return cashAccountService.getWallets();
    }

    /**
     * Resolves the target currency from a payment source, falling back to {@code def} if none is specified.
     *
     * @param paymentSource the payment channel for the order
     * @param def           default currency used when the source carries no currency info
     * @return the resolved target currency
     */
    private CurrencyUnit targetCurrencyOf(final PaymentSource paymentSource, final CurrencyUnit def) {
        return switch (paymentSource) {
            case PaymentSource.CashAccountChannel cashSrc -> cashSrc.account().getBaseCurrency();
            case PaymentSource.StableCoinPositionChannel stableCoinSrc -> stableCoinSrc.stableCoin();
            case PaymentSource.NoPaymentChannel noSrc ->
                    def; // If no payment source specified, default to the asset's currency for cost estimation
        };
    }

    /**
     * Computes the order total in the currency of the payment source.
     *
     * @param quantity      number of units traded
     * @param unitPrice     price per unit
     * @param fee           transaction fee
     * @param paymentSource channel used for payment (determines the target currency)
     * @param addFee        {@code true} for buys (fee added), {@code false} for sells (fee subtracted)
     * @return the estimated cost/proceeds in the target currency
     */
    private Asset estimateOrderInTargetCurrency(
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final PaymentSource paymentSource,
            final boolean addFee
    ) {
        var nativeTotalCost = unitPrice.multiply(quantity);
        nativeTotalCost = addFee ? nativeTotalCost.add(fee) : nativeTotalCost.subtract(fee);
        final CurrencyUnit targetCurrency = targetCurrencyOf(paymentSource, getCurrentAccountOrThrow().getBaseCurrency());

        return nativeTotalCost.currency().equals(targetCurrency)
                ? nativeTotalCost
                : exchangeRateProvider.convert(nativeTotalCost, targetCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset estimateOrderCost(
            final OrderType orderType,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final PaymentSource paymentSource
    ) {
        return switch (orderType) {
            case BUY -> estimateOrderInTargetCurrency(quantity, unitPrice, fee, paymentSource, true);
            case SELL -> estimateOrderInTargetCurrency(quantity, unitPrice, fee, paymentSource, false);
            case TRANSFER -> Asset.of(asset, quantity);
        };
    }

    /**
     * Returns the available balance for a given payment source, or empty if there is no source.
     *
     * @param paymentSource the channel to check
     * @return the available {@link Asset}, or {@link Optional#empty()} for {@code NoPaymentChannel}
     */
    private Optional<Asset> availableAsset(final PaymentSource paymentSource) {
        return switch (paymentSource) {
            case PaymentSource.CashAccountChannel cashSrc -> Optional.of(cashSrc.account().getBalance());
            case PaymentSource.StableCoinPositionChannel stableCoinSrc -> {
                final var stableCoinPosition = stableCoinSrc.account().getPositions().stream()
                        .filter(pos -> pos.asset().equals(stableCoinSrc.stableCoin()))
                        .findFirst();
                yield Optional.of(
                        stableCoinPosition.map(p -> Asset.of(stableCoinSrc.stableCoin(), p.quantity()))
                                .orElse(Asset.zero(stableCoinSrc.stableCoin()))
                );
            }
            // If no payment source specified, available amount is considered zero for the purpose of cost comparison
            case PaymentSource.NoPaymentChannel noSrc ->
                    Optional.empty();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBuy(
            final PaymentSource paymentSource,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee) {

        final var estimatedCost = estimateOrderCost(OrderType.BUY, asset, quantity, unitPrice, fee, paymentSource);
        final var availableOpt = availableAsset(paymentSource);
        if (availableOpt.isEmpty()) {
            return true; // means there is no payment source, so yes to transfer
        }
        final var available = availableOpt.get();
        return available.compareTo(estimatedCost) >= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSell(
            final InvestmentAccount src,
            final CurrencyUnit asset,
            final BigDecimal quantity
    ) {
        final var position = src.getPositions().stream()
                .filter(pos -> pos.asset().equals(asset))
                .findFirst();

        return position.isPresent() && position.get().quantity().compareTo(quantity) >= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canTransfer(
            final InvestmentAccount src,
            final InvestmentAccount dst,
            final CurrencyUnit asset,
            final BigDecimal quantity
    ) {
        if (src.getId().equals(dst.getId())) {
            return false; // Cannot transfer to the same account
        }
        final var position = src.getPositions().stream()
                .filter(pos -> pos.asset().equals(asset))
                .findFirst();

        return position.isPresent() && position.get().quantity().compareTo(quantity) >= 0;
    }

    private String getBuyOrderDescription(
            final BigDecimal quantity,
            final CurrencyUnit asset,
            final Asset unitPrice
    ) {
        return "Buy " + quantity + " " + asset + " @ " + unitPrice;
    }

    private String getSellOrderDescription(
            final BigDecimal quantity,
            final CurrencyUnit asset,
            final Asset unitPrice
    ) {
        return "Sell " + quantity + " " + asset + " @ " + unitPrice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderResult executeBuyOrder(
            final InvestmentAccount targetAccount,
            final PaymentSource paymentSource,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final LocalDate date,
            final String notes) {

        final var cost = estimateOrderCost(OrderType.BUY, asset, quantity, unitPrice, fee, paymentSource);

        if (!canBuy(paymentSource, asset, quantity, unitPrice, fee)) {
            final var available = availableAsset(paymentSource).orElse(Asset.zero(cost.currency()));
            return new OrderResult.InsufficientFunds(cost, available);
        }

        final InvestmentTransaction investmentTransactionIn = InvestmentTransaction.of(
                Asset.of(asset, quantity),
                Category.INVESTMENT_BUY,
                date,
                getBuyOrderDescription(quantity, asset, unitPrice),
                notes,
                unitPrice,
                fee
        );
        targetAccount.addTransaction(investmentTransactionIn);

        return switch (paymentSource) {
            case PaymentSource.CashAccountChannel cashSrc -> {
                final var cashTransaction = CashTransaction.of(
                        Asset.of(cost.currency(), cost.amount().negate()), // Cash outflow
                        Category.INVESTMENT_BUY,
                        date,
                        getBuyOrderDescription(quantity, asset, unitPrice),
                        notes
                );
                // Execute the cash transaction on the source cash account
                cashSrc.account().addTransaction(cashTransaction);
                yield new OrderResult.BuyWithCashSuccess(cashTransaction, investmentTransactionIn);
            }
            case PaymentSource.StableCoinPositionChannel stableCoinSrc -> {
                final var investmentTransactionOut = InvestmentTransaction.of(
                        Asset.of(stableCoinSrc.stableCoin(), cost.amount().negate()), // Stablecoin outflow
                        Category.INVESTMENT_SELL,
                        date,
                        "Use " + cost + " from stablecoin position to buy " + quantity + " " + asset,
                        notes,
                        Asset.of(
                                stableCoinSrc.stableCoin(),
                                exchangeRateProvider.convert(cost, stableCoinSrc.stableCoin()).amount()
                        ),
                        fee
                );
                // Execute the stablecoin transaction on the source investment account
                stableCoinSrc.account().addTransaction(investmentTransactionOut);
                yield new OrderResult.BuyWithStablesSuccess(investmentTransactionOut, investmentTransactionIn);
            }
            case PaymentSource.NoPaymentChannel noSrc -> {
                yield new OrderResult.BuyNoPaymentSuccess(investmentTransactionIn);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderResult executeSellOrder(
            final InvestmentAccount sourceAccount,
            final PaymentSource cashFlowTarget,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final LocalDate date,
            final String notes
    ) {
        if (!canSell(sourceAccount, asset, quantity)) {
            final var availableQty = sourceAccount.getPositions().stream()
                    .filter(pos -> pos.asset().equals(asset))
                    .findFirst()
                    .map(Position::quantity)
                    .orElse(BigDecimal.ZERO);
            return new OrderResult.InsufficientAssets(
                    quantity,
                    availableQty
            );
        }

        final var proceeds = estimateOrderCost(
                OrderType.SELL, asset, quantity, unitPrice, fee, cashFlowTarget
        );

        final var sellTransaction = InvestmentTransaction.of(
                Asset.of(asset, quantity.negate()), // Asset outflow
                Category.INVESTMENT_SELL,
                date,
                getSellOrderDescription(quantity, asset, unitPrice),
                notes,
                unitPrice,
                fee
        );
        sourceAccount.addTransaction(sellTransaction);

        return switch (cashFlowTarget) {
            case PaymentSource.CashAccountChannel cashDst -> {
                final var cashTransaction = CashTransaction.of(
                        proceeds, // Cash inflow
                        Category.INVESTMENT_SELL,
                        date,
                        "Proceeds from selling " + quantity + " " + asset,
                        notes
                );
                cashDst.account().addTransaction(cashTransaction);
                yield new OrderResult.SellWithCashSuccess(sellTransaction, cashTransaction);
            }
            case PaymentSource.StableCoinPositionChannel stableCoinDst -> {
                final var stableCoinBuyTransaction = InvestmentTransaction.of(
                        Asset.of(
                                stableCoinDst.stableCoin(),
                                exchangeRateProvider.convert(proceeds, stableCoinDst.stableCoin()).amount()
                        ), // Stablecoin inflow
                        Category.INVESTMENT_BUY,
                        date,
                        "Convert proceeds to " + stableCoinDst.stableCoin(),
                        notes,
                        Asset.of(stableCoinDst.stableCoin(), BigDecimal.ONE),   // 1:1 currently
                        // No fee for the conversion transaction itself, fee is accounted in the sell transaction
                        Asset.zero(stableCoinDst.stableCoin())
                );
                stableCoinDst.account().addTransaction(stableCoinBuyTransaction);
                yield new OrderResult.SellWithStablesSuccess(sellTransaction, stableCoinBuyTransaction);
            }
            case PaymentSource.NoPaymentChannel noDst -> {
                yield new OrderResult.SellNoPaymentSuccess(sellTransaction);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderResult executeTransferOrder(
            final InvestmentAccount sourceAccount,
            final InvestmentAccount targetAccount,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final LocalDate date,
            final String notes
    ) {
        if (!canTransfer(sourceAccount, targetAccount, asset, quantity)) {
            final var available = sourceAccount.getPositionForAsset(asset)
                    .map(Position::quantity)
                    .orElse(BigDecimal.ZERO);
            return new OrderResult.InsufficientAssets(quantity, available);
        }
        final var sourcePosition = sourceAccount.getPositionForAsset(asset).orElseThrow();
        final var avgCost = sourcePosition.averageBasisCost();

        final var transferOutTransaction = InvestmentTransaction.of(
                Asset.of(asset, quantity.negate()), // Asset outflow
                Category.INVESTMENT_SELL,
                date,
                "Transfer " + quantity + " " + asset + " to account " + targetAccount.getName(),
                notes,
                avgCost,
                Asset.zero(asset)  // No fee for transfers in this implementation
        );
        sourceAccount.addTransaction(transferOutTransaction);
        final var transferInTransaction = InvestmentTransaction.of(
                Asset.of(asset, quantity), // Asset inflow
                Category.INVESTMENT_BUY,
                date,
                "Receive " + quantity + " " + asset + " from account " + sourceAccount.getName(),
                notes,
                avgCost, // No unit price for transfers
                Asset.zero(asset)  // No fee for transfers in this implementation
        );
        targetAccount.addTransaction(transferInTransaction);

        return new OrderResult.TransferSuccess(transferOutTransaction, transferInTransaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportResult exportCurrentAccountData(final File file) {
        Objects.requireNonNull(file);
        final var account = getCurrentAccountOrThrow();
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();
        try (
                FileWriter wr = new FileWriter(file);
                CSVPrinter pr = new CSVPrinter(wr, csvFormat)
        ) {
            pr.printRecord("name", account.getName());
            pr.printRecord("Currency", account.getBaseCurrency());
            pr.printRecord("Balance", account.getBalance());
            pr.printRecord("All time profit", account.getTotalProfitLoss());
            pr.printRecord("Total cost basis", account.getTotalCostBasis());

            pr.println();
            pr.printRecord("Asset", "Quantity", "Avg Cost", "Market Value", "Unrealized P/L", "Unrealized P/L %");
            for (final var position : account.getPositions()) {
                pr.printRecord(
                        position.asset(),
                        position.quantity(),
                        position.averageBasisCost(),
                        position.currentMarketValue(),
                        position.getUnrealizedProfitLoss(),
                        position.getUnrealizedProfitLossPercentage()
                );
            }

            pr.println();
            pr.printRecord("Date", "Currency", "Quantity", "Unit Price", "Fee", "Note");
            for (final var transaction : account.getHistory().getTransactions()) {
                pr.printRecord(
                        transaction.getDate(),
                        transaction.getAsset().currency(),
                        transaction.getAsset().amount(),
                        transaction.getUnitPrice(),
                        transaction.getFee(),
                        transaction.getNotes()
                );
            }

            return new ExportResult.Success(file);

        } catch (final IOException e) {
            return new ExportResult.Error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Position> getBestPerformer() {
        return getPositions().stream()
                .max(Comparator.comparing(Position::getUnrealizedProfitLossPercentage));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Position> getWorstPerformer() {
        return getPositions().stream()
                .min(Comparator.comparing(Position::getUnrealizedProfitLossPercentage));
    }

}
