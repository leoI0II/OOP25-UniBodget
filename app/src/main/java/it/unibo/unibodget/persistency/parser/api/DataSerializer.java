package it.unibo.unibodget.persistency.parser.api;

/**
 * Defines the contract for parsing data into a specific type
 * @param <T> the type of the parsed data
 */
public interface DataParser<T> {
    T parse(String data) throws DataParserException;
}