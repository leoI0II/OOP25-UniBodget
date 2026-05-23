package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileAppender;

/**
 * Base class for file appenders providing common safety utilities.
 *
 * Appending to a file requires the target file to exist. This class ensures
 * that the file and its parent directories are created if missing, so that
 * concrete appenders can focus only on writing
 */
public abstract class BasicFileAppender implements FileAppender {

    /**
     * Ensures that the target file exists before performing an append operation.
     * 
     * If the file does not exist, all missing parent directories are created
     * and an empty file is generated, preventing {@link IOException} errors
     * 
     * @param path the file path to check or create
     * @throws IOException if the file or directories cannot be created
     */
    protected void ensureFileExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
    }
}
