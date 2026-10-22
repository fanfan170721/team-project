package com.wordtrail.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Pbkdf2PasswordHasherTest {
    @Test void usesUniqueSaltsAndRejectsWrongOrMalformedPasswords() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        char[] password = "example password".toCharArray();
        String hash = hasher.hash(password);
        assertNotEquals(hash, hasher.hash(password));
        assertFalse(hash.contains("example password"));
        assertTrue(hasher.matches(password, hash));
        assertFalse(hasher.matches("wrong".toCharArray(), hash));
        assertFalse(hasher.matches(password, "corrupt"));
        assertFalse(hasher.matches(password, "pbkdf2-sha256$999999999$invalid$invalid"));
    }
}
