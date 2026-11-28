package com.wordtrail.adapter;
import com.wordtrail.usecase.BackupUseCase;
public final class BackupController {
    private final BackupUseCase input;
    public BackupController(BackupUseCase input) { this.input = input; }
    public void exportTo(String destination) {
        ControllerCalls.call(() -> { input.exportTo(destination); return true; });
    }
    public String inspect(String source) { return ControllerCalls.call(() -> input.inspect(source)); }
    public String restore(String source, String digest, boolean confirmed) {
        return ControllerCalls.call(() -> input.restore(source, digest, confirmed));
    }
}
