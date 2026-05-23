package it.unibo.unibodget.persistency.writer;

import it.unibo.unibodget.persistency.writer.api.FileAppender;
import it.unibo.unibodget.persistency.writer.api.FileOverwriter;
import it.unibo.unibodget.persistency.writer.api.FileSaver;
import it.unibo.unibodget.persistency.writer.api.FileUpdater;

import it.unibo.unibodget.persistency.writer.impl.JsonFileAppender;
import it.unibo.unibodget.persistency.writer.impl.JsonFileOverwriter;
import it.unibo.unibodget.persistency.writer.impl.JsonFileSaver;
import it.unibo.unibodget.persistency.writer.impl.JsonFileUpdater;

/**
 * Factory class responsible for creating writer components used to modify or
 * persist data into files. This class centralizes the instantiation logic for
 * all writer types (appenders, overwriters, savers, updaters).
 * 
 * Attempting to request a writer for an unsupported type will result 
 * in an {@link IllegalArgumentException}.
 */
public final class WriterFactory {

    private WriterFactory() {
        // prevent instantiation: utility class
    }

    /**
     * Creates a {@link FileAppender} capable of appending content to a file
     * of the specified type.
     *
     * @param type the file format
     * @return a {@link FileAppender} implementation suitable for the given type
     * @throws IllegalArgumentException if the type is unsupported
     */
    public static FileAppender createAppender(String type) {
        return switch (type.toLowerCase()) {
            case "json" -> new JsonFileAppender();
            default -> throw new IllegalArgumentException("Unsupported appender type: " + type);
        };
    }

    /**
     * Creates a {@link FileOverwriter} capable of replacing the entire content
     * of a file of the specified type.
     *
     * @param type the file format
     * @return a {@link FileOverwriter} implementation suitable for the given type
     * @throws IllegalArgumentException if the type is unsupported
     */
    public static FileOverwriter createOverwriter(String type) {
        return switch (type.toLowerCase()) {
            case "json" -> new JsonFileOverwriter();
            default -> throw new IllegalArgumentException("Unsupported overwriter type: " + type);
        };
    }

    /**
     * Creates a {@link FileSaver} responsible for saving new content to a file
     * of the specified type. Used when creating or exporting new data structures.
     *
     * @param type the file format
     * @return a {@link FileSaver} implementation suitable for the given type
     * @throws IllegalArgumentException if the type is unsupported
     */
    public static FileSaver createSaver(String type) {
        return switch (type.toLowerCase()) {
            case "json" -> new JsonFileSaver();
            default -> throw new IllegalArgumentException("Unsupported saver type: " + type);
        };
    }

    /**
     * Creates a {@link FileUpdater} capable of modifying existing content inside
     * a file of the specified type. This is used when partial updates are needed
     * without rewriting the entire file.
     *
     * @param type the file format
     * @return a {@link FileUpdater} implementation suitable for the given type
     * @throws IllegalArgumentException if the type is unsupported
     */
    public static FileUpdater createUpdater(String type) {
        return switch (type.toLowerCase()) {
            case "json" -> new JsonFileUpdater();
            default -> throw new IllegalArgumentException("Unsupported updater type: " + type);
        };
    }
}
