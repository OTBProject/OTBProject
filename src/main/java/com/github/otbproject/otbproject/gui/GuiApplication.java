package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.api.APIBot;
import com.github.otbproject.otbproject.fs.FSUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.util.Optional;

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
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("console.fxml"));
        Parent start = loader.load();
        primaryStage.setScene(new Scene(start, 1200, 500));
        primaryStage.setResizable(false);
        primaryStage.setTitle("OTBProject");
        primaryStage.getIcons().add(new Image("http://otbproject.github.io/images/logo.png"));
        primaryStage.setOnCloseRequest(t -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Close Without Stopping Bot");
            alert.setHeaderText("WARNING: THIS DOES NOT STOP THE BOT.");
            alert.setContentText("Closing this window may make it difficult to stop the bot.\nPress \"Cancel\" to keep the window open.");
            ButtonType buttonTypeOk = new ButtonType("Close", ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeStop = new ButtonType("Stop Bot", ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeStop, buttonTypeCancel);
            alert = GuiUtils.setDefaultButton(alert, buttonTypeStop);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeOk) {
                primaryStage.hide();
            } else if (result.get() == buttonTypeStop) {
                App.logger.info("Stopping the process");
                if (APIBot.getBot() != null && APIBot.getBot().isConnected()) {
                    APIBot.getBot().shutdown();
                }
                App.logger.info("Process Stopped, Goodbye");
                System.exit(0);
            } else {
                alert.close();
            }
        });
        primaryStage.show();
        controller = loader.<GuiController>getController();
        controller.cliOutput.appendText(">  ");
        controller.commandsInput.setEditable(false);
        controller.commandsOutput.appendText("Type \"stop\" to stop the bot. \nThe PID of the bot is probably " + App.PID + ", if you are using an Oracle JVM, but it may be different, especially if you are using a different JVM. Be careful stopping the bot using this PID. \n");
        File logFile = new File(FSUtil.logsDir() + File.separator + "console.log");
        tailer = Tailer.create(logFile, new CustomTailer(), 250);
    }

    public static void start(String[] args) {
        launch(args);
    }

    public static void setInputActive() {
        controller.commandsInput.setEditable(true);
    }

    class CustomTailer extends TailerListenerAdapter {
        @Override
        public void handle(String line) {
            GuiUtils.runSafe(() -> controller.logOutput.appendText(line + "\n"));
        }
    }
}
