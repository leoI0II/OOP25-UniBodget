package it.unibo.unibodget.model.investment.service;

import it.unibo.unibodget.model.investment.BalanceSnapshot;

import java.util.List;
import java.util.UUID;

public interface InvestmentsSnapshotService {
    void save(UUID accountId, BalanceSnapshot snapshot);
    List<BalanceSnapshot> getSnapshots(UUID accountId);
}
