package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

import it.unibo.unibodget.persistency.writer.api.FileUpdater;

/**
 * Basic implementation of {@link FileUpdater}.
 * Reads the file, applies a transformation, and writes the updated content.
 */
public class BasicFileUpdater implements FileUpdater {

    @Override
    public void update(final Path path, final UnaryOperator<String> updateFunction) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Cannot update non-existing file: " + path);
        }
        final String oldContent = Files.readString(path);
        final String newContent = updateFunction.apply(oldContent);
        Files.writeString(path, newContent);
    }
}
