package it.unibo.unibodget.persistency.writer.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Defines an operation for appending content to an existing file.
 * Implementations must ensure that the file is created if it does not exist.
 */
public interface FileAppender {

    /**
     * Appends the given content to the specified file.
     *
     * @param path    the file to append to
     * @param content the content to append
     * @throws IOException if the file cannot be created or written
     */
    void append(Path path, String content) throws IOException;
}
