package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.writer.api.FileAppender;
import it.unibo.unibodget.persistency.writer.api.FileOverwriter;
import it.unibo.unibodget.persistency.writer.api.FileSaver;
import it.unibo.unibodget.persistency.writer.api.FileUpdater;

import it.unibo.unibodget.persistency.writer.impl.JsonFileAppender;
import it.unibo.unibodget.persistency.writer.impl.JsonFileOverwriter;
import it.unibo.unibodget.persistency.writer.impl.JsonFileSaver;
import it.unibo.unibodget.persistency.writer.impl.JsonFileUpdater;

/**
 * Unit tests for {@link WriterFactory}.
 *
 * <p>These tests verify that the factory correctly instantiates the expected
 * writer implementations for supported types and throws exceptions for
 * unsupported ones.</p>
 */
class WriterFactoryTest {

    /**
     * Ensures that requesting a JSON appender returns a {@link JsonFileAppender}.
     */
    @Test
    void testCreateJsonAppender() {
        FileAppender appender = WriterFactory.createAppender("json");
        assertTrue(appender instanceof JsonFileAppender);
    }

    /**
     * Ensures that requesting a JSON overwriter returns a {@link JsonFileOverwriter}.
     */
    @Test
    void testCreateJsonOverwriter() {
        FileOverwriter overwriter = WriterFactory.createOverwriter("json");
        assertTrue(overwriter instanceof JsonFileOverwriter);
    }

    /**
     * Ensures that requesting a JSON saver returns a {@link JsonFileSaver}.
     */
    @Test
    void testCreateJsonSaver() {
        FileSaver saver = WriterFactory.createSaver("json");
        assertTrue(saver instanceof JsonFileSaver);
    }

    /**
     * Ensures that requesting a JSON updater returns a {@link JsonFileUpdater}.
     */
    @Test
    void testCreateJsonUpdater() {
        FileUpdater updater = WriterFactory.createUpdater("json");
        assertTrue(updater instanceof JsonFileUpdater);
    }

    /**
     * Ensures that unsupported types cause an {@link IllegalArgumentException}.
     */
    @Test
    void testUnsupportedTypesThrowException() {
        assertThrows(IllegalArgumentException.class, () -> WriterFactory.createAppender("xml"));
        assertThrows(IllegalArgumentException.class, () -> WriterFactory.createOverwriter("txt"));
        assertThrows(IllegalArgumentException.class, () -> WriterFactory.createSaver("yaml"));
        assertThrows(IllegalArgumentException.class, () -> WriterFactory.createUpdater("csv"));
    }
}
