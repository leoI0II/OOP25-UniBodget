package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParser;

public final class ParserFactory {

    private ParserFactory() {}

    /*
    public static DataParser<?> create(String type) {
        return switch (type.toLowerCase()) {
            case "category" -> new CategoryParser();
            case "currency" -> new CurrencyParser();
            case "transaction" -> new TransactionDataParser();
            case "userdata" -> new UserDataParser();
            default -> throw new IllegalArgumentException("Unknown parser type: " + type);
        };
    }
    */
}
