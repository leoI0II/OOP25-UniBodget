package it.unibo.unibodget.persistency.parser.api;

/**
 * Exception thrown when a serialization operation fails due to
 * an invalid object state or an unexpected structure that cannot
 * be converted into a file representation.
 * 
 * This exception class extends {@link Throwable}
 */
public class DataSerializerException extends Throwable {

    /**
     * Constructs a new {@code DataSerializerException} with no detail message.
     */
    public DataSerializerException() {
        super();
    }

    /**
     * Constructs a new {@code DataSerializerException} with the specified detail message.
     *
     * @param message the detail message
     */
    public DataSerializerException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code DataSerializerException} with the specified cause.
     *
     * @param cause the cause
     */
    public DataSerializerException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code DataSerializerException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public DataSerializerException(String message, Throwable cause) {
        super(message, cause);
    }

}
