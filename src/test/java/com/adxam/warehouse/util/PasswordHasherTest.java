package com.adxam.warehouse.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void newSaltIsRandom() {
        String s1 = PasswordHasher.newSalt();
        String s2 = PasswordHasher.newSalt();
        assertNotEquals(s1, s2);
        assertEquals(32, s1.length()); // 16 bytes hex = 32 chars
    }

    @Test
    void hashIsDeterministicGivenSameSalt() {
        String salt = "abcdef0123456789";
        String h1 = PasswordHasher.hash("password", salt);
        String h2 = PasswordHasher.hash("password", salt);
        assertEquals(h1, h2);
    }

    @Test
    void hashDiffersAcrossSalts() {
        String h1 = PasswordHasher.hash("password", "salt-a");
        String h2 = PasswordHasher.hash("password", "salt-b");
        assertNotEquals(h1, h2);
    }

    @Test
    void matchesAcceptsCorrectPassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("hunter2", salt);
        assertTrue(PasswordHasher.matches("hunter2", salt, hash));
    }

    @Test
    void matchesRejectsWrongPassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("hunter2", salt);
        assertFalse(PasswordHasher.matches("hunter3", salt, hash));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "longerpassword", "with spaces", "пароль"})
    void hashWorksForVariousInputs(String pw) {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash(pw, salt);
        assertNotNull(hash);
        assertTrue(PasswordHasher.matches(pw, salt, hash));
    }

    @Test
    void hashRejectsNullInputs() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null, "s"));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash("p", null));
    }
}
