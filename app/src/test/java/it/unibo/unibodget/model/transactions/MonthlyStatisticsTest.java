package it.unibo.unibodget.model.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.CategoryType;

class MonthlyStatisticsTest {

    private static final String FOOD = "Food";
    private static final int VAL_50 = 50;
    private static final int VAL_100 = 100;
    private static final int YEAR_2024 = 2024;
    private static final int MONTH = 1;

    @Test
    void shouldStoreFieldsCorrectly() {
        final MonthlyStatistics.CategoryTotal ct =
                new MonthlyStatistics.CategoryTotal(FOOD, 
                                                    CategoryType.EXPENSE, 
                                                    new BigDecimal(VAL_50));

        final MonthlyStatistics stats = new MonthlyStatistics(
                YearMonth.of(YEAR_2024, MONTH),
                new BigDecimal(VAL_100),
                new BigDecimal(VAL_50),
                new BigDecimal(VAL_50),
                List.of(ct)
        );

        assertEquals(YearMonth.of(YEAR_2024, MONTH), stats.getMonth());
        assertEquals(new BigDecimal(VAL_100), stats.getTotalIncome());
        assertEquals(new BigDecimal(VAL_50), stats.getTotalExpense());
        assertEquals(new BigDecimal(VAL_50), stats.getNetBalance());
        assertEquals(1, stats.getCategoryTotals().size());
        assertEquals(ct, stats.getCategoryTotals().get(0));
    }

}
