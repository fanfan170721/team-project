package com.wordtrail.adapter;

import com.wordtrail.usecase.AuthUseCase;

/** Coordinates authentication actions and produces safe account display data. */
public final class AuthController {
    private final AuthUseCase auth;
    private final AuthPresenter presenter;

    public AuthController(AuthUseCase auth, AuthPresenter presenter) {
        this.auth = auth;
        this.presenter = presenter;
    }

    public boolean needsSetup() { return ControllerCalls.call(auth::needsSetup); }

    public ViewModels.Account setup(String username, char[] password) {
        return ControllerCalls.call(() -> presenter.account(auth.setupAdministrator(username, password)));
    }

    public ViewModels.Account register(String username, char[] password) {
        return ControllerCalls.call(() -> presenter.account(auth.register(username, password)));
    }

    public ViewModels.Account login(String username, char[] password) {
        return ControllerCalls.call(() -> presenter.account(auth.login(username, password)));
    }

    public void logout() {
        ControllerCalls.call(() -> { auth.logout(); return null; });
    }
}
