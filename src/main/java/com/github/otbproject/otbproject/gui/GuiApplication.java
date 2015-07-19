package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.Util;
import com.github.otbproject.otbproject.util.version.AppVersion;
import com.github.otbproject.otbproject.util.version.Version;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

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
        Font.loadFont(getClass().getClassLoader().getResourceAsStream("UbuntuMono-R.ttf"), 12);
        Font.loadFont(getClass().getClassLoader().getResourceAsStream("Ubuntu-R.ttf"), 12);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("console.fxml"));
        Parent start = loader.load();
        primaryStage.setScene(new Scene(start, 1200, 515));
        primaryStage.setResizable(false);
        primaryStage.setTitle("OTB");
        primaryStage.getIcons().add(new Image("http://otbproject.github.io/images/logo.png"));
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("WARNING: \"Close Window\" DOES NOT STOP THE BOT.");
            alert.setContentText("Closing this window without exiting may make it difficult to stop the bot.\nPress \"Exit\" to stop the bot and exit.\nPress \"Cancel\" to keep the window open.");
            DialogPane dialogPane = alert.getDialogPane();
            setDialogPaneStyle(dialogPane);
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
                    Bot.Control.shutdownAndExit();
                    System.exit(0);
                }
            });
            event.consume();
        });
        controller = loader.<GuiController>getController();
        setUpMenus();
        controller.cliOutput.appendText(">  ");
        controller.commandsInput.setEditable(false);
        controller.commandsOutput.appendText("Type \"help\" for a list of commands.\nThe PID of the bot is probably " + App.PID + ", if you are using an Oracle JVM, but it may be different, especially if you are using a different JVM. Be careful stopping the bot using this PID.");
        File logFile = new File(FSUtil.logsDir() + File.separator + "console.log");
        tailer = Tailer.create(logFile, new CustomTailer(), 250);
        controller.readHistory();
        primaryStage.show();
        checkForNewRelease();
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
            controller.writeHistory();
        });
    }

    static class CustomTailer extends TailerListenerAdapter {
        @Override
        public void handle(String line) {
            GuiUtils.runSafe(() -> controller.logOutput.appendText(line + "\n"));
        }
    }

    private void setUpMenus() {
        controller.openBaseDir.setOnAction(event -> {
            Util.getSingleThreadExecutor("file-explorer-%d").execute(() -> {
                try {
                    Desktop.getDesktop().open(new File(FSUtil.getBaseDir()));
                } catch (IOException e) {
                    App.logger.catching(e);
                }
            });
            event.consume();
        });
        controller.quit.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Quit");
            alert.setHeaderText("Are you sure you want to quit?");
            DialogPane dialogPane = alert.getDialogPane();
            setDialogPaneStyle(dialogPane);
            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.FINISH);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
            GuiUtils.setDefaultButton(alert, buttonTypeYes);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeYes) {
                    Bot.Control.shutdownAndExit();
                }
            });
            event.consume();
        });
        controller.botStart.setOnAction(event -> {
            try {
                addInfo(Bot.Control.startup() ? "Started bot" : "Failed to start bot");
            } catch (Bot.StartupException ignored) {
                addInfo("Did not start bot - bot already running");
            }
            event.consume();
        });
        controller.botStop.setOnAction(event -> {
            addInfo(Bot.Control.shutdown(true) ? "Bot stopped" : "Did not stop bot - bot not running");
            event.consume();
        });
        controller.botRestart.setOnAction(event -> {
            addInfo(Bot.Control.restart() ? "Restarted bot" : "Failed to restart bot");
            event.consume();
        });
        controller.webOpen.setOnAction(event -> {
            openWebInterfaceInBrowser();
            event.consume();
        });
    }

    private void setDialogPaneStyle(DialogPane dialogPane) {
        URL resource = getClass().getClassLoader().getResource("style.css");
        if (resource != null) {
            dialogPane.getStylesheets().add(resource.toExternalForm());
        } else {
            App.logger.error("Unable to get style sheet for alerts.");
        }
    }

    private void openWebInterfaceInBrowser() {
        this.getHostServices().showDocument("http://127.0.0.1:" + Configs.getWebConfig().getPortNumber());
    }

    private void checkForNewRelease() {
        if (Configs.getGeneralConfig().isUpdateChecking()
                && (AppVersion.latest().compareTo(App.VERSION) > 0)
                && (AppVersion.latest().type == Version.Type.RELEASE)) {
            String url = "https://github.com/OTBProject/OTBProject/releases/latest";
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New Release Available");
            alert.setHeaderText("New Release Available: OTB Version " + AppVersion.latest());
            alert.setContentText("Version " + AppVersion.latest() + " of OTB is now available!" +
                    "\n\nPress \"Get New Release\" or go to" +
                    "\n" + url +
                    "\nto get the new release." +
                    "\n\nPressing \"Don't Ask Again\" will prevent notifications " +
                    "\nfor all future releases of OTB.");
            DialogPane dialogPane = alert.getDialogPane();
            setDialogPaneStyle(dialogPane);
            ButtonType buttonTypeDontAskAgain = new ButtonType("Don't Ask Again", ButtonBar.ButtonData.LEFT);
            ButtonType buttonTypeGetRelease = new ButtonType("Get New Release", ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeIgnoreOnce = new ButtonType("Ignore Once", ButtonBar.ButtonData.FINISH);
            alert.getButtonTypes().setAll(buttonTypeDontAskAgain, buttonTypeGetRelease, buttonTypeIgnoreOnce);
            GuiUtils.setDefaultButton(alert, buttonTypeGetRelease);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeDontAskAgain) {
                    Configs.getGeneralConfig().setUpdateChecking(false);
                    Configs.writeGeneralConfig();
                } else if (buttonType == buttonTypeGetRelease) {
                    this.getHostServices().showDocument(url);
                }
            });
        }
    }
}
