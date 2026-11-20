package com.wordtrail.adapter;

import com.wordtrail.usecase.AppException;
import java.util.function.Supplier;

/** Keeps expected application failures within the UI adapter boundary. */
final class ControllerCalls {
    private ControllerCalls() { }

    static <T> T call(Supplier<T> action) {
        try { return action.get(); }
        catch (AppException ex) { throw new UiException(ex.getMessage(), ex); }
    }
}
