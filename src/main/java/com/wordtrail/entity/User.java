package com.wordtrail.entity;

import java.util.Objects;

/** An account contains a password hash, never a plaintext password. */
public final class User {
    private final String username;
    private final String passwordHash;
    private final Role role;

    public User(String username, String passwordHash, Role role) {
        this.username = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}
