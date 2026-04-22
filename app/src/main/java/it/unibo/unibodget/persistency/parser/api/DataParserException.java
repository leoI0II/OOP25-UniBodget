package it.unibo.unibodget.persistency.parser.api;

/**
 * Exception thrown when a parsing operation fails due to
 * malformed content or unexpected structure
 * This exception class extends {@link Throwable}
 */
public class DataParserException extends Throwable {

    /**
     * Constructs a new {@code DataParserException} with no detail message
     */
    public DataParserException() {
        super();
    }

    /**
     * Constructs a new {@code DataParserException} with the specified detail message
     *
     * @param message the detail message
     */

    public DataParserException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code DataParserException} with the specified cause
     *
     * @param cause the cause
     *              {@link Throwable#getCause()} method)
     */
    public DataParserException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code DataParserException} with the specified detail message and cause
     *
     * @param message the detail message
     * @param cause   the cause
     *                {@link Throwable#getCause()} method)
     */
    public DataParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
