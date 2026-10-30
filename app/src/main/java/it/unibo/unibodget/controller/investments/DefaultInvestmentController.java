package it.unibo.unibodget.controller.investments;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.investment.ExportResult;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.CreateNewInvestmentWalletRequestedEvent;
import it.unibo.unibodget.model.utils.event.NewWalletAddedEvent;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import it.unibo.unibodget.persistency.CsvExportFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVPrinter;

/**
 * Default implementation of {@link InvestmentController}. Coordinates buy/sell/transfer orders
 * across {@link InvestmentAccount}s and {@link it.unibo.unibodget.model.wallet.CashAccount}s via
 * the injected services.
 */
public class DefaultInvestmentController implements InvestmentController {

    private static final String CSV_COL_CURRENCY = "Currency";
    private static final String CSV_COL_WALLET = "Wallet";
    private static final String CSV_COL_ASSET = "Asset";
    private static final String CSV_COL_QUANTITY = "Quantity";
    private static final String CSV_COL_DATE = "Date";
    private static final String CSV_COL_NOTES = "Notes";
    private static final String CSV_COL_DESCRIPTION = "Description";
    private static final String CSV_COL_CATEGORY = "Category";
    private static final String CSV_COL_TYPE = "Type";
    private static final String CSV_COL_FEE = "Fee";
    private static final String CSV_ZERO_FEE = "0.00";

    private final InvestmentAccountService investmentAccountService;
    private final CashAccountService cashAccountService;
    private final CurrencyConverter exchangeRateProvider;
    private final Settings settings;
    private final InvestmentsSnapshotService snapshotService;

    /**
     * Creates a new controller wiring together all required services.
     *
     * @param investmentAccountService manages investment wallets
     * @param cashAccountService manages cash wallets
     * @param exchangeRateProvider converts between currencies
     * @param settings user preferences (base currency, …)
     * @param snapshotService persists balance snapshots after each order
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of stateful services")
    public DefaultInvestmentController(
            final InvestmentAccountService investmentAccountService,
            final CashAccountService cashAccountService,
            final CurrencyConverter exchangeRateProvider,
            final Settings settings,
            final InvestmentsSnapshotService snapshotService) {
        this.investmentAccountService = Objects.requireNonNull(investmentAccountService);
        this.cashAccountService = Objects.requireNonNull(cashAccountService);
        this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider);
        this.settings = Objects.requireNonNull(settings);
        this.snapshotService = Objects.requireNonNull(snapshotService);

        MessageBus.subscribe(
                CreateNewInvestmentWalletRequestedEvent.class,
                this::onCreateNewWalletRequestedEvent);
    }

    /**
     * Returns the snapshot service used by this controller.
     *
     * @return the snapshot service
     */
    public InvestmentsSnapshotService getSnapshotService() {
        return this.snapshotService;
    }

    private void onCreateNewWalletRequestedEvent(
            final CreateNewInvestmentWalletRequestedEvent event) {
        final var name = event.name();
        final var currency = event.currency();
        final var newWallet = new InvestmentAccount(name, currency, exchangeRateProvider);
        investmentAccountService.addWallet(newWallet);
        investmentAccountService.selectWallet(newWallet.getId());
        MessageBus.send(new NewWalletAddedEvent());
    }

    /** {@inheritDoc} */
    @Override
    public List<InvestmentAccount> getAllInvestmentAccounts() {
        return investmentAccountService.getWallets();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<InvestmentAccount> getCurrentInvestmentAccount() {
        return investmentAccountService.getCurrentWallet();
    }

    /** {@inheritDoc} */
    @Override
    public boolean selectWallet(final UUID walletId) {
        return investmentAccountService.selectWallet(walletId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean renameWallet(final UUID walletId, final String newName) {
        return investmentAccountService.renameWallet(walletId, newName);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeWallet(final UUID walletId) {
        return investmentAccountService.removeWallet(walletId);
    }

    /**
     * Helper method to retrieve the currently selected investment account or throw an exception if
     * none is selected.
     *
     * @return the currently selected InvestmentAccount
     * @throws IllegalStateException if no investment account is currently selected
     */
    private InvestmentAccount getCurrentAccountOrThrow() {
        return getCurrentInvestmentAccount()
                .orElseThrow(
                        () -> new IllegalStateException("No current investment account selected"));
    }

    /** {@inheritDoc} */
    @Override
    public Asset getAggregatedBalance() {
        return getAllInvestmentAccounts().stream()
                .map(InvestmentAccount::getBalance)
                .map(
                        balance ->
                                exchangeRateProvider
                                        .convert(
                                                balance.amount(),
                                                balance.currency(),
                                                settings.getBaseCurrencyUnit())
                                        .getAsset())
                .reduce(Asset.zero(settings.getBaseCurrencyUnit()), Asset::add);
    }

    /** {@inheritDoc} */
    @Override
    public Asset getCurrentBalance() {
        return getCurrentAccountOrThrow().getBalance();
    }

    /** {@inheritDoc} */
    @Override
    public Asset getCurrentAllTimeProfitLoss() {
        return getCurrentAccountOrThrow().getTotalProfitLoss();
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getCurrentAllTimeProfitLossPercentage() {
        return getCurrentAccountOrThrow().getTotalProfitLossPercentage();
    }

    /** {@inheritDoc} */
    @Override
    public Asset getCurrentTotalCostBasis() {
        return getCurrentAccountOrThrow().getTotalCostBasis();
    }

    /** {@inheritDoc} */
    @Override
    public List<Position> getPositions() {
        return getCurrentAccountOrThrow().getPositions();
    }

    /** {@inheritDoc} */
    @Override
    public List<InvestmentTransaction> getTransactionHistory() {
        return getCurrentAccountOrThrow().getHistory().getTransactions();
    }

    /** {@inheritDoc} */
    @Override
    public List<CurrencyUnit> getAllTradeableAssets() {
        return Stream.<CurrencyUnit>concat(
                        Arrays.stream(StockMarketCurrency.values()),
                        Arrays.stream(CryptoCurrency.values()))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<CurrencyUnit> getAllOwnedAssets() {
        return getPositions().stream().map(Position::asset).distinct().toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<CurrencyUnit> getOwnedStableCoins() {
        return getAllOwnedAssets().stream()
                .filter(
                        asset ->
                                asset instanceof CryptoCurrency
                                        && ((CryptoCurrency) asset).isStableCoin())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Asset getCurrentMarketPrice(final CurrencyUnit asset) {
        // return exchangeRateProvider.convert(
        //         Asset.of(asset, BigDecimal.ONE),
        //         getCurrentAccountOrThrow().getBaseCurrency()
        // );
        return exchangeRateProvider
                .convert(BigDecimal.ONE, asset, getCurrentAccountOrThrow().getBaseCurrency())
                .getAsset();
    }

    /** {@inheritDoc} */
    @Override
    public List<CashAccount> getAvailableCashAccounts() {
        return cashAccountService.getWallets();
    }

    /**
     * Resolves the target currency from a payment source, falling back to {@code def} if none is
     * specified.
     *
     * @param paymentSource the payment channel for the order
     * @param def default currency used when the source carries no currency info
     * @return the resolved target currency
     */
    private CurrencyUnit targetCurrencyOf(
            final PaymentSource paymentSource, final CurrencyUnit def) {
        return switch (paymentSource) {
            case PaymentSource.CashAccountChannel cashSrc -> cashSrc.account().getBaseCurrency();
            case PaymentSource.StableCoinPositionChannel stableCoinSrc ->
                    stableCoinSrc.stableCoin();
            // If no payment source specified, default to the asset's currency for cost estimation
            case PaymentSource.NoPaymentChannel() -> def;
        };
    }

    /**
     * Computes the order total in the currency of the payment source.
     *
     * @param quantity number of units traded
     * @param unitPrice price per unit
     * @param fee transaction fee
     * @param paymentSource channel used for payment (determines the target currency)
     * @param addFee {@code true} for buys (fee added), {@code false} for sells (fee subtracted)
     * @return the estimated cost/proceeds in the target currency
     */
    private Asset estimateOrderInTargetCurrency(
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final PaymentSource paymentSource,
            final boolean addFee) {
        var nativeTotalCost = unitPrice.multiply(quantity);
        nativeTotalCost = addFee ? nativeTotalCost.add(fee) : nativeTotalCost.subtract(fee);
        final CurrencyUnit targetCurrency =
                targetCurrencyOf(paymentSource, getCurrentAccountOrThrow().getBaseCurrency());

        return nativeTotalCost.currency().equals(targetCurrency)
                ? nativeTotalCost
                // : exchangeRateProvider.convert(nativeTotalCost, targetCurrency);
                : exchangeRateProvider
                        .convert(
                                nativeTotalCost.amount(),
                                nativeTotalCost.currency(),
                                targetCurrency)
                        .getAsset();
    }

    /** {@inheritDoc} */
    @Override
    public Asset estimateOrderCost(
            final OrderType orderType,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final PaymentSource paymentSource) {
        return switch (orderType) {
            case BUY ->
                    estimateOrderInTargetCurrency(quantity, unitPrice, fee, paymentSource, true);
            case SELL ->
                    estimateOrderInTargetCurrency(quantity, unitPrice, fee, paymentSource, false);
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
            case PaymentSource.StableCoinPositionChannel stableCoinSrc -> Optional.of(Asset.of(
                    stableCoinSrc.stableCoin(),
                    heldQuantity(stableCoinSrc.account(), stableCoinSrc.stableCoin())
            ));
            // no payment source, so there is no balance to check
            case PaymentSource.NoPaymentChannel() -> Optional.empty();
        };
    }

    /**
     * Checks if the payment source has enough money to pay the given cost.
     *
     * @param paymentSource the channel used to pay
     * @param cost          the cost of the order, in the currency of the payment source
     * @return {@code true} if the available balance covers the cost, always {@code true} with no payment source
     */
    private boolean hasEnough(final PaymentSource paymentSource, final Asset cost) {
        return availableAsset(paymentSource)
                .map(available -> available.compareTo(cost) >= 0)
                .orElse(true);
    }

    /**
     * Returns how much of an asset an account holds.
     *
     * @param account the investment account
     * @param asset   the asset to look for
     * @return the quantity of the open position, or zero if there is no position on that asset
     */
    private static BigDecimal heldQuantity(final InvestmentAccount account, final CurrencyUnit asset) {
        return account.getPositionForAsset(asset)
                .map(Position::quantity)
                .orElse(BigDecimal.ZERO);
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
        return hasEnough(paymentSource, estimateOrderCost(OrderType.BUY, asset, quantity, unitPrice, fee, paymentSource));
    }

    /** {@inheritDoc} */
    @Override
    public boolean canSell(
            final InvestmentAccount src,
            final CurrencyUnit asset,
            final BigDecimal quantity
    ) {
        // without a position there is nothing to sell
        return src.getPositionForAsset(asset)
                .map(position -> position.quantity().compareTo(quantity) >= 0)
                .orElse(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canTransfer(
            final InvestmentAccount src,
            final InvestmentAccount dst,
            final CurrencyUnit asset,
            final BigDecimal quantity
    ) {
        // cannot transfer to the same account, for the rest its like a sell
        return !src.getId().equals(dst.getId()) && canSell(src, asset, quantity);
    }

    /**
     * Builds the description of a buy or sell order, e.g. "Buy 3 BTC @ ...".
     *
     * @param action    "Buy" or "Sell"
     * @param quantity  number of units traded
     * @param asset     the traded asset
     * @param unitPrice price per unit
     * @return the description of the order
     */
    private static String orderDescription(
            final String action,
            final BigDecimal quantity,
            final CurrencyUnit asset,
            final Asset unitPrice
    ) {
        return action + " " + quantity + " " + asset + " @ " + unitPrice;
    }

    /**
     * Adds to a cash account the money that goes out for a buy or comes in from a sell.
     *
     * @param channel      the cash account used for the order
     * @param signedAmount the money moved: negative goes out (buy), positive comes in (sell)
     * @param category     the category of the cash transaction
     * @param date         date of the order
     * @param description  description of the cash transaction
     * @param notes        notes of the order
     * @return the cash transaction added to the account
     */
    private static CashTransaction moveCash(
            final PaymentSource.CashAccountChannel channel,
            final Asset signedAmount,
            final Category category,
            final LocalDate date,
            final String description,
            final String notes
    ) {
        final var transaction = CashTransaction.of(signedAmount, category, date, description, notes);
        channel.account().addTransaction(transaction);
        return transaction;
    }

    /**
     * Adds to a stablecoin position the coins used for a buy or received from a sell.
     *
     * @param channel        the stablecoin position used for the order
     * @param signedQuantity coins moved, already in the stablecoin: negative go out (buy), positive come in (sell)
     * @param date           date of the order
     * @param description    description of the stablecoin transaction
     * @param notes          notes of the order
     * @return the investment transaction added to the account of the stablecoin
     */
    private InvestmentTransaction moveStableCoin(
            final PaymentSource.StableCoinPositionChannel channel,
            final BigDecimal signedQuantity,
            final LocalDate date,
            final String description,
            final String notes
    ) {
        final var stableCoin = channel.stableCoin();
        final var baseCurrency = channel.account().getBaseCurrency();
        final var transaction = InvestmentTransaction.of(
                Asset.of(stableCoin, signedQuantity),
                // coins that go out are sold, coins that come in are bought
                signedQuantity.signum() < 0 ? Category.INVESTMENT_SELL : Category.INVESTMENT_BUY,
                date,
                description,
                notes,
                // price of ONE stablecoin in the base currency of its account. before it was the total cost
                // of the order (buy) or "1 USDT" also with an account in EUR (sell), so the P/L was wrong
                exchangeRateProvider.convert(BigDecimal.ONE, stableCoin, baseCurrency).getAsset(),
                // the fee is already in the main transaction of the order, so here is zero
                Asset.zero(baseCurrency)
        );
        channel.account().addTransaction(transaction);
        return transaction;
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
        if (!hasEnough(paymentSource, cost)) {
            final var available = availableAsset(paymentSource).orElse(Asset.zero(cost.currency()));
            return new OrderResult.InsufficientFunds(cost, available);
        }

        final var description = orderDescription("Buy", quantity, asset, unitPrice);
        final var buyTransaction = InvestmentTransaction.of(
                Asset.of(asset, quantity), Category.INVESTMENT_BUY, date, description, notes, unitPrice, fee);
        targetAccount.addTransaction(buyTransaction);

        // the cost goes out of the payment source
        final var costOut = Asset.of(cost.currency(), cost.amount().negate());
        return switch (paymentSource) {
            case PaymentSource.CashAccountChannel cashSrc -> new OrderResult.BuyWithCashSuccess(
                    moveCash(cashSrc, costOut, Category.INVESTMENT_BUY, date, description, notes),
                    buyTransaction);
            case PaymentSource.StableCoinPositionChannel stableCoinSrc -> new OrderResult.BuyWithStablesSuccess(
                    moveStableCoin(stableCoinSrc, costOut.amount(), date,
                            "Use " + cost + " from stablecoin position to buy " + quantity + " " + asset, notes),
                    buyTransaction);
            case PaymentSource.NoPaymentChannel() -> new OrderResult.BuyNoPaymentSuccess(buyTransaction);
        };
    }

    /** {@inheritDoc} */
    @Override
    public OrderResult executeSellOrder(
            final InvestmentAccount sourceAccount,
            final PaymentSource cashFlowTarget,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final Asset unitPrice,
            final Asset fee,
            final LocalDate date,
            final String notes) {
        if (!canSell(sourceAccount, asset, quantity)) {
            return new OrderResult.InsufficientAssets(quantity, heldQuantity(sourceAccount, asset));
        }

        final var proceeds = estimateOrderCost(OrderType.SELL, asset, quantity, unitPrice, fee, cashFlowTarget);
        final var sellTransaction = InvestmentTransaction.of(
                Asset.of(asset, quantity.negate()), Category.INVESTMENT_SELL, date,
                orderDescription("Sell", quantity, asset, unitPrice), notes, unitPrice, fee);
        sourceAccount.addTransaction(sellTransaction);

        // the proceeds go in the payment target
        return switch (cashFlowTarget) {
            case PaymentSource.CashAccountChannel cashDst -> new OrderResult.SellWithCashSuccess(
                    sellTransaction,
                    moveCash(cashDst, proceeds, Category.INVESTMENT_SELL, date,
                            "Proceeds from selling " + quantity + " " + asset, notes));
            case PaymentSource.StableCoinPositionChannel stableCoinDst -> new OrderResult.SellWithStablesSuccess(
                    sellTransaction,
                    moveStableCoin(stableCoinDst, proceeds.amount(), date,
                            "Convert proceeds to " + stableCoinDst.stableCoin(), notes));
            case PaymentSource.NoPaymentChannel() -> new OrderResult.SellNoPaymentSuccess(sellTransaction);
        };
    }

    /** {@inheritDoc} */
    @Override
    public OrderResult executeTransferOrder(
            final InvestmentAccount sourceAccount,
            final InvestmentAccount targetAccount,
            final CurrencyUnit asset,
            final BigDecimal quantity,
            final LocalDate date,
            final String notes) {
        if (!canTransfer(sourceAccount, targetAccount, asset, quantity)) {
            return new OrderResult.InsufficientAssets(quantity, heldQuantity(sourceAccount, asset));
        }
        // the coins move at the average cost, so the cost basis stays the same in the other account
        final var avgCost = sourceAccount.getPositionForAsset(asset).orElseThrow().averageBasisCost();

        final var transferOutTransaction =
                InvestmentTransaction.of(
                        Asset.of(asset, quantity.negate()), // Asset outflow
                        Category.INVESTMENT_SELL,
                        date,
                        "Transfer "
                                + quantity
                                + " "
                                + asset
                                + " to account "
                                + targetAccount.getName(),
                        notes,
                        avgCost,
                        Asset.zero(asset) // No fee for transfers in this implementation
                        );
        sourceAccount.addTransaction(transferOutTransaction);
        final var transferInTransaction = InvestmentTransaction.of(
                Asset.of(asset, quantity), // Asset inflow
                Category.INVESTMENT_BUY,
                date,
                "Receive " + quantity + " " + asset + " from account " + sourceAccount.getName(),
                notes,
                avgCost,
                Asset.zero(asset)  // No fee for transfers in this implementation
        );
        targetAccount.addTransaction(transferInTransaction);

        return new OrderResult.TransferSuccess(transferOutTransaction, transferInTransaction);
    }

    /** {@inheritDoc} */
    @Override
    public ExportResult exportCurrentAccountData(final File file) {
        Objects.requireNonNull(file);

        final var account = getCurrentAccountOrThrow();

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, CsvExportFormat.getFormat())) {
            final String baseCurrency = account.getBaseCurrency().getCode();

            printer.printRecord(
                    CSV_COL_WALLET,
                    CSV_COL_CURRENCY,
                    CSV_COL_DATE,
                    CSV_COL_DESCRIPTION,
                    CSV_COL_CATEGORY,
                    CSV_COL_TYPE,
                    CSV_COL_ASSET,
                    CSV_COL_QUANTITY,
                    "Unit Price",
                    CSV_COL_FEE,
                    CSV_COL_NOTES);

            for (final var transaction : account.getHistory().getTransactions()) {
                final String feeStr = transaction.getFee() != null
                        ? formatAssetAmount(transaction.getFee())
                        : CSV_ZERO_FEE;
                final String categoryName = transaction.getCategory() != null
                        ? transaction.getCategory().getName()
                        : "";
                final String categoryType = transaction.getCategory() != null
                        && transaction.getCategory().getType() != null
                        ? transaction.getCategory().getType().name()
                        : "";
                final String notes = transaction.getNotes() != null
                        ? transaction.getNotes()
                        : "";

                printer.printRecord(
                        account.getName(),
                        baseCurrency,
                        transaction.getDate().toString(),
                        transaction.getDescription() != null ? transaction.getDescription() : "",
                        categoryName,
                        categoryType,
                        transaction.getAsset().currency().getCode(),
                        transaction.getAsset().amount().stripTrailingZeros().toPlainString(),
                        formatAssetAmount(transaction.getUnitPrice()),
                        feeStr,
                        notes);
            }

            // Completes writing before reporting success.
            printer.flush();
        } catch (final IOException exception) {
            return new ExportResult.Error(exception.getMessage());
        }

        return new ExportResult.Success(file);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Position> getBestPerformer() {
        return getPositions().stream()
                .max(Comparator.comparing(Position::getUnrealizedProfitLossPercentage));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Position> getWorstPerformer() {
        return getPositions().stream()
                .min(Comparator.comparing(Position::getUnrealizedProfitLossPercentage));
    }

    /**
     * Formats an asset amount using the currency's display decimals.
     *
     * @param asset the asset to format
     * @return the formatted numeric string
     */
    private static String formatAssetAmount(final Asset asset) {
        if (asset == null) {
            return "";
        }
        final int decimals = asset.currency().getDisplayDecimals();
        return asset.amount().setScale(decimals, RoundingMode.HALF_UP).toPlainString();
    }
}
