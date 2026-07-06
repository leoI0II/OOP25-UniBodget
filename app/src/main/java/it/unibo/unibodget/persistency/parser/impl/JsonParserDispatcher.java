package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * Dispatcher for JSON parsing and serialization operations.
 * Automatically selects the appropriate parser implementation
 * based on the complexity of the target type.
 *
 * <p>Simple types (primitives, strings, enums) are handled by
 * {@link JsonDataParser}, while complex types (collections, maps,
 * records, or objects with nested fields) are delegated to
 * {@link JsonComplexDataParser} and {@link JsonComplexDataSerializer}.
 */
public final class JsonParserDispatcher {

    /**
     * Parses a JSON string into an instance of the specified type.
     * Automatically selects {@link JsonComplexDataParser} for complex types
     * and {@link JsonDataParser} for simple types.
     *
     * @param <T>  the target type
     * @param json the JSON string to parse
     * @param type the {@link Class} of the target type
     * @return the parsed instance of type {@code T}
     * @throws DataParserException if parsing fails
     */
    public static <T> T parse(String json, Class<T> type) throws DataParserException {
        if (shouldUseComplexParser(type)) {
            return new JsonComplexDataParser<>(type).parse(json);
        }
        return new JsonDataParser<>(type).parse(json);
    }

    /**
     * Serializes an object into its JSON string representation.
     *
     * @param <T>   the type of the object to serialize
     * @param value the object to serialize
     * @return the JSON string representation of {@code value}
     * @throws DataSerializerException if serialization fails
     */
    public static <T> String serialize(T value) throws DataSerializerException {
        if (shouldUseComplexParser(value.getClass())) {
            return new JsonComplexDataSerializer<T>().serialize(value);
        }
        return new JsonDataSerializer<T>().serialize(value);
    }

    /**
     * Determines whether the given type requires the complex parser.
     * A type is considered complex if it is a {@link Collection}, a {@link Map},
     * a record, or a class containing fields of non-simple types.
     *
     * @param type the {@link Class} to evaluate
     * @return {@code true} if the complex parser should be used, {@code false} otherwise
     */
    private static boolean shouldUseComplexParser(Class<?> type) {
        if (Collection.class.isAssignableFrom(type)) return true;
        if (Map.class.isAssignableFrom(type)) return true;
        if (type.isRecord()) return true;
        for (Field f : type.getDeclaredFields()) {
            if (!isSimpleType(f.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the given type is considered simple.
     * Simple types include primitives, {@link String}, {@link Number} subclasses,
     * {@link Boolean}, and enums.
     *
     * @param type the {@link Class} to evaluate
     * @return {@code true} if the type is simple, {@code false} otherwise
     */
    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || Number.class.isAssignableFrom(type)
            || type == Boolean.class
            || type.isEnum();
    }
}
