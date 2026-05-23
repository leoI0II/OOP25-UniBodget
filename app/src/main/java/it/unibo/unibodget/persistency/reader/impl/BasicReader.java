package it.unibo.unibodget.persistency.reader.impl;

import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.reader.api.FileReader;

/**
 * A base abstract class for FileReader implementations.
 *
 * @param <T> the type returned by the reader
 */
public abstract class BasicReader<T> implements FileReader<T> {
    private final String path;

    /**
     * Assigning the path of the file to be read
     * If the path is null, blank, does not exist, is not a regular file,
     * or cannot be read, an {@link IllegalArgumentException} is thrown
     * 
     * @param path the path of the file to be read
     * @throws IllegalArgumentException if the path is invalid or the file is not accessible
     */
    protected BasicReader(final String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or blank");
        }
        this.path = path;
        validatePath();
    }

    /** 
     * Returns the validated path of the file associated with this reader
     *
     * @return the path of the file
     */
    protected String getPath() {
        return this.path;
    }

    /**
     * Validates the provided file path
     * Checks if the file exists, is a regular file, and is readable.
     * 
     * If any of these conditions are not met, an {@link IllegalArgumentException} 
     * is thrown with an appropriate message
     * 
     * @param path the file path to validate
     * @throws IllegalArgumentException if the file does 
     *             not match conditions for existence, regularity, or readability
     */
    protected void validatePath() {
        final Path path_to_check = Path.of(path);
        if (!Files.exists(path_to_check)) {
            throw new IllegalArgumentException("File does not exist: " + path);
        }
        if (!Files.isRegularFile(path_to_check)) {
            throw new IllegalArgumentException("Path is not a file: " + path);
        }
        if (!Files.isReadable(path_to_check)) {
            throw new IllegalArgumentException("File is not readable: " + path);
        }
    }
}