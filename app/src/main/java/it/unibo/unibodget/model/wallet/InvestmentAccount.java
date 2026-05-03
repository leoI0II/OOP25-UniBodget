package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unibo.unibodget.model.converter.provider.PriceProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * A wallet that holds {@link InvestmentTransaction} entries (stocks, crypto, funds).
 *
 * <p>The balance depends on current market prices and is not implemented yet:
 * it will be computed as Σ(quantity × current market price − fees) for each held asset.
 */
public class InvestmentAccount extends Wallet<InvestmentTransaction> {

    private final PriceProvider priceProvider;

    /**
     * Creates an InvestmentAccount with an existing transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     * @param history      the pre-existing transaction ledger
     */
    public InvestmentAccount(
        String name, 
        CurrencyUnit baseCurrency, 
        Historical<InvestmentTransaction> history,
        PriceProvider priceProvider) {
        super(name, baseCurrency, history, "Investment Account");
        this.priceProvider = priceProvider;
    }

    /**
     * Creates an InvestmentAccount with an empty transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     * @param priceProvider the provider for fetching market prices
     */
    public InvestmentAccount(String name, CurrencyUnit baseCurrency, PriceProvider priceProvider) {
        this(name, baseCurrency, new Historical<>(), priceProvider);
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

    public Optional<Position> getPositionForAsset(CurrencyUnit asset) {
        return getPositions().stream()
                .filter(p -> p.asset().equals(asset))
                .findFirst();
    }

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
            }
            else { // Sell
                var soldQty = qty.abs();
                var avgCostBeforeSale = totalQty.signum() == 0
                        ? BigDecimal.ZERO
                        : totalCost.divide(totalQty, 10, RoundingMode.HALF_UP);
                totalCost = totalCost.subtract(avgCostBeforeSale.multiply(soldQty));
                totalQty = totalQty.add(qty); // qty is negative for sells
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
            currentMarketValue);
    }

    public List<InvestmentTransaction> sortedByDate(List<InvestmentTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(InvestmentTransaction::getDate))
                .toList();
    }

}
