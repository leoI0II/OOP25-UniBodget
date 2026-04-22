package it.unibo.unibodget.persistency.filemanager.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * An interface for creating new files in a controlled and safe manner.
 *
 * Allows different creation strategies, such as creating empty files, 
 * initializing them with default content, or
 * generating structured templates (example JSON).
 */
public interface FileCreator {

    /**
     * Creates a new file at the specified path.
     * If the file already exists skip creation
     *
     * @param path the path where the file should be created
     * @return the created {@link Path} instance
     * @throws IOException if the file cannot be created
     */
    Path open(Path path) throws IOException;
}