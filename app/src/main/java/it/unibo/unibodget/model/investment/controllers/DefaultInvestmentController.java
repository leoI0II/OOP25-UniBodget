package it.unibo.unibodget.model.investment.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.converter.provider.ExchangeRateProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.investment.ExportResult;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

public class DefaultInvestmentController implements InvestmentController {

    private final InvestmentAccountService investmentAccountService;
    private final CashAccountService cashAccountService;
    private final ExchangeRateProvider exchangeRateProvider;
    private final List<CurrencyUnit> displayCurrencies;

    public DefaultInvestmentController(
        InvestmentAccountService investmentAccountService,
        CashAccountService cashAccountService,
        ExchangeRateProvider exchangeRateProvider,
        List<CurrencyUnit> displayCurrencies
    ) {
        this.investmentAccountService = Objects.requireNonNull(investmentAccountService);
        this.cashAccountService = Objects.requireNonNull(cashAccountService);
        this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider);
        this.displayCurrencies = Objects.requireNonNull(displayCurrencies);
    }
    
    @Override
    public List<InvestmentAccount> getAllInvestmentAccounts() {
        return investmentAccountService.getWallets();
    }

    @Override
    public Optional<InvestmentAccount> getCurrentInvestmentAccount() {
        return investmentAccountService.getCurrentWallet();
    }

    @Override
    public boolean selectWallet(UUID walletId) {
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

    @Override
    public Map<CurrencyUnit, Asset> getAggregatedBalances() {
        return getDisplayCurrencies().stream()
            .collect(Collectors.toMap(
                currency -> currency,
                currency -> getAllInvestmentAccounts().stream()
					.map(InvestmentAccount::getBalance)
					.map(balance -> exchangeRateProvider.convert(balance, currency))
					.reduce(Asset.zero(currency), Asset::add)
            ));
    }

    @Override
    public List<CurrencyUnit> getDisplayCurrencies() {
        return displayCurrencies;
    }

    @Override
    public Asset getCurrentBalance() {
        return getCurrentAccountOrThrow().getBalance();
    }

    @Override
    public Asset getCurrentAllTimeProfitLoss() {
        return getCurrentAccountOrThrow().getTotalProfitLoss();
    }

    @Override
    public BigDecimal getCurrentAllTimeProfitLossPercentage() {
        return getCurrentAccountOrThrow().getTotalProfitLossPercentage();
    }

    @Override
    public Asset getCurrentTotalCostBasis() {
        return getCurrentAccountOrThrow().getTotalCostBasis();
    }

    @Override
    public List<Position> getPositions() {
        return getCurrentAccountOrThrow().getPositions();
    }

    @Override
    public List<InvestmentTransaction> getTransactionHistory() {
        return getCurrentAccountOrThrow().getHistory().getTransactions();
    }

    // for all buy dialog (all curencies the user can buy, including those not currently owned)
    @Override
	public List<CurrencyUnit> getAllTradeableAssets() {
		return Stream.<CurrencyUnit>concat(
            Arrays.stream(StockMarketCurrency.values()),
            Arrays.stream(CryptoCurrency.values())
        ).toList();
	}

    // for sell dialog (only assets currently owned)
	@Override
	public List<CurrencyUnit> getAllOwnedAssets() {
		return getPositions().stream()
			.map(Position::asset)
			.distinct()
			.toList();
	}

    // subset of all owned assets for buy by stables
    @Override
	public List<CurrencyUnit> getOwnedStableCoins() {
		return getAllOwnedAssets().stream()
			.filter(asset -> asset instanceof CryptoCurrency && ((CryptoCurrency)asset).isStableCoin())
			.toList();
	}

    @Override
    public Asset getCurrentMarketPrice(CurrencyUnit asset) {
        return exchangeRateProvider.convert(
            Asset.of(asset, BigDecimal.ONE), 
            getCurrentAccountOrThrow().getBaseCurrency()
        );
    }

    @Override
    public List<CashAccount> getAvailableCashAccounts() {
        return cashAccountService.getWallets();
    }

    private CurrencyUnit targetCurrencyOf(PaymentSource paymentSource, CurrencyUnit def) {
        return switch(paymentSource) {
            case PaymentSource.CashAccountChannel cashSrc -> cashSrc.account().getBaseCurrency();
            case PaymentSource.StableCoinPositionChannel stableCoinSrc -> stableCoinSrc.stableCoin();
            case PaymentSource.NoPaymentChannel noSrc -> def; // If no payment source specified, default to the asset's currency for cost estimation
        };
    }

    private Asset estimateOrderInTargetCurrency(
        BigDecimal quantity,
        Asset unitPrice,
        Asset fee,
        PaymentSource paymentSource,
        boolean addFee
    ) {
        var nativeTotalCost = unitPrice.multiply(quantity);
        nativeTotalCost = addFee ? nativeTotalCost.add(fee) : nativeTotalCost.subtract(fee);
        final CurrencyUnit targetCurrency = targetCurrencyOf(paymentSource, nativeTotalCost.currency());

        return nativeTotalCost.currency().equals(targetCurrency) 
            ? nativeTotalCost 
            : exchangeRateProvider.convert(nativeTotalCost, targetCurrency);
    }

    @Override
    public Asset estimateOrderCost(
		OrderType orderType, 
		CurrencyUnit asset, 
		BigDecimal quantity, 
		Asset unitPrice,
        Asset fee, 
		PaymentSource paymentSource
    ) {    
		return switch (orderType) {
            case BUY -> estimateOrderInTargetCurrency(quantity, unitPrice, fee, paymentSource, true);
            case SELL -> estimateOrderInTargetCurrency(quantity, unitPrice, fee, paymentSource, false);
            case TRANSFER -> Asset.of(asset, quantity);
        };
    }

    private Optional<Asset> availableAsset(PaymentSource paymentSource) {
        return switch(paymentSource) {
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
            case PaymentSource.NoPaymentChannel noSrc -> Optional.empty(); // If no payment source specified, available amount is considered zero for the purpose of cost comparison
        };
    }

    @Override
    public boolean canBuy(
		PaymentSource paymentSource, 
		CurrencyUnit asset, 
		BigDecimal quantity, 
		Asset unitPrice,
        Asset fee) {
        
		final var estimatedCost = estimateOrderCost(OrderType.BUY, asset, quantity, unitPrice, fee, paymentSource);
        final var availableOpt = availableAsset(paymentSource);
        if (availableOpt.isEmpty()) {
            return true; // means there is no payment source, so yes to transfer
        }
        final var available = availableOpt.get();
        return available.compareTo(estimatedCost) >= 0;
    }

    @Override
    public boolean canSell(InvestmentAccount src, CurrencyUnit asset, BigDecimal quantity) {
		final var position = src.getPositions().stream()
			.filter(pos -> pos.asset().equals(asset))
			.findFirst();

		return position.isPresent() && position.get().quantity().compareTo(quantity) >= 0;
    }

    @Override
    public boolean canTransfer(InvestmentAccount src, InvestmentAccount dst, CurrencyUnit asset, BigDecimal quantity) {
		if (src.getId().equals(dst.getId())) {
            return false; // Cannot transfer to the same account
        }
        final var position = src.getPositions().stream()
			.filter(pos -> pos.asset().equals(asset))
			.findFirst();

		return position.isPresent() && position.get().quantity().compareTo(quantity) >= 0;
	}

    @Override
    public OrderResult executeBuyOrder(
		InvestmentAccount targetAccount, 
		PaymentSource paymentSource, 
		CurrencyUnit asset,
        BigDecimal quantity, 
		Asset unitPrice, 
		Asset fee, 
		LocalDate date, 
		String notes) {
		
		final var cost = estimateOrderCost(OrderType.BUY, asset, quantity, unitPrice, fee, paymentSource);
		
		if (!canBuy(paymentSource, asset, quantity, unitPrice, fee)) {
			final var available = availableAsset(paymentSource).orElse(Asset.zero(cost.currency()));
			return new OrderResult.InsufficientFunds(cost, available);
		}

		InvestmentTransaction investmentTransactionIn = InvestmentTransaction.of(
            Asset.of(asset, quantity),
            Category.INVESTMENT_BUY,
            date,
            "Buy " + quantity + " " + asset + " @ " + unitPrice,
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
					"Buy " + quantity + " " + asset + " @ " + unitPrice,
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
					Asset.of(stableCoinSrc.stableCoin(), exchangeRateProvider.convert(cost, stableCoinSrc.stableCoin()).amount()), // Unit price in terms of the stablecoin
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

    @Override
    public OrderResult executeSellOrder(
        InvestmentAccount sourceAccount,
        PaymentSource cashFlowTarget,
        CurrencyUnit asset, 
        BigDecimal quantity,
        Asset unitPrice, 
        Asset fee, 
        LocalDate date, 
        String notes
    ) {
        if (!canSell(sourceAccount, asset, quantity)) {
            final var availableQty = sourceAccount.getPositions().stream()
                .filter(pos -> pos.asset().equals(asset))
                .findFirst()
                .map(p -> p.quantity())
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
            "Sell " + quantity + " " + asset + " @ " + unitPrice,
            notes,
            unitPrice,
            fee
        );
        sourceAccount.addTransaction(sellTransaction);

        return switch(cashFlowTarget) {
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
                    Asset.of(stableCoinDst.stableCoin(), exchangeRateProvider.convert(proceeds, stableCoinDst.stableCoin()).amount()), // Stablecoin inflow
                    Category.INVESTMENT_BUY,
                    date,
                    "Convert proceeds to " + stableCoinDst.stableCoin(),
                    notes,
                    Asset.of(stableCoinDst.stableCoin(), BigDecimal.ONE),   // 1:1 currently
                    Asset.zero(stableCoinDst.stableCoin()) // No fee for the conversion transaction itself, fee is accounted in the sell transaction
                );
                stableCoinDst.account().addTransaction(stableCoinBuyTransaction);
                yield new OrderResult.SellWithStablesSuccess(sellTransaction, stableCoinBuyTransaction);
            }
            case PaymentSource.NoPaymentChannel noDst -> {
                yield new OrderResult.SellNoPaymentSuccess(sellTransaction);
            }
        };

    }

    @Override
    public OrderResult executeTransferOrder(
        InvestmentAccount sourceAccount, 
        InvestmentAccount targetAccount,
        CurrencyUnit asset, 
        BigDecimal quantity, 
        LocalDate date, 
        String notes
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

    @Override
    public ExportResult exportCurrentAccountData(final File file) {
        Objects.requireNonNull(file);
        var account = getCurrentAccountOrThrow();
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();
        try (
            final FileWriter wr = new FileWriter(file);
            final CSVPrinter pr = new CSVPrinter(wr, csvFormat)
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
            
        } catch (IOException e) {
            return new ExportResult.Error(e.getMessage());
        }
    }
    
}
