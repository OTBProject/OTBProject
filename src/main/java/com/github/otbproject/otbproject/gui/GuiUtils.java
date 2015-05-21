package com.github.otbproject.otbproject.gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Objects;

public class GuiUtils {
    public static void runSafe(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static Alert setDefaultButton(Alert alert, ButtonType defBtn) {
        DialogPane pane = alert.getDialogPane();
        alert.getButtonTypes().forEach(t -> ((Button) pane.lookupButton(t)).setDefaultButton(t == defBtn));
        return alert;
    }
}
