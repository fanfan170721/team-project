package com.wordtrail.view;

import com.wordtrail.adapter.UiException;
import java.awt.Component;
import java.awt.Container;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Serializes background work and restores each control's previous enabled state. */
final class UiTasks {
    private final Container root;
    private final Consumer<String> message;
    private volatile boolean busy;

    UiTasks(Container root, Consumer<String> message) { this.root = root; this.message = message; }
    boolean isBusy() { return busy; }
    void message(String text) { message.accept(text); }

    <T> void run(String description, Supplier<T> work, Consumer<T> onSuccess) {
        if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("UI tasks must start on the EDT."); }
        if (busy) { return; }
        busy = true;
        message.accept(description + "...");
        Map<Component, Boolean> enabled = new LinkedHashMap<>();
        capture(root, enabled);
        enabled.keySet().forEach(component -> component.setEnabled(false));
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() { return work.get(); }
            @Override protected void done() {
                enabled.forEach(Component::setEnabled);
                busy = false;
                try {
                    T result = get();
                    message.accept("Ready");
                    onSuccess.accept(result);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    message.accept("Operation interrupted. Please try again.");
                }
                catch (ExecutionException ex) { report(ex.getCause()); }
                catch (RuntimeException ex) { report(ex); }
            }
        }.execute();
    }

    private void report(Throwable failure) {
        if (failure instanceof UiException) { message.accept(failure.getMessage()); }
        else {
            Logger.getLogger(UiTasks.class.getName()).log(Level.SEVERE, "Unexpected application failure", failure);
            message.accept("An unexpected error occurred. See the console for details.");
        }
    }

    private void capture(Component component, Map<Component, Boolean> enabled) {
        enabled.put(component, component.isEnabled());
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) { capture(child, enabled); }
        }
    }
}
