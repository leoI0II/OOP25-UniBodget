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

    /**
     * Updates the content of the specified file by applying a transformation function.
     *
     * <p>
     * Subclasses overriding this method should either call
     * {@code super.update(path, updateFunction)} to preserve the default behavior,
     * or explicitly document why the update semantics are being changed.
     * Any override must maintain the contract of throwing an {@link IOException}
     * when the file cannot be written.
     * </p>
     *
     * @param path the file to update
     * @param updateFunction the transformation to apply to the file content
     * @throws IOException if the file does not exist or cannot be written
     */
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
