package it.unibo.unibodget.model.investment;

/** Enum representing the type of investment order. */
public enum OrderType {
    /** Purchase order for an asset. */
    BUY,
    /** Sale order for an asset. */
    SELL,
    /** Transfer of holdings between accounts. */
    TRANSFER
}
