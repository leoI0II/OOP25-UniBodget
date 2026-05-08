package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileSaver;

/**
 * JSON-specific implementation of {@link FileSaver}.
 *
 * <p>This class saves JSON content to a new file. If the file already exists,
 * the operation fails.</p>
 *
 * <p>No JSON validation is performed. The caller is responsible for providing
 * valid JSON content.</p>
 */
public class JsonFileSaver extends BasicFileSaver {

    /**
     * Saves the given JSON content to a new file.
     *
     * @param path    the JSON file to create
     * @param content the JSON content to write
     * @throws IOException if the file already exists or cannot be written
     */
    @Override
    public void save(Path path, String content) throws IOException {
        if (Files.exists(path)) {
            throw new IOException("File already exists: " + path);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
