package it.unibo.unibodget.model.currency;

/**
 * Enumerates the supported currency categories.
 *
 * This type is used to classify currencies loaded from JSON
 */
public enum CurrencyType {
    FIAT,
    CRYPTO,
    STOCK,
    CUSTOM;
}
