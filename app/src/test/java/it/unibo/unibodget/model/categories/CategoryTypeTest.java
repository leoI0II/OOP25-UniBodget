package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CategoryTypeTest {

    @Test
    void shouldContainAllTypes() {
        assertNotNull(CategoryType.INCOME);
        assertNotNull(CategoryType.EXPENSE);
        assertNotNull(CategoryType.TRANSFER);
        assertNotNull(CategoryType.FRIEND_LOAN);
        assertNotNull(CategoryType.BANK_LOAN);
    }

}
