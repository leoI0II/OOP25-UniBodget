package it.unibo.unibodget.persistency.util.impl;

import it.unibo.unibodget.persistency.util.api.Logger;

/**
 * Default implementation of the {@link Logger} interface.
 * This implementation logs messages to the standard output, prefixing
 * them with the log level (INFO, WARN, ERROR). For error messages,
 * it also prints the stack trace of the associated throwable cause if provided.
 * 
 * When an error includes a {@code Throwable}, its stack trace is printed
 * to assist debugging
 */
public class LoggerImpl implements Logger {

    @Override
    public void info(String message) {
        System.out.println("[INFO] " + message);
    }

    @Override
    public void warn(String message) {
        System.out.println("[WARN] " + message);
    }

    @Override
    public void error(String message, Throwable cause) {
        System.out.println("[ERROR] " + message);
        if (cause != null) {
            cause.printStackTrace();
        }
    }
    
}
