package it.unibo.unibodget.persistency.writer.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

/**
 * Defines an operation that updates the content of an existing file
 * by applying a transformation function.
 */
public interface FileUpdater {

    /**
     * Updates the content of the specified file by applying the given function.
     *
     * @param path           the file to update
     * @param updateFunction a function transforming the old content into new content
     * @throws IOException if the file does not exist or cannot be written
     */
    void update(Path path, UnaryOperator<String> updateFunction) throws IOException;
}
