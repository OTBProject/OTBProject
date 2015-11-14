package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.App;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.net.URL;
import java.util.Objects;

public class GuiUtils {
    public static void runSafe(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");

        // Attempt to prevent NPE by queueing Runnables if GuiApplication
        // not ready
        if (GuiApplication.notReady()) {
            GuiApplication.READY_LOCK.lock();
            try {
                if (GuiApplication.notReady()) {
                    GuiApplication.NOT_READY_QUEUE.add(runnable);
                    return;
                }
            } finally {
                GuiApplication.READY_LOCK.unlock();
            }
        }

        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void setDefaultButton(Alert alert, ButtonType defBtn) {
        DialogPane pane = alert.getDialogPane();
        alert.getButtonTypes().forEach(t -> ((Button) pane.lookupButton(t)).setDefaultButton(t == defBtn));
    }

    public static void setDialogPaneStyle(DialogPane dialogPane) {
        URL resource = GuiApplication.class.getClassLoader().getResource("style.css");
        if (resource != null) {
            dialogPane.getStylesheets().add(resource.toExternalForm());
        } else {
            App.logger.error("Unable to get style sheet for alerts.");
        }
    }
}
