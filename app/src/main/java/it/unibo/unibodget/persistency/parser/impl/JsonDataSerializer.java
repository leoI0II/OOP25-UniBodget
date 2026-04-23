package it.unibo.unibodget.persistency.parser.impl;
import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

public class JsonDataSerializer<T> extends BasicDataSerializer<T> implements DataSerializer<T>{
    private final Class<T> type;

    public JsonDataSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public String serialize(T obj) throws DataSerializerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }
}
