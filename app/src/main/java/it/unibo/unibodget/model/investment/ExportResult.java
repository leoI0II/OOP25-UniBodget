package it.unibo.unibodget.model.investment;

import java.io.File;

/**
 * 
 * 
 */
public sealed interface ExportResult permits ExportResult.Success, ExportResult.Error {
    /**
     * 
     *
     * @param file
     */
    record Success(File file) implements ExportResult {

    }

    /**
     * 
     *
     * @param message
     */
    record Error(String message) implements ExportResult {

    }
}
