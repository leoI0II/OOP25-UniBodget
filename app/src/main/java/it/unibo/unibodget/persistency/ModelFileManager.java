package it.unibo.unibodget.persistency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Manages the lifecycle of reading and writing a single model type {@code T}
 * to a JSON file. This class abstracts file creation, loading, saving,
 * and updating, ensuring consistent JSON handling through Jackson.
 *
 * <p>Typical usage:</p>
 * <pre>
 * try (ModelFileManager<Settings> mgr =
 *         new ModelFileManager<>(path, resource, Settings.class)) {
 *     mgr.open();
 *     Settings s = mgr.loadObject();
 *     mgr.saveObject(s);
 * }
 * </pre>
 *
 * @param <T> the model type to persist
 */
public final class ModelFileManager<T> implements AutoCloseable {

    private final Path path;
    private final String resourcePath;
    private final Class<T> type;

    private final String objectKey;
    private final String listKey;

    private enum State { OPEN, CLOSED }
 
    private State state = State.CLOSED;

    private final ObjectMapper mapper = PersistenceJacksonConfig.mapper();

    /**
     * Creates a manager for the given file and model type.
     *
     * @param path         the file path where JSON is stored
     * @param resourcePath fallback resource used if the file is missing or invalid
     * @param type         the model class to serialize/deserialize
     */
    public ModelFileManager(final Path path, final String resourcePath, final Class<T> type) {
        this(path, resourcePath, type, defaultKey(type), null);
    }

    /**
     * Creates a manager with a custom JSON object key.
     *
     * @param path         the file path
     * @param resourcePath fallback resource
     * @param type         the model class
     * @param objectKey    the JSON key used to store the object
     */
    public ModelFileManager(final Path path, final String resourcePath, 
                            final Class<T> type, final String objectKey) {
        this(path, resourcePath, type, objectKey, null);
    }

    /**
     * Creates a manager with custom object and list keys.
     *
     * @param path         the file path
     * @param resourcePath fallback resource
     * @param type         the model class
     * @param objectKey    JSON key for the object
     * @param listKey      JSON key for a list of objects
     */
    public ModelFileManager(final Path path, final String resourcePath, final Class<T> type, 
                            final String objectKey, final String listKey) {
        this.path = path;
        this.resourcePath = resourcePath;
        this.type = type;
        this.objectKey = objectKey;
        this.listKey = listKey;
    }

    /**
     * Opens the manager and ensures the file exists and contains valid JSON.
     *
     * @throws IOException if the file cannot be created or restored
     */
    public void open() throws IOException {
        ensureFileExistsOrRestore();
        state = State.OPEN;
    }

    /**
     * Ensures the JSON file exists and contains valid JSON.
     * If the file is missing, empty, or invalid, it is restored
     * from the bundled resource.
     */
    private void ensureFileExistsOrRestore() throws IOException {
        if (!Files.exists(path)) {
            restoreFromResources();
            return;
        }

        try {
            final String raw = Files.readString(path).trim();
            if (raw.isEmpty()) {
                restoreFromResources();
                return;
            }
            new ObjectMapper().readTree(raw); // validate JSON
        } catch (Exception e) {
            restoreFromResources();
        }
    }

    /**
     * Generates a default JSON key based on the class name.
     * Example: Settings → "settings".
     */
    private static String defaultKey(final Class<?> type) {
        final String simple = type.getSimpleName();
        return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }

    /**
     * Restores the JSON file from the bundled resource.
     *
     * @throws IOException if the resource cannot be read
     */
    private void restoreFromResources() throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        Files.createDirectories(path.getParent());
        Files.writeString(path, new String(is.readAllBytes()));
    }

    /**
     * Loads the entire JSON file as a {@link JsonNode}.
     *
     * @return the parsed JSON tree
     * @throws IOException if the file cannot be read
     */
    public JsonNode loadJson() throws IOException {
        requireOpen();
        String raw = Files.readString(path);
        return new ObjectMapper().readTree(raw);
    }

    /**
     * Loads a list of objects stored under the given JSON key.
     *
     * @param key the JSON array key
     * @return the list of deserialized objects
     * @throws IOException if the key is missing or invalid
     */
    public List<T> loadList(final String key) throws IOException {
        final JsonNode root = loadJson();
        final JsonNode arr = root.get(key);

        if (arr == null || !arr.isArray()) {
            throw new IOException("Key '" + key + "' not found or not an array");
        }

        return mapper.convertValue(
            arr,
            mapper.getTypeFactory().constructCollectionType(List.class, type)
        );
    }

    /**
     * Ensures the manager is in OPEN state.
     *
     * @throws IllegalStateException if called after close()
     */
    private void requireOpen() {
        if (state != State.OPEN) {
            throw new IllegalStateException("File manager is not open.");
        }
    }

    /**
     * Closes the manager and prevents further operations.
     */
    @Override
    public void close() {
        state = State.CLOSED;
    }

    /**
     * Saves a list of objects under the given JSON key.
     * The rest of the JSON file is preserved.
     *
     * @param key  the JSON array key
     * @param list the list to save
     * @throws IOException if writing fails
     */
    public void saveList(final String key, final List<T> list) throws IOException {
        requireOpen();

        final JsonNode rootNode;
        if (Files.exists(path)) {
            final String raw = Files.readString(path);
            rootNode = raw == null || raw.trim().isEmpty()
                    ? mapper.createObjectNode()
                    : mapper.readTree(raw);
        } else {
            rootNode = mapper.createObjectNode();
        }

        final ObjectNode root = (ObjectNode) rootNode;
        final ArrayNode array = mapper.valueToTree(list);
        root.set(key, array);

        final String updated = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        Files.writeString(path, updated);
    }

    /**
     * Saves a single object under the configured JSON key.
     *
     * @param obj the object to save
     * @throws IOException if writing fails
     */
    public void saveObject(final T obj) throws IOException {
        requireOpen();
        final ObjectNode root = readCurrentRootOrEmpty();
        root.set(objectKey, mapper.valueToTree(obj));
        mapper.writerWithDefaultPrettyPrinter()
            .writeValue(path.toFile(), root);
    }

    /**
     * Loads a single object from the configured JSON key.
     *
     * @return the deserialized object, or null if missing
     * @throws IOException if reading fails
     */
    public T loadObject() throws IOException {
        requireOpen();
        final JsonNode root = mapper.readTree(path.toFile());
        final JsonNode node = root.get(objectKey);
        if (node == null || node.isNull()) {
            return null;
        }
        return mapper.treeToValue(node, type);
    }

    /**
     * Reads the current JSON root or returns an empty object node.
     *
     * @return a valid {@link ObjectNode}
     * @throws IOException if the file cannot be read
     */
    private ObjectNode readCurrentRootOrEmpty() throws IOException {
        if (!Files.exists(path)) {
            return mapper.createObjectNode();
        }
        final String raw = Files.readString(path);
        if (raw.trim().isEmpty()) {
            return mapper.createObjectNode();
        }
        final JsonNode node = mapper.readTree(raw);
        return node instanceof ObjectNode ? (ObjectNode) node : mapper.createObjectNode();
    }

    /**
     * Loads the list of objects stored under the configured list key.
     *
     * @return the list of deserialized objects
     * @throws IOException if the configured list key is missing/invalid
     * @throws IllegalStateException if no list key was configured for this manager
     */
    public List<T> loadList() throws IOException {
        requireListKey();
        return loadList(listKey);
    }

    /**
     * Saves a list of objects under the configured list key.
     *
     * @param list the list to save
     * @throws IOException if writing fails
     * @throws IllegalStateException if no list key was configured for this manager
     */
    public void saveList(final List<T> list) throws IOException {
        requireListKey();
        saveList(listKey, list);
    }

    private void requireListKey() {
        if (listKey == null) {
            throw new IllegalStateException(
                "No list key configured for this ModelFileManager. "
                + "Use the 4-arg constructor to set or call saveList(key, list)/loadList(key) ");
        }
    }

}
