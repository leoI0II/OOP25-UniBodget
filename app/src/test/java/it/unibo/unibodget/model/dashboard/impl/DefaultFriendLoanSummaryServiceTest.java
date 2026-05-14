package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Tests for {@link DefaultFriendLoanSummaryService}.
 */
class DefaultFriendLoanSummaryServiceTest {

    @Test
    void shouldSummarizeFriendLoansByLoanId() {
        final DefaultFriendLoanSummaryService service = new DefaultFriendLoanSummaryService();

        final Category friendLoanCategory = new Category(
                "Friend loan",
                new ARGBColor(0xFF9C27B0),
                CategoryType.FRIEND_LOAN
        );

        final UUID marcoLoanId = UUID.randomUUID();
        final UUID lucaLoanId = UUID.randomUUID();

        final CashTransaction marcoLoan = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-100.00")),
                friendLoanCategory,
                LocalDate.of(2026, 5, 1),
                "Loan to Marco",
                "Initial loan",
                marcoLoanId,
                "Marco"
        );

        final CashTransaction marcoRepayment = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("30.00")),
                friendLoanCategory,
                LocalDate.of(2026, 5, 10),
                "Marco repayment",
                "Partial repayment",
                marcoLoanId,
                "Marco"
        );

        final CashTransaction lucaLoan = new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("-50.00")),
                friendLoanCategory,
                LocalDate.of(2026, 5, 3),
                "Loan to Luca",
                "Short loan",
                lucaLoanId,
                "Luca"
        );

        final List<FriendLoanSummary> result = service.summarize(
                List.of(marcoLoan, marcoRepayment, lucaLoan)
        );

        assertEquals(2, result.size());

        final FriendLoanSummary first = result.get(0);
        final FriendLoanSummary second = result.get(1);

        assertEquals("Marco", first.getFriendName());
        assertEquals(new BigDecimal("100.00"), first.getTotalGiven());
        assertEquals(new BigDecimal("30.00"), first.getTotalReceived());
        assertEquals(new BigDecimal("70.00"), first.getNetBalance());

        assertEquals("Luca", second.getFriendName());
        assertEquals(new BigDecimal("50.00"), second.getTotalGiven());
        assertEquals(BigDecimal.ZERO, second.getTotalReceived());
        assertEquals(new BigDecimal("50.00"), second.getNetBalance());
    }
}