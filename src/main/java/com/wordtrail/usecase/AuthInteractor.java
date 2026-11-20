package com.wordtrail.usecase;

import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;
import com.wordtrail.usecase.port.AccountRepository;
import com.wordtrail.usecase.port.PasswordHasher;
import java.util.Arrays;

/** US-01: account setup, learner registration and login. */
public final class AuthInteractor implements AuthUseCase {
    private final AccountRepository accounts;
    private final PasswordHasher hasher;
    private final SessionContext session;

    public AuthInteractor(AccountRepository accounts, PasswordHasher hasher, SessionContext session) {
        this.accounts = accounts;
        this.hasher = hasher;
        this.session = session;
    }

    @Override
    public boolean needsSetup() {
        return !accounts.hasAccounts();
    }

    @Override
    public User setupAdministrator(String username, char[] password) {
        try {
            if (!needsSetup()) {
                throw new AppException("Setup is already complete. Please log in.");
            }
            return create(username, password, Role.ADMIN);
        } finally {
            clear(password);
        }
    }

    @Override
    public User register(String username, char[] password) {
        try {
            if (needsSetup()) {
                throw new AppException("Create the initial administrator first.");
            }
            return create(username, password, Role.LEARNER);
        } finally {
            clear(password);
        }
    }

    private User create(String username, char[] password, Role role) {
        String name = InputValidation.username(username);
        InputValidation.password(password);
        if (accounts.findUser(name).isPresent()) {
            throw new AppException("That username is already taken.");
        }
        User user = new User(name, hasher.hash(password), role);
        accounts.createUser(user);
        session.signIn(user);
        return user;
    }

    @Override
    public User login(String username, char[] password) {
        try {
            String name = InputValidation.username(username);
            if (password == null || password.length == 0 || password.length > 128) {
                throw new AppException("Enter your username and password.");
            }
            User user = accounts.findUser(name).orElseThrow(() -> new AppException("Incorrect username or password."));
            if (!hasher.matches(password, user.getPasswordHash())) {
                throw new AppException("Incorrect username or password.");
            }
            session.signIn(user);
            return user;
        } finally {
            clear(password);
        }
    }

    @Override
    public void logout() {
        session.signOut();
    }

    private static void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
