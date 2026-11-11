package it.unibo.unibodget.model.utils.event;

/**
 * Event sent when the user asks to replace all the wallets with the demo data inside the jar.
 */
public record LoadDemoDataRequestedEvent() implements MessageEvent { }
