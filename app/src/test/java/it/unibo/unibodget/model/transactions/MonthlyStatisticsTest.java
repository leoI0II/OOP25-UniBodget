package it.unibo.unibodget.model.transactions;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.CategoryType;

class MonthlyStatisticsTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        MonthlyStatistics.CategoryTotal ct =
                new MonthlyStatistics.CategoryTotal("Food", CategoryType.EXPENSE, new BigDecimal("50"));

        MonthlyStatistics stats = new MonthlyStatistics(
                YearMonth.of(2024, 1),
                new BigDecimal("100"),
                new BigDecimal("50"),
                new BigDecimal("50"),
                List.of(ct)
        );

        assertEquals(YearMonth.of(2024, 1), stats.getMonth());
        assertEquals(new BigDecimal("100"), stats.getTotalIncome());
        assertEquals(new BigDecimal("50"), stats.getTotalExpense());
        assertEquals(new BigDecimal("50"), stats.getNetBalance());
        assertEquals(1, stats.getCategoryTotals().size());
        assertEquals(ct, stats.getCategoryTotals().get(0));
    }

}
