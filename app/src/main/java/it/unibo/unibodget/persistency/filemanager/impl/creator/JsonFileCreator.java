package it.unibo.unibodget.persistency.filemanager.impl.creator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link BasicFileCreator} specialization for JSON files.
 * 
 * This creator ensures that the file exists and, if newly created,
 * initializes it with an empty JSON object {}.
 */
public class JsonFileCreator extends BasicFileCreator<String> {
    
    /**
     * Creates a new JsonFileCreator.
     * No additional state is required.
     */
    public JsonFileCreator() {
        // no state needed
    }

    /**
     * Ensures that the JSON file exists and initializes it with "{}" if it was newly created.
     *
     * @param path the path of the JSON file to open or create
     * @return the created or existing {@link Path}
     * @throws IOException if the file cannot be created or written
     */
    @Override
    public Path open(Path path) throws IOException {
        boolean existed = Files.exists(path);
        Path created = super.open(path);

        if (!existed) {
            Files.writeString(created, "{}");
        }

        return created;
    }
}