package it.unibo.unibodget.persistency.parser.api;

/**
 * Defines the contract for parsing data into a specific type
 * @param <T> the type of the parsed data
 */
public interface DataSerializer<T> {

    /**
     * Serializes the given object into its textual representation
     * returning a {@code String} 
     * The returned string should contain a complete and valid
     * representation of the object 
     *
     * @param data the object to serialize
     * @return the textual representation of the object
     * @throws DataSerializerException if the object cannot be serialized
     */
    String serialize(T data) throws DataSerializerException;
}