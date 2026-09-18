package it.unibo.unibodget.model.investment.service;

import it.unibo.unibodget.model.investment.BalanceSnapshot;

import java.util.List;
import java.util.UUID;

/**
 * Service for persisting and retrieving {@link BalanceSnapshot} records for investment accounts.
 *
 * <p>Each snapshot captures the portfolio's total P/L and cost basis at a given point in time,
 * allowing the UI to render a historical performance chart.</p>
 */
public interface InvestmentsSnapshotService {

    /**
     * Persists a balance snapshot for the specified investment account.
     *
     * @param accountId the unique identifier of the investment account
     * @param snapshot  the snapshot to save
     */
    void save(UUID accountId, BalanceSnapshot snapshot);

    /**
     * Returns all previously saved snapshots for the specified investment account,
     * ordered by the time they were recorded.
     * Returns an empty list if no snapshots exist yet for the account.
     *
     * @param accountId the unique identifier of the investment account
     * @return an unmodifiable list of {@link BalanceSnapshot} records, possibly empty
     */
    List<BalanceSnapshot> getSnapshots(UUID accountId);
}
