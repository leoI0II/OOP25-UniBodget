package it.unibo.unibodget.model.investment;

import java.io.File;

/**
 * Represents the result of an export operation.
 * This is a sealed interface that can either be a {@link Success} or an {@link Error}.
 */
public sealed interface ExportResult permits ExportResult.Success, ExportResult.Error {

    /**
     * Represents a successful export operation.
     *
     * @param file the file that was successfully exported
     */
    record Success(File file) implements ExportResult { }

    /**
     * Represents a failed export operation.
     *
     * @param message the error message detailing why the export failed
     */
    record Error(String message) implements ExportResult { }
}
