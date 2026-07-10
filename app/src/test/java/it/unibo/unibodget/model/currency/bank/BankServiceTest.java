package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BankServiceTest {

    @Test
    void shouldLoadBanks() {
        BankService service = new BankService();

        assertFalse(service.loadBanks().isEmpty());
    }

    @Test
    void shouldAddBankInMemory() {
        BankService service = new BankService();

        Bank b = new Bank("CustomBank", 2.0, 1.0);
        service.addBankInMemory(b);

        assertTrue(service.loadBanks().contains(b));
    }

}
