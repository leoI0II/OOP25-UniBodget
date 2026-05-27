package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.transactions.base.Transaction;
import it.unibo.unibodget.model.wallet.Wallet;

public record TransactionAddedEvent(Wallet wallet, Transaction transaction) implements MessageEvent {}
