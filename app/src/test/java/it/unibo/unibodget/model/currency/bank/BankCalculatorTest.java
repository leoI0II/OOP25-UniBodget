package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class BankCalculatorTest {

    @Test
    void shouldCalculateCommissionCorrectly() {
        Bank bank = new Bank("TestBank", 5.0, 10.0);

        BigDecimal commission =
                BankCalculator.calculateCommission(new BigDecimal("100"), bank);

        // fixed 5 + 10% of 100 = 10 → total = 15
        assertEquals(new BigDecimal("15.00"), commission);
    }

    @Test
    void shouldRoundCommissionToTwoDecimals() {
        Bank bank = new Bank("TestBank", 1.0, 3.333);

        BigDecimal commission =
                BankCalculator.calculateCommission(new BigDecimal("50"), bank);

        assertEquals(2, commission.scale());
    }

}
