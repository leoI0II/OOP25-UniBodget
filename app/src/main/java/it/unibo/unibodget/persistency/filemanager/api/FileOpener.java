package it.unibo.unibodget.persistency.filemanager.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * An interface for safely opening a file, also depending on its extension
 * and ensuring that it exists
 * and is accessible before any read or write operation is performed.
 *
 * Allows different strategies for file validation,
 * such as creation, overwrite existing corrupted/invalid files
 */
public interface FileOpener {
    /**
     * Ensures that the file at the given path exists and is accessible.
     * Implementations may create the file if it does not exist or throw
     * an exception depending on the chosen strategy.
     *
     * @param path the path of the file to be opened or validated
     * @return the validated {@link Path} instance
     * @throws IOException if the file cannot be accessed or created
     */
    Path open(Path path) throws IOException;
}