package it.unibo.unibodget.persistency.parser.api;

/**
 * Defines the contract for parsing data into a specific type
 * @param <T> the type of the parsed data
 * 
 * A generic interface for parsing raw textual content and converting it
 * into an object of type {@code T}.
 * 
 * @param <T> the type of object produced by the parsing process
 */
public interface DataParser<T> {

    /**
     * Parses the given textual content and converts it into an object
     * of type {@code T}.
     *
     * @param data the raw textual content to be parsed
     * @return the parsed object of type {@code T}
     * @throws DataParserException if the content is malformed
     */
    T parse(String data) throws DataParserException;
}