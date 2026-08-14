package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileOverwriter;

/**
 * Basic implementation of {@link FileOverwriter}.
 * Overwrites the file completely with the provided content.
 */
public class BasicFileOverwriter implements FileOverwriter {

    /**
     * Overwrites the content of the specified file.
     *
     * <p>
     * Subclasses overriding this method should either call
     * {@code super.overwrite(path, content)} to preserve the default behavior,
     * or explicitly document why the overwrite semantics are being changed.
     * Any override must maintain the contract of throwing an {@link IOException}
     * when the file cannot be written.
     * </p>
     *
     * @param path the file to overwrite
     * @param content the new content to write
     * @throws IOException if the file cannot be created or written
     */
    @Override
    public void overwrite(final Path path, final String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
