package it.unibo.unibodget.persistency;

import org.apache.commons.csv.CSVFormat;

/**
 * Provides the shared format used by CSV exports.
 */
public final class CsvExportFormat {

    /**
     * Prevents instantiation.
     */
    private CsvExportFormat() {
    }

    /**
     * Returns the CSV format used for spreadsheet exports.
     *
     * @return a semicolon-separated format with Windows-compatible line endings
     */
    public static CSVFormat getFormat() {
        return CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setRecordSeparator("\r\n")
                .build();
    }
}
