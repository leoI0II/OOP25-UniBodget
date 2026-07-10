package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.converter.provider.PriceProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * Wallet specialized for investment assets. It computes balance from market
 * prices, tracks realized/unrealized profit/loss, and derives open positions
 * using the Average Cost Method. All values are expressed in the base currency.
 */
public class InvestmentAccount extends Wallet<InvestmentTransaction> {

    @JsonIgnore
    private final PriceProvider priceProvider;

    /**
     * Creates an InvestmentAccount with an existing transaction history.
     *
     * @param name the display name of the wallet
     * @param baseCurrency the currency used to express the balance
     * @param history the transaction ledger to attach
     * @param priceProvider provider used to fetch live market prices
     */
    public InvestmentAccount(
        final String name,
        final CurrencyUnit baseCurrency,
        final Historical<InvestmentTransaction> history,
        final PriceProvider priceProvider) {
        super(name, baseCurrency, history, "Investment Account");
        this.priceProvider = Objects.requireNonNull(priceProvider, "Price provider cannot be null");
    }

    /**
     * Creates an InvestmentAccount with an empty transaction history.
     *
     * @param name the display name of the wallet
     * @param baseCurrency the currency used to express the balance
     * @param priceProvider provider used to fetch live market prices
     */
    public InvestmentAccount(String name, CurrencyUnit baseCurrency, PriceProvider priceProvider) {
        this(name, baseCurrency, new Historical<>(), priceProvider);
    }

    /**
     * JSON constructor used when deserializing InvestmentAccount objects.
     * The price provider is not restored from JSON and must be reattached manually.
     *
     * @param name the wallet name
     * @param baseCurrency the currency used to express the balance
     * @param history the transaction ledger, or null for an empty one
     */
    @JsonCreator
    private InvestmentAccount(
            @JsonProperty("name") final String name,
            @JsonProperty("baseCurrency") final CurrencyUnit baseCurrency,
            @JsonProperty("history") final Historical<InvestmentTransaction> history) {

        super(
                name,
                baseCurrency,
                history != null ? history : new Historical<>(),
                "Investment Account"
        );
        this.priceProvider = null;
    }

    /**
     * Returns the price provider used to fetch live market prices.
     *
     * @return the {@link PriceProvider}, or null if deserialized from JSON
     */
    @JsonIgnore
    public PriceProvider getPriceProvider() {
        return priceProvider;
    }

    /**
     * Computes the current balance as the sum of market values of all open positions.
     *
     * @return an {@link Asset} representing the total balance in base currency
     */
    @Override
    public Asset getBalance() {
        return getPositions().stream()
            .map(Position::currentMarketValue)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Computes all open positions derived from the transaction history.
     * Closed positions (net quantity ≤ 0) are excluded.
     *
     * @return a list of open {@link Position} objects
     */
    @JsonIgnore
    public List<Position> getPositions() {
        return getHistory().getTransactions().stream()
                .collect(Collectors.groupingBy(t -> t.getAsset().currency()))
                .entrySet().stream()
                .map(e -> computePosition(e.getKey(), e.getValue()))
                .filter(p -> !p.isClosedPosition())
                .toList();
    }

    /**
     * Returns the open position for a specific asset, if present.
     *
     * @param asset the asset to search for
     * @return an {@link Optional} containing the position, or empty if none exists
     */
    public Optional<Position> getPositionForAsset(CurrencyUnit asset) {
        return getPositions().stream()
                .filter(p -> p.asset().equals(asset))
                .findFirst();
    }

    /**
     * Computes a single position using the Average Cost Method.
     * Buys increase cost and quantity; sells reduce cost proportionally.
     *
     * @param asset the asset being computed
     * @param transactions all transactions for the asset
     * @return the resulting {@link Position}
     */
    private Position computePosition(CurrencyUnit asset, List<InvestmentTransaction> transactions) {
        var totalCost = BigDecimal.ZERO;
        var totalQty = BigDecimal.ZERO;

        for (var transaction : sortedByDate(transactions)) {
            var qty = transaction.getAsset().amount();
            var unitPrice = transaction.getUnitPrice().amount();
            var fee = transaction.getFee() != null ? transaction.getFee().amount() : BigDecimal.ZERO;

            if (qty.signum() > 0) { // Buy
                totalQty = totalQty.add(qty);
                totalCost = totalCost.add(qty.multiply(unitPrice)).add(fee);
            } else { // Sell
                var soldQty = qty.abs();
                var avgCostBeforeSale = totalQty.signum() == 0
                        ? BigDecimal.ZERO
                        : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);
                totalCost = totalCost.subtract(avgCostBeforeSale.multiply(soldQty));
                totalQty = totalQty.add(qty);
            }
        }

        var avgCost = totalQty.signum() == 0
                ? BigDecimal.ZERO
                : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);

        Asset currentPrice = priceProvider.getCurrentPrice(asset, getBaseCurrency());
        Asset currentMarketValue = currentPrice.multiply(totalQty);

        return new Position(
            asset,
            totalQty,
            Asset.of(getBaseCurrency(), avgCost),
            currentMarketValue
        );
    }

    /**
     * Returns the transactions sorted chronologically.
     *
     * @param transactions the list to sort
     * @return a new sorted list
     */
    private List<InvestmentTransaction> sortedByDate(List<InvestmentTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(InvestmentTransaction::getDate))
                .toList();
    }

    /**
     * Computes total unrealized profit/loss across all open positions.
     *
     * @return an {@link Asset} representing unrealized P/L
     */
    @JsonIgnore
    public Asset getUnrealizedProfitLoss() {
        return getPositions().stream()
            .map(Position::getUnrealizedProfitLoss)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Computes total realized profit/loss from completed sell operations.
     *
     * @return an {@link Asset} representing realized P/L
     */
    @JsonIgnore
    public Asset getRealizedProfitLoss() {
        return getHistory().getTransactions().stream()
            .collect(Collectors.groupingBy(t -> t.getAsset().currency()))
            .values().stream()
            .map(this::computeRealizedProfitLoss)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Returns total profit/loss (realized + unrealized).
     *
     * @return an {@link Asset} representing total P/L
     */
    @JsonIgnore
    public Asset getTotalProfitLoss() {
        return getRealizedProfitLoss().add(getUnrealizedProfitLoss());
    }

    /**
     * Computes total profit/loss as a percentage of total cost basis.
     *
     * @return the P/L percentage, or zero if cost basis is zero
     */
    @JsonIgnore
    public BigDecimal getTotalProfitLossPercentage() {
        var totalCostBasis = getPositions().stream()
            .map(Position::getTotalCost)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);

        if (totalCostBasis.amount().signum() == 0) {
            return BigDecimal.ZERO;
        }

        return getTotalProfitLoss().amount()
            .divide(totalCostBasis.amount(), 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Returns the total cost basis across all open positions.
     *
     * @return an {@link Asset} representing total cost basis
     */
    @JsonIgnore
    public Asset getTotalCostBasis() {
        return getPositions().stream()
            .map(Position::getTotalCost)
            .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

    /**
     * Computes realized profit/loss for a single asset.
     *
     * @param transactions all transactions for the asset
     * @return realized P/L as an {@link Asset}
     */
    private Asset computeRealizedProfitLoss(List<InvestmentTransaction> transactions) {
        var realizedPL = BigDecimal.ZERO;
        var totalQty = BigDecimal.ZERO;
        var totalCost = BigDecimal.ZERO;

        for (var transaction : sortedByDate(transactions)) {
            var qty = transaction.getAsset().amount();
            var unitPrice = transaction.getUnitPrice().amount();
            var fee = transaction.getFee() != null ? transaction.getFee().amount() : BigDecimal.ZERO;

            if (qty.signum() > 0) { // Buy
                totalCost = totalCost.add(qty.multiply(unitPrice)).add(fee);
                totalQty = totalQty.add(qty);
            } else { // Sell
                var soldQty = qty.abs();
                var avgCostBeforeSale = totalQty.signum() == 0
                        ? BigDecimal.ZERO
                        : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);

                var pricePL = unitPrice.subtract(avgCostBeforeSale)
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
     * Checks whether the wallet holds enough quantity of an asset to sell.
     *
     * @param asset the asset to check
     * @param qtyToSell the quantity requested for sale
     * @return true if the quantity can be sold
     * @throws IllegalArgumentException if {@code qtyToSell} is not positive
     */
    public boolean canSell(CurrencyUnit asset, BigDecimal qtyToSell) {
        if (qtyToSell.signum() <= 0) {
            throw new IllegalArgumentException("Quantity to sell must be positive.");
        }
        return getPositionForAsset(asset)
            .map(pos -> pos.quantity().compareTo(qtyToSell) >= 0)
            .orElse(false);
    }

    /**
     * Returns a new InvestmentAccount instance with the specified price provider.
     * Useful when loading accounts from JSON, where providers are not restored.
     *
     * @param provider the price provider to attach
     * @return a new {@link InvestmentAccount} with the same data and the new provider
     */
    public InvestmentAccount withProvider(PriceProvider provider) {
        return new InvestmentAccount(
            getName(),
            getBaseCurrency(),
            getHistory(),
            provider
        );
    }
}
