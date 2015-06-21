package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.fs.FSUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;

public class GuiApplication extends Application {


    private static GuiController controller;
    private Tailer tailer;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getClassLoader().getResourceAsStream("UbuntuMono-R.ttf"),12);
        Font.loadFont(getClass().getClassLoader().getResourceAsStream("Ubuntu-R.ttf"),12);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("console.fxml"));
        Parent start = loader.load();
        primaryStage.setScene(new Scene(start, 1200, 500));
        primaryStage.setResizable(false);
        primaryStage.setTitle("OTBProject");
        primaryStage.getIcons().add(new Image("http://otbproject.github.io/images/logo.png"));
        primaryStage.setOnCloseRequest(t -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("WARNING: \"Close Window\" DOES NOT STOP THE BOT.");
            alert.setContentText("Closing this window without exiting may make it difficult to stop the bot.\nPress \"Exit\" to stop the bot and exit.\nPress \"Cancel\" to keep the window open.");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());
            ButtonType buttonTypeCloseNoExit = new ButtonType("Close Window", ButtonBar.ButtonData.LEFT);
            ButtonType buttonTypeExit = new ButtonType("Exit", ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.FINISH);
            alert.getButtonTypes().setAll(buttonTypeCloseNoExit, buttonTypeExit, buttonTypeCancel);
            GuiUtils.setDefaultButton(alert, buttonTypeExit);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeCloseNoExit) {
                    primaryStage.hide();
                    tailer.stop();
                } else if (buttonType == buttonTypeExit) {
                    App.logger.info("Stopping the process");
                    if (Bot.getBot() != null && Bot.getBot().isConnected()) {
                        Bot.getBot().shutdown();
                    }
                    App.logger.info("Process Stopped, Goodbye");
                    System.exit(0);
                } else {
                    t.consume();
                }
            });

        });
        controller = loader.<GuiController>getController();
        controller.cliOutput.appendText(">  ");
        controller.commandsInput.setEditable(false);
        controller.commandsOutput.appendText("Type \"help\" for list of commands.\nThe PID of the bot is probably " + App.PID + ", if you are using an Oracle JVM, but it may be different, especially if you are using a different JVM. Be careful stopping the bot using this PID.");
        File logFile = new File(FSUtil.logsDir() + File.separator + "console.log");
        tailer = Tailer.create(logFile, new CustomTailer(), 250);
        primaryStage.show();
    }

    public static void start(String[] args) {
        launch(args);
    }

    public static void setInputActive() {
        GuiUtils.runSafe(() -> {
            controller.commandsInput.setEditable(true);
            controller.commandsInput.setPromptText("Enter command here...");
        });
    }

    public static void addInfo(String text) {
        GuiUtils.runSafe(() -> controller.commandsOutput.appendText("\n\n" + text));
    }

    public static void clearLog() {
        GuiUtils.runSafe(controller.logOutput::clear);
    }

    public static void clearInfo() {
        GuiUtils.runSafe(controller.commandsOutput::clear);
    }

    public static void clearCliOutput() {
        GuiUtils.runSafe(controller.cliOutput::clear);
    }

    public static void clearHistory() {
        GuiUtils.runSafe(() -> {
            controller.history.clear();
            controller.historyPointer = 0;
        });
    }

    class CustomTailer extends TailerListenerAdapter {
        @Override
        public void handle(String line) {
            GuiUtils.runSafe(() -> controller.logOutput.appendText(line + "\n"));
        }
    }
}
