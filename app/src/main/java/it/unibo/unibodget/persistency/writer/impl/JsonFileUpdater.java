package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

import it.unibo.unibodget.persistency.writer.api.FileUpdater;

/**
 * JSON-specific implementation of {@link FileUpdater}.
 *
 * <p>This class updates the content of an existing JSON file by applying a
 * transformation function to the current content.</p>
 *
 * <p>No JSON validation is performed. The caller is responsible for ensuring
 * that the transformation produces valid JSON.</p>
 */
public class JsonFileUpdater extends BasicFileUpdater {

    /**
     * Updates the JSON file by applying the given transformation function.
     *
     * @param path           the JSON file to update
     * @param updateFunction a function that transforms the old JSON content
     * @throws IOException if the file does not exist or cannot be written
     */
    @Override
    public void update(Path path, UnaryOperator<String> updateFunction) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Cannot update non-existing file: " + path);
        }
        String oldContent = Files.readString(path);
        String newContent = updateFunction.apply(oldContent);
        Files.writeString(path, newContent);
    }
}
