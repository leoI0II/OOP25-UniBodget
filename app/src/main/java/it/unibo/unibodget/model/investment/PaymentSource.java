package it.unibo.unibodget.model.investment;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

/**
 * Sealed interface representing the source of funds for an investment order.
 *
 * <p>Three variants are permitted:
 * <ul>
 *   <li>{@link CashAccountChannel} — funds come from a fiat {@link CashAccount}.</li>
 *   <li>{@link StableCoinPositionChannel} — funds come from a stablecoin position
 *       held inside an {@link InvestmentAccount}.</li>
 *   <li>{@link NoPaymentChannel} — no payment source is selected (the user left
 *       the source checkbox unchecked).</li>
 * </ul>
 * Use exhaustive {@code switch} or {@code instanceof} pattern matching to handle all variants.</p>
 */
public sealed interface PaymentSource
    permits PaymentSource.CashAccountChannel,
            PaymentSource.StableCoinPositionChannel,
            PaymentSource.NoPaymentChannel {

    /**
     * Payment source backed by a fiat {@link CashAccount}.
     *
     * @param account the cash account from which funds will be debited
     */
    record CashAccountChannel(CashAccount account) implements PaymentSource { }

    /**
     * Payment source backed by a stablecoin position inside an {@link InvestmentAccount}.
     *
     * @param account     the investment account holding the stablecoin
     * @param stableCoin  the stablecoin currency used as payment
     */
    record StableCoinPositionChannel(InvestmentAccount account, CurrencyUnit stableCoin) implements PaymentSource { }

    /**
     * Represents the absence of a payment source (user did not select one).
     */
    record NoPaymentChannel() implements PaymentSource { }
}
