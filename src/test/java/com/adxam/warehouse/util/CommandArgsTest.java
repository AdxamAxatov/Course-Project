package com.adxam.warehouse.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CommandArgsTest {

    @Test
    void requireStringConsumesTokens() {
        CommandArgs args = new CommandArgs(new String[]{"add", "laptop", "HP"}, 1);
        assertEquals("laptop", args.requireString("kind"));
        assertEquals("HP", args.requireString("name"));
    }

    @Test
    void requireStringThrowsWhenMissing() {
        CommandArgs args = new CommandArgs(new String[]{"add"}, 1);
        ValidationException e = assertThrows(ValidationException.class,
                () -> args.requireString("kind"));
        assertTrue(e.getMessage().contains("kind"));
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100",
            "-5, -5",
            "0, 0"
    })
    void requireLongParsesInts(String token, long expected) {
        CommandArgs args = new CommandArgs(new String[]{token}, 0);
        assertEquals(expected, args.requireLong("v"));
    }

    @Test
    void requireLongRejectsNonNumeric() {
        CommandArgs args = new CommandArgs(new String[]{"abc"}, 0);
        assertThrows(ValidationException.class, () -> args.requireLong("v"));
    }

    @Test
    void requireDoubleParsesDecimals() {
        CommandArgs args = new CommandArgs(new String[]{"2.5"}, 0);
        assertEquals(2.5, args.requireDouble("v"));
    }

    @Test
    void optionalStringReturnsDefaultWhenMissing() {
        CommandArgs args = new CommandArgs(new String[]{}, 0);
        assertEquals("fallback", args.optionalString("x", "fallback"));
    }

    @Test
    void hasMoreReflectsCursor() {
        CommandArgs args = new CommandArgs(new String[]{"a", "b"}, 0);
        assertTrue(args.hasMore());
        args.requireString("first");
        assertTrue(args.hasMore());
        args.requireString("second");
        assertFalse(args.hasMore());
    }
}
