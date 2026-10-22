package com.wordtrail.usecase;

import java.util.Locale;

/** Shared input policies; no persistence or UI dependencies. */
final class InputValidation {
    private InputValidation() { }

    static String text(String value, String label, int maximum) {
        if (value == null || value.strip().isEmpty()) { throw new AppException(label + " is required."); }
        String result = value.strip();
        if (result.length() > maximum) { throw new AppException(label + " must be at most " + maximum + " characters."); }
        return result;
    }

    static String username(String value) {
        String result = text(value, "Username", 30).toLowerCase(Locale.ROOT);
        if (!result.matches("[a-z0-9_]{3,30}")) {
            throw new AppException("Use 3-30 letters, digits or underscores for the username.");
        }
        return result;
    }

    static void password(char[] password) {
        if (password == null || password.length < 8 || password.length > 128) {
            throw new AppException("Use a password between 8 and 128 characters.");
        }
    }
}
