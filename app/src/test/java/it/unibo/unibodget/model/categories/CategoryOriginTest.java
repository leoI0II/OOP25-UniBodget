package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CategoryOriginTest {

    @Test
    void shouldContainDefaultAndCustom() {
        assertNotNull(CategoryOrigin.DEFAULT);
        assertNotNull(CategoryOrigin.CUSTOM);
    }

}
