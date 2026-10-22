package com.wordtrail.usecase;

import com.wordtrail.entity.User;

public interface AuthUseCase {
    boolean needsSetup();
    User setupAdministrator(String username, char[] password);
    User register(String username, char[] password);
    User login(String username, char[] password);
    void logout();
}
