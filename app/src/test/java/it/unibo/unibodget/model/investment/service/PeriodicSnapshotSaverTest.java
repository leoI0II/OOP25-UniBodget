package it.unibo.unibodget.model.investment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.investment.BalanceSnapshot;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link PeriodicSnapshotSaver} saves a snapshot only when the last one is older than
 * the interval. The ticks are simulated calling onTick with a chosen time, so no JavaFX is needed.
 */
class PeriodicSnapshotSaverTest {

    private static final Duration INTERVAL = Duration.ofMinutes(10);
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 23, 10, 0);
    private static final int MINUTES_3 = 3;
    private static final int MINUTES_5 = 5;
    private static final int MINUTES_7 = 7;
    private static final int MINUTES_9 = 9;
    private static final int MINUTES_10 = 10;
    // empty accounts never ask a price, so the converter is never used
    private static final CurrencyConverter NO_CONVERSION = (amount, from, to) -> null;

    private final Map<UUID, List<BalanceSnapshot>> saved = new HashMap<>();
    private final InvestmentsSnapshotService memoryService =
            new InvestmentsSnapshotService() {
                @Override
                public void save(final UUID accountId, final BalanceSnapshot snapshot) {
                    saved.computeIfAbsent(accountId, k -> new ArrayList<>()).add(snapshot);
                }

                @Override
                public List<BalanceSnapshot> getSnapshots(final UUID accountId) {
                    return saved.getOrDefault(accountId, List.of());
                }
            };

    private InvestmentAccount account;
    private PeriodicSnapshotSaver saver;

    @BeforeEach
    void setUp() {
        account = new InvestmentAccount("Broker", FiatCurrency.EUR, NO_CONVERSION);
        saver = new PeriodicSnapshotSaver(() -> List.of(account), memoryService, INTERVAL);
    }

    @Test
    void shouldSaveAtTheFirstTickWhenThereAreNoSnapshots() {
        saver.onTick(START);
        assertEquals(1, savedCount());
    }

    @Test
    void shouldWaitTheIntervalBetweenTwoSnapshots() {
        saver.onTick(START);
        saver.onTick(START.plusMinutes(MINUTES_5));
        saver.onTick(START.plusMinutes(MINUTES_9));
        assertEquals(1, savedCount());

        saver.onTick(START.plusMinutes(MINUTES_10));
        assertEquals(2, savedCount());
    }

    @Test
    void shouldUseTheLastSavedSnapshotAfterARestart() {
        // a snapshot saved 3 minutes before the app was opened
        memoryService.save(
                account.getId(),
                new BalanceSnapshot(START.minusMinutes(MINUTES_3), BigDecimal.ZERO, BigDecimal.ZERO));

        saver.onTick(START);
        assertEquals(1, savedCount());

        saver.onTick(START.plusMinutes(MINUTES_7));
        assertEquals(2, savedCount());
    }

    private int savedCount() {
        return saved.getOrDefault(account.getId(), List.of()).size();
    }
}
