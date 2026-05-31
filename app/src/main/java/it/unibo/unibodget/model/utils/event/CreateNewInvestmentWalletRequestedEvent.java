package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.currency.CurrencyUnit;

public record CreateNewInvestmentWalletRequestedEvent(String name, CurrencyUnit currency) implements MessageEvent { }
