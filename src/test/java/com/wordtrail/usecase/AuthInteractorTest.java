package com.wordtrail.usecase;

import com.wordtrail.entity.Role;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthInteractorTest {
    @Test void firstRunCreatesOneAdminAndRegistrationAlwaysCreatesLearners() {
        UseCaseFixture f = new UseCaseFixture();
        assertTrue(f.auth.needsSetup());
        assertThrows(AppException.class, () -> f.learner("alice"));
        char[] password = "password123".toCharArray();
        assertEquals(Role.ADMIN, f.auth.setupAdministrator(" ADMIN ", password).getRole());
        assertArrayEquals(new char[password.length], password);
        assertFalse(f.auth.needsSetup());
        assertThrows(AppException.class, () -> f.auth.setupAdministrator("other", "password123".toCharArray()));
        f.learner("alice");
        assertEquals(Role.LEARNER, f.session.requireUser().getRole());
        assertThrows(AppException.class, () -> f.learner("ALICE"));
    }

    @Test void invalidCredentialsCannotOpenASession() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        f.auth.logout();
        assertThrows(AppException.class, () -> f.auth.login("admin", "wrong".toCharArray()));
        assertThrows(AppException.class, f.session::requireUser);
        assertThrows(AppException.class, () -> f.auth.login("missing", "password123".toCharArray()));
        assertEquals("admin", f.auth.login("ADMIN", "password123".toCharArray()).getUsername());
    }

    @Test void invalidRegistrationClearsPasswordsAndDoesNotCreateAccounts() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        for (String username : Arrays.asList("ab", "with space", "../unsafe")) {
            char[] password = "password123".toCharArray();
            assertThrows(AppException.class, () -> f.auth.register(username, password));
            assertArrayEquals(new char[password.length], password);
        }
        assertThrows(AppException.class, () -> f.auth.register("alice", "short".toCharArray()));
        assertEquals(1, f.store.users.size());
    }
}
