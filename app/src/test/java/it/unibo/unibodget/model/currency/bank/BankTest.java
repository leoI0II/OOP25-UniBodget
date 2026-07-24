package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class BankTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        final Bank b = new Bank("MyBank", 5.0, 2.5);

        assertEquals("MyBank", b.getName());
        assertEquals(5.0, b.getFixedFee());
        assertEquals(2.5, b.getPercentageFee());
    }

    @Test
    void shouldImplementEqualsByName() {
        final Bank b1 = new Bank("MyBank", 5.0, 2.5);
        final Bank b2 = new Bank("MyBank", 10.0, 1.0);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void shouldNotBeEqualIfNameDiffers() {
        final Bank b1 = new Bank("A", 1, 1);
        final Bank b2 = new Bank("B", 1, 1);

        assertNotEquals(b1, b2);
    }

}
