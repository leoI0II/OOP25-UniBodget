package it.unibo.unibodget.model.currency.bank;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankListTest {

    @BeforeEach
    void resetBankList() throws Exception {
        Field loadedField = BankList.class.getDeclaredField("LOADED");
        loadedField.setAccessible(true);
        List<?> loadedList = (List<?>) loadedField.get(null);
        loadedList.clear();

        Field initField = BankList.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, true);
    }

    @Test
    void shouldLoadMockBanksIfJsonMissing() throws Exception {
        Field initField = BankList.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, false);

        BankList list = new BankList();

        assertFalse(list.getBanks().isEmpty());
        assertEquals(1, list.getBanks().size());
    }

    @Test
    void shouldAddBank() {
        BankList list = new BankList(); 

        Bank b = new Bank("NewBank", 1.0, 1.0);

        assertTrue(list.add(b));
        assertTrue(list.getBanks().contains(b));
    }

    @Test
    void shouldNotAddDuplicateBank() {
        BankList list = new BankList();

        Bank b = new Bank("NewBank", 1.0, 1.0);

        assertTrue(list.add(b)); 
        assertFalse(list.add(b)); 
    }
}
