package it.unibo.unibodget.persistency.writer.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Defines an operation that replaces the entire content of a file.
 */
public interface FileOverwriter {

    /**
     * Overwrites the specified file with the given content.
     *
     * @param path    the file to overwrite
     * @param content the new content to write
     * @throws IOException if the file cannot be written
     */
    void overwrite(Path path, String content) throws IOException;
}
