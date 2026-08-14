package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.util.FilesUtils;

/**
 * Factory responsible for creating {@link DataSerializer} instances
 * based on the file extension of the target output file.
 * 
 * <p>
 * Supports JSON serialization and throws an exception for unsupported formats.
 * </p>
 */
public final class SerializerFactory {

    private SerializerFactory() {
        // Utility class
    }

    /**
     * Creates a serializer based on the file extension.
     *
     * @param <T> the type handled by the serializer
     * @param filePath the path of the file to serialize into
     * @param type the class of the object to serialize
     * @return a DataSerializer for the given type and file format
     * @throws DataSerializerException if the file extension is unsupported
     */
    public static <T> DataSerializer<T> create(final String filePath, final Class<T> type) 
        throws DataSerializerException {
        final String ext = FilesUtils.getFileExtension(filePath);

        switch (ext) {
            case "json":
                return new JsonDataSerializer<>(type);
            default:
                throw new DataSerializerException("Unsupported file type: " + ext);
        }
    }

}
