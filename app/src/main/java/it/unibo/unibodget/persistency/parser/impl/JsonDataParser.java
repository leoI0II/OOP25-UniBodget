package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParserException;

public class JsonDataParser<T> extends BasicDataParser<T> {

    private final Class<T> type;

    public JsonDataParser(Class<T> type) {
        this.type = type;
    }

    @Override
    public T parse(String data) throws DataParserException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parse'");
    }

}