package it.unibo.unibodget.model.investment.service;

import it.unibo.unibodget.model.investment.BalanceSnapshot;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CSVInvestmentsSnapshotService implements InvestmentsSnapshotService {

    private static final Path DEFAULT_DATA_DIR = Path.of(
            System.getProperty("user.home"), ".unibodget", "snapshots"
    );

    private final Path dataDir;

    public CSVInvestmentsSnapshotService(final Path dataDir) {
        this.dataDir = dataDir;
        var dir =  dataDir.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to create data directory: " + dir.getAbsolutePath());
        }
    }

    public CSVInvestmentsSnapshotService() {
        this(DEFAULT_DATA_DIR);
    }

    private Path fileFor(UUID accountId) {
        return dataDir.resolve(accountId + "_snapshots.csv");
    }

    @Override
    public void save(UUID accountId, BalanceSnapshot snapshot) {
        var filePath = fileFor(accountId);
        final CSVFormat format = CSVFormat.DEFAULT.builder().build();
        try (
                final FileWriter writer = new FileWriter(filePath.toFile(), true);
                final CSVPrinter pr = new CSVPrinter(writer, format);
        ) {
            pr.printRecord(
                    snapshot.timestamp(),
                    snapshot.totalPL(),
                    snapshot.costBasis()
            );
        } catch (final IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    @Override
    public List<BalanceSnapshot> getSnapshots(UUID accountId) {
        var filePath = fileFor(accountId);
        if (!filePath.toFile().exists()) {
            return List.of(); // no snapshot yet
        }

        try (
                var reader = new FileReader(filePath.toFile());
                var parser = CSVFormat.DEFAULT.parse(reader)
        ) {
            return parser.getRecords().stream()
                    .map(record -> new BalanceSnapshot(
                            LocalDate.parse(record.get(0)),
                            new BigDecimal(record.get(1)),
                            new BigDecimal(record.get(2))
                    ))
                    .toList();
        } catch (IOException e) {
            System.err.println("Error reading snapshots: " + e.getMessage());
            return List.of();
        }
    }
}
