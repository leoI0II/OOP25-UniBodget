package it.unibo.unibodget.persistency.util.impl;

import it.unibo.unibodget.persistency.util.api.Logger;

/**
 * Default implementation of the {@link Logger} interface.
 * 
 * <p>
 * This implementation logs messages to the standard output, prefixing
 * them with the log level (INFO, WARN, ERROR). For error messages,
 * it also prints the stack trace of the associated throwable cause if provided.
 * 
 * <p>
 * When an error includes a {@code Throwable}, its stack trace is printed
 * to assist debugging
 */
public final class LoggerImpl implements Logger {

    @Override
    public void info(final String message) {
        System.out.println("[INFO] " + message);
    }

    @Override
    public void warn(final String message) {
        System.out.println("[WARN] " + message);
    }

    @Override
    public void error(final String message, final Throwable cause) {
        System.out.println("[ERROR] " + message);
        if (cause != null) {
            cause.printStackTrace();
        }
    }

}
