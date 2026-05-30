package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unibo.unibodget.model.converter.provider.ExchangeRateProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * A wallet specialized for investment assets (stocks, crypto, funds).
 *
 * <p>The balance is computed dynamically as the sum of current market values
 * of all open positions, using a {@link ExchangeRateProvider} for live prices.
 * Profit/loss is tracked separately as realized (from closed sells)
 * and unrealized (on open positions), following the Average Cost Method.
 */
public class InvestmentAccount extends AbstractWallet<InvestmentTransaction> {

    private final ExchangeRateProvider exchangeRateProvider;

    /**
     * Creates an InvestmentAccount with an existing transaction history.
     *
     * @param name          the display name; if empty a default name is generated
     * @param baseCurrency  the currency in which the balance is expressed
     * @param history       the pre-existing transaction ledger
     * @param exchangeRateProvider the provider used to fetch current market prices
     */
    public InvestmentAccount(
        final String name, 
        final CurrencyUnit baseCurrency, 
        final Historical<InvestmentTransaction> history,
        final ExchangeRateProvider exchangeRateProvider
    ) {
        super(name, baseCurrency, history, "Investment Account");
        this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider, "Price provider cannot be null");
    }

    /**
     * Creates an InvestmentAccount with an empty transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     * @param exchangeRateProvider the provider for fetching market prices
     */
    public InvestmentAccount(
        final String name, 
        final CurrencyUnit baseCurrency, 
        final ExchangeRateProvider exchangeRateProvider
    ) {
        this(name, baseCurrency, new Historical<>(), exchangeRateProvider);
    }

    /**
     * Current balance = sum of market values of all open positions, in baseCurrency.
     *
     * @return an {@link Asset} representing the total balance in the base currency
     */
    @Override
    public Asset getBalance() {
        return getPositions().stream()
            .map(Position::currentMarketValue)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Computes the current open positions based on the transaction history.
     *
     * <p>Positions are grouped by asset, and for each asset the net quantity, avg cost, and
     * current market value are calculated. Closed positions (net quantity zero or neg) are
     * filtered out.
     *
     * @return a list of open positions for each held asset
     */
    public List<Position> getPositions() {
        return getHistory().getTransactions().stream()
                .collect(Collectors.groupingBy(t -> t.getAsset().currency()))
                .entrySet().stream()
                .map(e -> computePosition(e.getKey(), e.getValue()))
                .filter(p -> !p.isClosedPosition()) // Exclude closed positions
                .toList();
    }

    /**
     * Returns the open position for a specific asset, if one exists.
     *
     * @param asset the asset to look up
     * 
     * @return an {@link Optional} containing the {@link Position}, or empty if not held
     */
    public Optional<Position> getPositionForAsset(final CurrencyUnit asset) {
        return getPositions().stream()
                .filter(p -> p.asset().equals(asset))
                .findFirst();
    }

    /**
     * Computes the {@link Position} for a single asset from its sorted transaction list.
     * Uses a weighted average cost basis updated on each buy; reduces cost proportionally on each sell.
     *
     * @param asset        the asset being computed
     * @param transactions all transactions for that asset (will be sorted by date internally)
     * @return the resulting {@link Position} including current market value from the price provider
     */
    private Position computePosition(
        final CurrencyUnit asset, 
        final List<InvestmentTransaction> transactions
    ) {
        var totalCost = BigDecimal.ZERO;
        var totalQty = BigDecimal.ZERO;

        for (final var transaction : sortedByDate(transactions)) {
            final var qty = transaction.getAsset().amount();
            final var unitPrice = transaction.getUnitPrice().amount();
            final var fee = transaction.getFee() != null ? transaction.getFee().amount() : BigDecimal.ZERO;

            if (qty.signum() > 0) { // Buy
                totalQty = totalQty.add(qty);
                totalCost = totalCost.add(qty.multiply(unitPrice)).add(fee);
            } else { // Sell
                final var soldQty = qty.abs();
                final var avgCostBeforeSale = totalQty.signum() == 0
                        ? BigDecimal.ZERO
                        : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);
                totalCost = totalCost.subtract(avgCostBeforeSale.multiply(soldQty));
                totalQty = totalQty.add(qty); // qty is negative for sells
            }
        }
        final var avgCost = totalQty.signum() == 0
                ? BigDecimal.ZERO
                : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);
        final Asset currentPrice = exchangeRateProvider.convert(new Asset(asset, BigDecimal.ONE), getBaseCurrency());
        final Asset currentMarketValue = currentPrice.multiply(totalQty);
        return new Position(
            asset, 
            totalQty, 
            Asset.of(getBaseCurrency(), avgCost), 
            currentMarketValue);
    }

    /**
     * Returns a new list of transactions sorted in ascending chronological order.
     *
     * @param transactions the transactions to sort
     * @return a sorted, unmodifiable list
     */
    private List<InvestmentTransaction> sortedByDate(final List<InvestmentTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(InvestmentTransaction::getDate))
                .toList();
    }

    /**
     * Returns the total unrealized profit/loss across all open positions.
     * Sums {@link Position#getUnrealizedProfitLoss()} for each open position.
     *
     * @return the total unrealized P/L in the base currency
     */
    public Asset getUnrealizedProfitLoss() {
        return getPositions().stream()
            .map(Position::getUnrealizedProfitLoss)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Returns the total realized profit/loss from all completed (closed) sell operations.
     * Computed as (sell price − average cost basis) × quantity sold − fees, summed across all assets.
     *
     * @return the total realized P/L in the base currency
     */
    public Asset getRealizedProfitLoss() {
        return getHistory().getTransactions().stream()
            .collect(Collectors.groupingBy(t -> t.getAsset().currency()))
            .values().stream()
            .map(this::computeRealizedProfitLoss)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Returns the combined total profit/loss: realized + unrealized.
     *
     * @return the total P/L in the base currency
     */
    public Asset getTotalProfitLoss() {
        return getRealizedProfitLoss().add(getUnrealizedProfitLoss());
    }

    /**
     * Returns the total profit/loss as a percentage of the total cost basis.
     *
     * <p>Returns {@code 0} if there is no cost basis to avoid division by zero.</p>
     *
     * @return the P/L percentage, e.g. {@code 12.50} for +12.5%
     */
    public BigDecimal getTotalProfitLossPercentage() {
        final var totalCostBasis = getPositions().stream()
            .map(Position::getTotalCost)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
        if (totalCostBasis.amount().signum() == 0) {
            return BigDecimal.ZERO; // Avoid division by zero; define 0% P/L when no cost basis
        }
        return getTotalProfitLoss().amount()
            .divide(totalCostBasis.amount(), 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Returns the total cost basis across all open positions.
     *
     * <p>The cost basis is the sum of {@link Position#getTotalCost()} for each open position,
     * representing the total amount invested at average purchase price.</p>
     *
     * @return the total cost basis as an {@link Asset} in the base currency
     */
    public Asset getTotalCostBasis() {
        return getPositions().stream()
            .map(Position::getTotalCost)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Computes the realized profit/loss for a single asset from its transaction history.
     * On each sell, P/L = (sell price − avg cost basis) × sold quantity − fee.
     *
     * @param transactions all transactions for a single asset, in any order (sorted internally)
     * @return the realized P/L as an {@link Asset} in the base currency
     */
    private Asset computeRealizedProfitLoss(final List<InvestmentTransaction> transactions) {
        var realizedPL = BigDecimal.ZERO;
        var totalQty = BigDecimal.ZERO;
        var totalCost = BigDecimal.ZERO;

        for (final var transaction : sortedByDate(transactions)) {
            final var qty = transaction.getAsset().amount();
            final var unitPrice = transaction.getUnitPrice().amount(); // Price at which the asset was bought/sold
            final var fee = transaction.getFee() != null ? transaction.getFee().amount() : BigDecimal.ZERO;

            if (qty.signum() > 0) { //Buy
                totalCost = totalCost.add(qty.multiply(unitPrice)).add(fee);
                totalQty = totalQty.add(qty);
            } else { //Sell
                final var soldQty = qty.abs();
                final var avgCostBeforeSale = totalQty.signum() == 0
                        ? BigDecimal.ZERO
                        : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);
                final var pricePL = unitPrice.subtract(avgCostBeforeSale)
                        .multiply(soldQty)
                        .subtract(fee);
                realizedPL = realizedPL.add(pricePL);
                totalCost = totalCost.subtract(avgCostBeforeSale.multiply(soldQty));
                totalQty = totalQty.add(qty);
            }
        }
        return Asset.of(getBaseCurrency(), realizedPL);
    }

    /**
     * Returns whether the account holds enough quantity of an asset to cover a sell order.
     *
     * @param asset     the asset to sell
     * @param qtyToSell the quantity to sell; must be positive
     * @return {@code true} if the open position covers {@code qtyToSell}, {@code false} otherwise
     * @throws IllegalArgumentException if {@code qtyToSell} is not positive
     */
    public boolean canSell(final CurrencyUnit asset, final BigDecimal qtyToSell) {
        if (qtyToSell.signum() <= 0) {
            throw new IllegalArgumentException("Quantity to sell must be positive.");
        }
        return getPositionForAsset(asset)
            .map(pos -> pos.quantity().compareTo(qtyToSell) >= 0)
            .orElse(false);
    }

}
