package it.unibo.unibodget.model.investment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BalanceSnapshot(
        LocalDate timestamp,
        BigDecimal totalPL,
        BigDecimal costBasis
) {
}
