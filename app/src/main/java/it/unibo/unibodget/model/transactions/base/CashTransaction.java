package it.unibo.unibodget.model.transactions.base;

import java.time.LocalDate;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;

public final class CashTransaction extends Transaction {

    /**
     * Creates a new CashTransaction with the given asset, category, date, description and notes.
     *
     * @param asset        the monetary value associated with the transaction
     *                     must not be null
     * @param category     the category describing the nature of the transaction
     *                     must not be null
     * @param date         the date on which the transaction occurred;
     *                     must not be null
     * @param description  a short human‑readable description of the transaction
     *                     may be null
     * @param notes        optional additional notes or comments; may be null
     */
    public CashTransaction(Asset asset, Category category, LocalDate date, String description, String notes) {
        super(asset, category, date, description, notes);
    }
    
}
