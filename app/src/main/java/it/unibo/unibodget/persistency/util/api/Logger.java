package it.unibo.unibodget.persistency.util.api;

/**
 * Simple logging abstraction used to report warnings, errors,
 * or diagnostic information within the persistence layer.
 */
public interface Logger {
    
    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs a warning message, indicating a potential 
     * issue that does not prevent the application from 
     * functioning but may require attention.
     * @param message the message to log
     */
    void warn(String message);

    /**
     * Logs an error message with a throwable cause
     * @param message the message to log
     * @param cause the throwable cause
     */
    void error(String message, Throwable cause);
}
