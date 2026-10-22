package com.wordtrail.usecase.port;

public interface PasswordHasher {
    String hash(char[] password);
    boolean matches(char[] password, String encodedHash);
}
