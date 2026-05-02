package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.util.FilesUtils;

/**
 * Factory responsible for creating the appropriate {@link DataParser}
 * implementation based on the file type.
 * 
 * Currently, only JSON files are supported. If the file extension is "json",
 * a {@link JsonDataParser} instance is returned. Otherwise, a
 * {@link DataParserException} is thrown.
 */
public final class ParserFactory {

    private ParserFactory() {
        // Utility class: prevent instantiation
    }

    /**
     * Returns a parser for the given type based on the file extension.
     *
     * @param targetClass           the class of the object to be parsed
     * @param filePath              the path of the file to be parsed
     * @param <T>                   the type of the object to be parsed
     * @return                      a {@link DataParser} instance suitable for the file type
     * @throws DataParserException  if the file type is not supported
     */
    public static <T> DataParser<T> of(Class<T> targetClass, String filePath) throws DataParserException {
        switch (FilesUtils.getFileExtension(filePath)){
            case "json":
                return new JsonDataParser<>(targetClass);
            default:
                throw new DataParserException("Unsupported file type: " + FilesUtils.getFileExtension(filePath));
        }
    }

}
