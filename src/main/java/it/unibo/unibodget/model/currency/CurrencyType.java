package it.unibo.unibodget.model.currency;

/**
 * Enumerates the supported currency categories. This type is used to classify currencies loaded
 * from JSON
 */
public enum CurrencyType {
    /** Government-issued fiat currency. */
    FIAT,
    /** Cryptocurrency or digital token. */
    CRYPTO,
    /** Equity or stock ticker. */
    STOCK,
    /** Custom or JSON-defined currency. */
    CUSTOM
}
