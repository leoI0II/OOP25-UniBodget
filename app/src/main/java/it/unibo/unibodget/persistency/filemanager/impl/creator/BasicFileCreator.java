package it.unibo.unibodget.persistency.filemanager.impl.creator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import it.unibo.unibodget.persistency.filemanager.api.FileCreator;

/**
 * A generic and basic implementation of {@link FileCreator}.
 *
 * Providing the minimal behavior required to ensure that a file
 * exists at the specified path. If the file does not exist, it is created.
 *
 * @param <T> the type associated with the creator
 */
public class BasicFileCreator<T> implements FileCreator {

    /**
     * Ensures that the file at the given path exists.
     * 
     * If the path is {@code null}, an {@link IllegalArgumentException} is thrown
     * If parent directories do not exist, they are created
     * If the file does not exist, it is created
     * If the file already exists, no action is taken
     *
     * @param path the path of the file to open or create
     * @return the same {@link Path} instance provided
     * @throws IOException if the file or its parent directories cannot be created
     */
    @Override
    public Path open(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        return path;
    }

}