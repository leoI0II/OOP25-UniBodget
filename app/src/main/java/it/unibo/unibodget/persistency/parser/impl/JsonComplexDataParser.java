package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

import java.lang.reflect.*;
import java.util.*;

public final class JsonComplexDataParser<T> implements DataParser<T> {

    private final Class<T> targetClass;

    public JsonComplexDataParser(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T parse(String json) throws DataParserException {
        T prova = null;
        return prova;
    }

    
}
