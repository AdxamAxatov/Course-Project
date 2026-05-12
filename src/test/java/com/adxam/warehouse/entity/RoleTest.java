package com.adxam.warehouse.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @ParameterizedTest
    @CsvSource({"ADMIN, ADMIN", "admin, ADMIN", "User, USER", "VISITOR, VISITOR", "visitor, VISITOR"})
    void parseIsCaseInsensitive(String raw, Role expected) {
        assertEquals(expected, Role.parse(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"guest", "owner", "", "  "})
    void parseRejectsUnknown(String raw) {
        assertThrows(IllegalArgumentException.class, () -> Role.parse(raw));
    }

    @Test
    void parseRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> Role.parse(null));
    }
}
