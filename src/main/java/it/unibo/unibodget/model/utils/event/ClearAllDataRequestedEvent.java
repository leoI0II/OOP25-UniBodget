package it.unibo.unibodget.model.utils.event;

/**
 * Event sent when the user asks to delete all the wallets (cash and investment) and their P/L
 * snapshots, to start again with an empty application.
 */
public record ClearAllDataRequestedEvent() implements MessageEvent { }
