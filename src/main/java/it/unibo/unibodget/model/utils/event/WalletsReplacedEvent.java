package it.unibo.unibodget.model.utils.event;

/**
 * Event sent after all the wallets have been replaced at once (demo data loaded or all data
 * cleared), so the pages can reload everything instead of updating a single wallet.
 */
public record WalletsReplacedEvent() implements MessageEvent { }
