package it.unibo.unibodget.persistency.filemanager.api;

/**
 * Exception thrown when a initialization operation fails 
 * due to issues such as file creation failure,
 * This exception class extends {@link Throwable}
 */
public class FileInitializationException extends Throwable {

    /**
     * Constructs a new {@code FileInitializationException} with no detail message
     */
    public FileInitializationException() {
        super();
    }

    /**
     * Constructs a new {@code FileInitializationException} with the specified detail message
     *
     * @param message the detail message
     */
    public FileInitializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code FileInitializationException} with the specified cause
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method)
     */
    public FileInitializationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code FileInitializationException} with the specified detail message and cause
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method)
     */
    public FileInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
