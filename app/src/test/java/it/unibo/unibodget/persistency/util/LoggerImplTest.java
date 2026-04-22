package it.unibo.unibodget.persistency.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.util.api.Logger;
import it.unibo.unibodget.persistency.util.impl.LoggerImpl;

/**
 * Unit test class for {@link LoggerImpl}.
 * 
 * This test suite verifies that:
 * - informational messages are correctly printed to System.out
 * - warning messages are correctly printed to System.out
 * - error messages are printed to System.out
 * - throwable stack traces are printed to System.err
 * 
 * The tests temporarily redirect System.out and System.err to
 * ByteArrayOutputStreams to capture and inspect the logger output.
 */
class LoggerImplTest {

    private Logger logger;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private PrintStream originalErr;

    /**
     * Redirects System.out and System.err to custom streams
     * so that logger output can be captured and inspected.
     */
    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;

        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));

        logger = new LoggerImpl();
    }

    /**
     * Restores System.out and System.err after each test
     * to avoid side effects on other tests.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Verifies that the logger prints informational messages
     * with the correct [INFO] prefix.
     */
    @Test
    void testInfoLogging() {
        logger.info("Hello info");

        String output = outputStream.toString();
        assertTrue(output.contains("[INFO] Hello info"));
    }

    /**
     * Verifies that the logger prints warning messages
     * with the correct [WARN] prefix.
     */
    @Test
    void testWarnLogging() {
        logger.warn("Warning message");

        String output = outputStream.toString();
        assertTrue(output.contains("[WARN] Warning message"));
    }

    /**
     * Verifies that error messages without a throwable cause
     * are printed to System.out with the correct [ERROR] prefix.
     */
    @Test
    void testErrorLoggingWithoutCause() {
        logger.error("Error occurred", null);

        String output = outputStream.toString();
        assertTrue(output.contains("[ERROR] Error occurred"));
    }

    /**
     * Verifies that error messages with a throwable cause:
     * - print the error message to System.out
     * - print the stack trace to System.err
     */
    @Test
    void testErrorLoggingWithCause() {
        Exception ex = new IllegalArgumentException("Invalid input");

        logger.error("Something failed", ex);

        String out = outputStream.toString();
        String err = errorStream.toString();

        assertTrue(out.contains("[ERROR] Something failed"));
        assertTrue(err.contains("java.lang.IllegalArgumentException"));
        assertTrue(err.contains("Invalid input"));
    }

}
