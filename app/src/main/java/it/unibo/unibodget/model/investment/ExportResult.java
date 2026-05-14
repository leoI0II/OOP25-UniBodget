package it.unibo.unibodget.model.investment;

import java.io.File;

public sealed interface ExportResult permits ExportResult.Success, ExportResult.Error {
    record Success(File file) implements ExportResult {}
    record Error(String message) implements ExportResult {}
}
