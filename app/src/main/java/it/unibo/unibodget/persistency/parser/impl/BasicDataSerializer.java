package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

public class BasicDataSerializer<T> {
    protected void ensure(boolean condition, String message) throws DataSerializerException {
        if (!condition) {
            throw new DataSerializerException(message);
        }
    }

    protected String quote(String s) {
        return "\"" + s + "\"";
    }
}
