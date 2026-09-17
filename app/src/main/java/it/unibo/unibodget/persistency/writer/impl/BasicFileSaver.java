package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileSaver;

/**
 * Basic implementation of {@link FileSaver}.
 * Saves content only if the file does not already exist.
 */
public class BasicFileSaver implements FileSaver {

    /**
     * Saves the given content to the specified path if the file does not already exist.
     *
     * <p>
     * Subclasses overriding this method must call {@code super.save(path, content)}
     * unless they intentionally replace the entire saving behavior. Any override
     * should preserve the contract of throwing an {@link IOException} when the
     * file cannot be written.
     * </p>
     *
     * @param path    the file path to write to
     * @param content the content to save
     * @throws IOException if the file already exists or writing fails
     */
    @Override
    public void save(final Path path, final String content) throws IOException {
        if (Files.exists(path)) {
            throw new IOException("File already exists: " + path);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
