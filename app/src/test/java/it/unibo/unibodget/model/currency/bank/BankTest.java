package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class BankTest {

    private static final String BANK_EX = "MyBank";
    private static final int FIX_FEE_1 = 1;
    private static final double FIXED_FEE_5 = 5.0;
    private static final double FIXED_FEE_10 = 10.0;
    private static final double PERC_FEE_2 = 2.5;
    private static final double PERC_FEE_DOUBLE_1 = 1.0;
    private static final int PERC_FEE_1 = 1;

    @Test
    void shouldStoreFieldsCorrectly() {
        final Bank b = new Bank(BANK_EX, FIXED_FEE_5, PERC_FEE_2);

        assertEquals(BANK_EX, b.getName());
        assertEquals(FIXED_FEE_5, b.getFixedFee());
        assertEquals(PERC_FEE_2, b.getPercentageFee());
    }

    @Test
    void shouldImplementEqualsByName() {
        final Bank b1 = new Bank(BANK_EX, FIXED_FEE_5, PERC_FEE_2);
        final Bank b2 = new Bank(BANK_EX, FIXED_FEE_10, PERC_FEE_DOUBLE_1);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void shouldNotBeEqualIfNameDiffers() {
        final Bank b1 = new Bank("A", FIX_FEE_1, PERC_FEE_1);
        final Bank b2 = new Bank("B", FIX_FEE_1, PERC_FEE_1);

        assertNotEquals(b1, b2);
    }

}
