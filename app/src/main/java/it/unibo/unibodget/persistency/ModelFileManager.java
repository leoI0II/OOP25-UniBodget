package it.unibo.unibodget.persistency;

import it.unibo.unibodget.persistency.filemanager.api.FileCreator;
import it.unibo.unibodget.persistency.filemanager.impl.creator.FileCreatorFactory;
import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.parser.impl.JsonComplexDataParser;
import it.unibo.unibodget.persistency.parser.impl.JsonComplexDataSerializer;
import it.unibo.unibodget.persistency.util.FilesUtils;
import it.unibo.unibodget.persistency.util.api.Logger;
import it.unibo.unibodget.persistency.util.impl.LoggerImpl;
import it.unibo.unibodget.persistency.writer.WriterFactory;
import it.unibo.unibodget.persistency.writer.api.FileOverwriter;
import it.unibo.unibodget.persistency.writer.api.FileUpdater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import java.util.function.Supplier;

/**
 * High-level lifecycle manager for persisting a single model type {@code T}
 * into a single file. It abstracts away:
 *
 * <ul>
 *     <li>file creation (if missing)</li>
 *     <li>loading (read + JSON parse)</li>
 *     <li>saving (serialize + overwrite)</li>
 *     <li>raw updating (patching the JSON string)</li>
 *     <li>lifecycle enforcement (open → load/save/update → close)</li>
 * </ul>
 *
 * <p>Typical usage with try-with-resources:
 * <pre>{@code
 * try (var mgr = new ModelFileManager<>(Path.of("data/settings.json"), Settings.class).open()) {
 *     Settings settings = mgr.load();
 *     settings.setTheme("dark");
 *     mgr.save(settings);
 * }
 * }</pre>
 *
 * <p>This class guarantees:
 * <ul>
 *     <li>consistent Jackson configuration via {@link JsonComplexDataParser}</li>
 *     <li>safe overwrite/update via {@link FileOverwriter} and {@link FileUpdater}</li>
 *     <li>no accidental operations after {@link #close()}</li>
 * </ul>
 *
 * @param <T> the model type persisted through this manager
 */
public final class ModelFileManager<T> implements AutoCloseable {

    /** Internal lifecycle state. Prevents load/save/update when closed. */
    private enum State { OPEN, CLOSED }

    private final Path path;
    private final JsonComplexDataParser<T> parser;
    private final JsonComplexDataSerializer<T> serializer;
    private final FileOverwriter overwriter;
    private final FileUpdater updater;
    private final Logger logger;

    private State state = State.CLOSED;

    /**
     * Creates a new manager for the given file and model type.
     * Uses a default logger.
     */
    public ModelFileManager(Path path, Class<T> type) {
        this(path, type, new LoggerImpl());
    }

    /**
     * Creates a new manager for the given file and model type.
     *
     * @param path  the file path to manage
     * @param type  the model class to serialize/deserialize
     * @param logger custom logger implementation
     */
    public ModelFileManager(Path path, Class<T> type, Logger logger) {
        this.path = path;
        this.logger = logger;
        this.parser = new JsonComplexDataParser<>(type);
        this.serializer = new JsonComplexDataSerializer<>();

        // Select writer implementations based on file extension (json, txt, etc.)
        String ext = FilesUtils.getFileExtension(path.toString());
        this.overwriter = WriterFactory.createOverwriter(ext);
        this.updater = WriterFactory.createUpdater(ext);
    }

    /**
     * Ensures the file exists (creating it if missing) and marks the manager as OPEN.
     *
     * @return this manager, now ready for load/save/update operations
     */
    public ModelFileManager<T> open() throws IOException {
        FileCreator creator = FileCreatorFactory.create(path.toString());
        creator.open(path); // creates file if missing
        state = State.OPEN;
        logger.info("Opened " + path);
        return this;
    }

    /**
     * Reads the entire file content and parses it into an instance of {@code T}.
     *
     * @throws IllegalStateException if called before {@link #open()}
     */
    public T load() throws IOException, DataParserException {
        requireOpen();
        String raw = Files.readString(path);
        return parser.parse(raw);
    }

    /**
     * Serializes the given model instance and overwrites the entire file.
     *
     * @throws IllegalStateException if called before {@link #open()}
     */
    public void save(T value) throws IOException, DataSerializerException {
        requireOpen();
        String json = serializer.serialize(value);
        overwriter.overwrite(path, json);
        logger.info("Saved " + path);
    }

    /**
     * Applies a raw JSON transformation without re-serializing the model.
     * Useful for small patches (e.g., updating a single field).
     *
     * @param transformation a function that receives the current JSON string
     *                       and returns the new JSON string
     */
    public void update(UnaryOperator<String> transformation) throws IOException {
        requireOpen();
        updater.update(path, transformation);
        logger.info("Updated " + path);
    }

    /**
     * Ensures the manager is OPEN before performing any operation.
     */
    private void requireOpen() {
        if (state != State.OPEN) {
            throw new IllegalStateException("File manager is not open: call open() first.");
        }
    }

    /**
     * Closes the manager. This does not close any OS file handles (none are held),
     * but it enforces the lifecycle: no load/save/update after close.
     */
    @Override
    public void close() {
        state = State.CLOSED;
        logger.info("Closed " + path);
    }

    /**
     * Loads the model from file, or creates + persists a default instance if the file
     * contains only "stub" content:
     * <ul>
     *     <li>empty string</li>
     *     <li>"{}"</li>
     *     <li>"[]"</li>
     * </ul>
     *
     * <p>This is useful for first-run initialization of settings or wallets.
     */
    public T loadOrElse(Supplier<T> defaultSupplier) throws IOException, DataSerializerException {
        requireOpen();
        String raw = Files.readString(path).trim();

        // If file is empty or contains trivial JSON, create default
        if (isStub(raw)) {
            T value = defaultSupplier.get();
            save(value);
            logger.info("No real content in " + path + ", created default.");
            return value;
        }

        // Try parsing normally
        try {
            return parser.parse(raw);
        } catch (DataParserException e) {
            // If parsing fails, fallback to default and overwrite file
            logger.warn("Invalid content in " + path + ", falling back to default: " + e.getMessage());
            T value = defaultSupplier.get();
            save(value);
            return value;
        }
    }

    /**
     * Determines whether the file content is considered "empty" or "stub".
     */
    private boolean isStub(String raw) {
        return raw.isEmpty() || raw.equals("{}") || raw.equals("[]");
    }
}
