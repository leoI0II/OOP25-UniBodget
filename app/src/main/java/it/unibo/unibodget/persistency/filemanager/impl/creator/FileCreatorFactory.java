package it.unibo.unibodget.persistency.filemanager.impl.creator;

import it.unibo.unibodget.persistency.filemanager.api.FileCreator;
import it.unibo.unibodget.persistency.util.FilesUtils;

/**
 * Factory class responsible for creating the appropriate {@link FileCreator}
 * implementation based on the file extension of the provided path.
 */
public class FileCreatorFactory{

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class containing only static methods.
     */
    private FileCreatorFactory() {
        // utility class
    }

    /**
     * Creates a {@link FileCreator} capable of creating the file located at the given path.
     *
     * @param path the path of the file to create
     * @return a {@link FileCreator} implementation suitable for the file type
     * @throws IllegalArgumentException if the file extension is missing or unsupported
     */
    public static FileCreator create(final String path) {
        return switch (FilesUtils.getFileExtension(path)) {
            case "json" -> new JsonFileCreator();
            default -> throw new IllegalArgumentException("Unsupported file type: " + FilesUtils.getFileExtension(path));
        };
    }
}