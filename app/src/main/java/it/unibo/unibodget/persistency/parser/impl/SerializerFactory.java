package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.util.FilesUtils;

public final class SerializerFactory {

    private SerializerFactory() {
        
    }
    
    /**
     * Creates a serializer based on the file extension.
     *
     * @param filePath the path of the file to serialize into
     * @param type the class of the object to serialize
     * @return a DataSerializer for the given type and file format
     */
    public static <T> DataSerializer<T> create(String filePath, Class<T> type) throws DataSerializerException {
        String ext = FilesUtils.getFileExtension(filePath);

        switch (ext) {
            case "json":
                return new JsonDataSerializer<>(type);
            default:
                throw new DataSerializerException("Unsupported file type: " + ext);
        }
    }

}
