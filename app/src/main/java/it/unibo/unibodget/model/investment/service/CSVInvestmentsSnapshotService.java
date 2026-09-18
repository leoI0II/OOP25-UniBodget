package it.unibo.unibodget.model.investment.service;

import it.unibo.unibodget.model.investment.BalanceSnapshot;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.TransactionAddedEvent;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * CSV-backed implementation of {@link InvestmentsSnapshotService}.
 *
 * <p>Each {@link InvestmentAccount} gets its own CSV file named
 * {@code <accountId>_snapshots.csv} stored under the configured data directory.
 * The service subscribes to {@link TransactionAddedEvent} via the {@link MessageBus}
 * and automatically persists a new balance snapshot whenever a transaction is added
 * to an {@link InvestmentAccount}.</p>
 *
 * <p>The default data directory is {@code ~/.unibodget/snapshots}.</p>
 */
public class CSVInvestmentsSnapshotService implements InvestmentsSnapshotService {

    private static final Path DEFAULT_DATA_DIR = Path.of(
            System.getProperty("user.home"), ".unibodget", "snapshots"
    );

    private final Path dataDir;

    /**
     * Creates the service using the specified directory for CSV storage.
     * The directory is created if it does not already exist.
     *
     * @param dataDir the directory in which snapshot CSV files are stored
     * @throws IllegalStateException if the directory does not exist and cannot be created
     */
    public CSVInvestmentsSnapshotService(final Path dataDir) {
        this.dataDir = dataDir;
        final var dir = dataDir.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to create data directory: " + dir.getAbsolutePath());
        }

        MessageBus.subscribe(TransactionAddedEvent.class, this::onTransactionAddedEvent);
    }

    /**
     * Creates the service using the default data directory ({@code ~/.unibodget/snapshots}).
     */
    public CSVInvestmentsSnapshotService() {
        this(DEFAULT_DATA_DIR);
    }

    /**
     * Handles a {@link TransactionAddedEvent} by saving a new balance snapshot
     * if the wallet involved is an {@link InvestmentAccount}.
     *
     * @param event the event carrying the wallet and transaction that were just added
     */
    private void onTransactionAddedEvent(final TransactionAddedEvent event) {
        if (event.wallet() instanceof InvestmentAccount account) {
            save(account.getId(), BalanceSnapshot.buildInvestmentSnapshot(account));
        }
    }

    /**
     * Returns the path of the CSV file associated with the given account.
     *
     * @param accountId the unique identifier of the investment account
     * @return the path to the account's snapshot CSV file
     */
    private Path fileFor(final UUID accountId) {
        return dataDir.resolve(accountId + "_snapshots.csv");
    }

    /** {@inheritDoc} */
    @Override
    public void save(final UUID accountId, final BalanceSnapshot snapshot) {
        final var filePath = fileFor(accountId);
        final CSVFormat format = CSVFormat.DEFAULT.builder().build();
        try (
                FileWriter writer = new FileWriter(filePath.toFile(), true);
                CSVPrinter pr = new CSVPrinter(writer, format);
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

    /**
     * Parses a timestamp string into a {@link LocalDateTime}, with fallback support
     * for older CSV files that stored only a {@link java.time.LocalDate}.
     *
     * @param s the timestamp string to parse
     * @return the parsed {@link LocalDateTime}
     */
    private LocalDateTime parseTimestamp(final String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (final DateTimeParseException e) {
            // fallback per file vecchi con LocalDate
            return LocalDate.parse(s).atStartOfDay();
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<BalanceSnapshot> getSnapshots(final UUID accountId) {
        final var filePath = fileFor(accountId);
        if (!filePath.toFile().exists()) {
            return List.of(); // no snapshot yet
        }

        try (
                var reader = new FileReader(filePath.toFile());
                var parser = CSVFormat.DEFAULT.parse(reader)
        ) {
            return parser.getRecords().stream()
                    .map(record -> new BalanceSnapshot(
                            parseTimestamp(record.get(0)),
                            new BigDecimal(record.get(1)),
                            new BigDecimal(record.get(2))
                    ))
                    .toList();
        } catch (final IOException e) {
            System.err.println("Error reading snapshots: " + e.getMessage());
            return List.of();
        }
    }
}
