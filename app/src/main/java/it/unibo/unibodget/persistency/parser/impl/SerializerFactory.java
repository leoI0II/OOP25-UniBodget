package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;

public final class SerializerFactory {

    private SerializerFactory() {
        
    }
    
    public static <T> DataSerializer<T> create(Class<T> type) {
        return new JsonDataSerializer<>(type);
    }

}
