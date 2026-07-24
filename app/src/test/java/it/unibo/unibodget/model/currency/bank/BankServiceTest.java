package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BankServiceTest {

    @Test
    void shouldLoadBanks() {
        final BankService service = new BankService();

        assertFalse(service.loadBanks().isEmpty());
    }

    @Test
    void shouldAddBankInMemory() {
        final BankService service = new BankService();

        final Bank b = new Bank("CustomBank", 2.0, 1.0);
        service.addBankInMemory(b);

        assertTrue(service.loadBanks().contains(b));
    }

}
