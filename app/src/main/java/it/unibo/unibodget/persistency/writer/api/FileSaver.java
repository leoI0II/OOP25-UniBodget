package it.unibo.unibodget.persistency.writer.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Defines an operation for saving content to a new file.
 * Implementations must fail if the file already exists.
 */
public interface FileSaver {

    /**
     * Saves the given content to the specified file.
     * The operation must fail if the file already exists.
     *
     * @param path    the file to create
     * @param content the content to write
     * @throws IOException if the file already exists or cannot be written
     */
    void save(Path path, String content) throws IOException;
}
