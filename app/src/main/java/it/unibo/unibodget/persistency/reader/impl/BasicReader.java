package it.unibo.unibodget.persistency.reader.impl;

import it.unibo.unibodget.persistency.reader.api.FileReader;

/**
 * A base abstract class for FileReader implementations.
 *
 * @param <T> the type returned by the reader
 */
public abstract class BasicReader<T> implements FileReader<T> {
    private final String path;

    protected BasicReader(final String path) {
        this.path = path;
    }

    /** 
     * Returns the path of the file to be read.
      *
      * @return the path of the file
     */
    protected String getPath() {
        return this.path;
    }
}