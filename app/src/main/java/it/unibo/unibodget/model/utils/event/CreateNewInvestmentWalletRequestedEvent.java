package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Event requesting creation of a new investment wallet.
 *
 * @param name wallet name
 * @param currency base currency
 */
public record CreateNewInvestmentWalletRequestedEvent(String name, CurrencyUnit currency)
        implements MessageEvent { }
