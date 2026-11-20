package com.wordtrail.adapter;

import com.wordtrail.usecase.ProgressUseCase;

/** Requests the current account's progress and builds its dashboard response. */
public final class ProgressController {
    private final ProgressUseCase progress;
    private final ProgressPresenter presenter;

    public ProgressController(ProgressUseCase progress, ProgressPresenter presenter) {
        this.progress = progress;
        this.presenter = presenter;
    }

    public ViewModels.Dashboard dashboard() {
        return ControllerCalls.call(() -> presenter.dashboard(progress.report()));
    }
}
