package com.wordtrail.adapter;

import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;

/** Exposes the signed-in account and role, never its stored password hash. */
public final class AuthPresenter {
    public ViewModels.Account account(User user) {
        return new ViewModels.Account(user.getUsername(), user.getRole() == Role.ADMIN);
    }
}
