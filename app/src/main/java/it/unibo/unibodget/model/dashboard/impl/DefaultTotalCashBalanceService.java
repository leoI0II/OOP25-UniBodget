package it.unibo.unibodget.model.dashboard.impl;

import java.util.Objects;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Computes the total balance across all cash wallets in a desired currency.
 *
 * <p>Each cash wallet balance is converted into the requested target currency
 * and then summed to produce a single aggregated total.</p>
 */
public final class DefaultTotalCashBalanceService {

    private final CashAccountService walletService;
    private final CashBalanceConverter balanceConverter;

    /**
     * Creates the service with the required dependencies.
     *
     * @param walletService
     *            the service exposing the available cash wallets
     * @param balanceConverter
     *            the component used to convert balances between currencies
     */
    public DefaultTotalCashBalanceService(
            final CashAccountService walletService,
            final CashBalanceConverter balanceConverter) {
        this.walletService = Objects.requireNonNull(walletService);
        this.balanceConverter = Objects.requireNonNull(balanceConverter);
    }

    /**
     * Returns the total balance of all cash wallets expressed in the target currency.
     *
     * @param targetCurrency
     *            the currency in which the total must be expressed
     * @return the aggregated total balance in the target currency
     */
    public Asset getTotalBalanceIn(final CurrencyUnit targetCurrency) {
        Objects.requireNonNull(targetCurrency);

        return walletService.getWallets().stream()
                .map(CashAccount::getBalance)
                .map(balance -> convertIfNecessary(balance, targetCurrency))
                .reduce(Asset.zero(targetCurrency), Asset::add);
    }

    private Asset convertIfNecessary(final Asset balance, final CurrencyUnit targetCurrency) {
        if (balance.currency().equals(targetCurrency)) {
            return balance;
        }
        return balanceConverter.convert(balance, targetCurrency);
    }
}