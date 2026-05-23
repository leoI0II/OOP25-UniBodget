package it.unibo.unibodget.model.investment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceSnapshot(
        LocalDateTime timestamp,
        BigDecimal totalPL,
        BigDecimal costBasis
) {
}
