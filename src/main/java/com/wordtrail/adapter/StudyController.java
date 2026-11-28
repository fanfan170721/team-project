package com.wordtrail.adapter;

import com.wordtrail.usecase.StudyInputBoundary;

public final class StudyController {
    private final StudyInputBoundary input;
    private final StudyPresenter presenter;

    public StudyController(StudyInputBoundary input, StudyPresenter presenter) {
        this.input = input;
        this.presenter = presenter;
    }

    private synchronized StudyViewModel run(Runnable action) {
        return ControllerCalls.call(() -> {
            action.run();
            return presenter.result();
        });
    }

    public StudyViewModel show() { return run(input::show); }
    public StudyViewModel start() { return run(input::start); }
    public StudyViewModel known(String token, int index, boolean value) {
        return run(() -> input.known(token, index, value));
    }
    public StudyViewModel advance(String token) { return run(() -> input.advance(token)); }
    public StudyViewModel answer(String token, int choice, boolean assessment) {
        return run(() -> input.answer(token, choice, assessment));
    }
    public StudyViewModel assess() { return run(input::assess); }
    public StudyViewModel adopt(String token) { return run(() -> input.adopt(token)); }
    public StudyViewModel goal(int target) { return run(() -> input.goal(target)); }
    public StudyViewModel replay(String id) { return run(() -> input.replay(id)); }
}
