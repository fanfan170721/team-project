package com.wordtrail.usecase;

import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;

/** Authentication state shared by interactors; session version invalidates old decks and quizzes. */
public final class SessionContext {
    private User user;
    private long version;

    public User requireUser() {
        if (user == null) {
            throw new AppException("Please log in first.");
        }
        return user;
    }

    public User requireAdmin() {
        User current = requireUser();
        if (current.getRole() != Role.ADMIN) {
            throw new AppException("Only an administrator can change content.");
        }
        return current;
    }

    public long getVersion() {
        return version;
    }
    void signIn(User current) {
        user = current;
        version++;
    }
    void signOut() {
        user = null;
        version++;
    }
}
