package it.unibo.unibodget.persistency.filemanager.impl.opener;

import it.unibo.unibodget.persistency.filemanager.api.FileOpener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Safe implementation of {@link FileOpener} that guarantees the availability
 * and accessibility of the target file before returning its {@link Path}.
 * <p>
 * This opener ensures:
 * <ul>
 *     <li>the parent directory exists (and creates it if necessary)</li>
 *     <li>the file exists (and creates it if missing)</li>
 *     <li>the file is both readable and writable</li>
 * </ul>
 * <p>
 * It is intended for components that require a guaranteed, ready-to-use file
 * without having to manually check or create filesystem structures.
 */
public class SafeFileOpener implements FileOpener {

    /**
     * Ensures that the file at the given path exists and is accessible.
     * <p>
     * Behavior:
     * <ul>
     *     <li>If {@code path} is {@code null}, an {@link IllegalArgumentException} is thrown.</li>
     *     <li>If the parent directory does not exist, it is created recursively.</li>
     *     <li>If the file does not exist, it is created as an empty file.</li>
     *     <li>If the file exists but is not readable or writable, an {@link IOException} is thrown.</li>
     * </ul>
     *
     * @param path the filesystem path of the file to open; must not be {@code null}
     * @return the validated and guaranteed-to-exist {@link Path}
     *
     * @throws IllegalArgumentException if {@code path} is {@code null}
     * @throws IOException if the file cannot be created or is not accessible
     */
    @Override
    public Path open(final Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Il path fornito non può essere nullo.");
        }

        // Ensure parent directory exists
        final Path parentDir = path.getParent();
        if (parentDir != null && Files.notExists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Create file if missing
        if (Files.notExists(path)) {
            Files.createFile(path);
        }

        // Validate accessibility
        if (!Files.isReadable(path) || !Files.isWritable(path)) {
            throw new IOException("File non accessibile (permessi insufficienti): " + path);
        }

        return path;
    }
}
