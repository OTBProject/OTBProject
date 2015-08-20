package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.github.otbproject.otbproject.util.version.AppVersion;
import com.github.otbproject.otbproject.util.version.Version;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
        Thread.currentThread().setUncaughtExceptionHandler(ThreadUtil.UNCAUGHT_EXCEPTION_HANDLER);
        Font.loadFont(getClass().getClassLoader().getResourceAsStream("assets/fonts/UbuntuMono-R.ttf"), 12);
        Font.loadFont(getClass().getClassLoader().getResourceAsStream("assets/fonts/Ubuntu-R.ttf"), 12);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("console.fxml"));
        Parent start = loader.load();
        primaryStage.setScene(new Scene(start, 1200, 515));
        primaryStage.setResizable(false);
        primaryStage.setTitle("OTB");
        primaryStage.getIcons().add(new Image("file://" + FSUtil.assetsDir() + File.separator + FSUtil.Assets.LOGO));
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("WARNING: \"Close Window\" DOES NOT STOP THE BOT.");
            alert.setContentText("Closing this window without exiting may make it difficult to stop the bot.\nPress \"Exit\" to stop the bot and exit.\nPress \"Cancel\" to keep the window open.");
            DialogPane dialogPane = alert.getDialogPane();
            GuiUtils.setDialogPaneStyle(dialogPane);
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
                    Control.shutdownAndExit();
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
            ThreadUtil.getSingleThreadExecutor("file-explorer-%d").execute(() -> {
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
            GuiUtils.setDialogPaneStyle(dialogPane);
            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.FINISH);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
            GuiUtils.setDefaultButton(alert, buttonTypeYes);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeYes) {
                    Control.shutdownAndExit();
                }
            });
            event.consume();
        });
        controller.botStart.setOnAction(event -> {
            try {
                addInfo(Control.startup() ? "Started bot" : "Did not start bot - bot already running");
            } catch (Control.StartupException ignored) {
                addInfo("Failed to start bot");
            }
            event.consume();
        });
        controller.botStop.setOnAction(event -> {
            addInfo(Control.shutdown(true) ? "Bot stopped" : "Did not stop bot - bot not running");
            event.consume();
        });
        controller.botRestart.setOnAction(event -> {
            addInfo(Control.restart() ? "Restarted bot" : "Failed to restart bot");
            event.consume();
        });
        controller.webOpen.setOnAction(event -> {
            openWebInterfaceInBrowser();
            event.consume();
        });
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
            GuiUtils.setDialogPaneStyle(dialogPane);
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

    public static void errorAlert(String title, String header) {
        GuiUtils.runSafe(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            String url = "https://github.com/OTBProject/OTBProject/issues";

            FlowPane outer = new FlowPane();
            FlowPane inner = new FlowPane();
            Hyperlink link = new Hyperlink("here");
            link.setOnAction((evt) -> ThreadUtil.getSingleThreadExecutor("open-log-dir").execute(() -> {
                try {
                    Desktop.getDesktop().open(new File(FSUtil.logsDir()));
                } catch (IOException e) {
                    App.logger.catching(e);
                }
            }));
            outer.getChildren().addAll(new Label("Please report this problem to the developers at"), new Label('"' + url + '"'), inner,
                    new Label("(in the \"logs\" folder in your installation directory)"));
            inner.getChildren().addAll(new Label("and give them the log file \"app.log\" found"), link);
            alert.getDialogPane().contentProperty().set(outer);

            showErrorAlert(alert, url);
        });
    }

    // TODO fix
    public static void fatalErrorAlert(String fileName) {
        GuiUtils.runSafe(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fatal Error");
            alert.setHeaderText("OTB has experienced a fatal error");
            String url = "https://github.com/OTBProject/OTBProject/issues";
            alert.setContentText("Please report this problem to the developers at\n\"" + url + "\"\nand provide them with the file: " + fileName);
            showErrorAlert(alert, url);
        });
    }

    private static void showErrorAlert(Alert alert, String url) {
        DialogPane dialogPane = alert.getDialogPane();
        GuiUtils.setDialogPaneStyle(dialogPane);
        ButtonType buttonTypeReportIssue = new ButtonType("Report Problem", ButtonBar.ButtonData.FINISH);
        ButtonType buttonTypeClose = new ButtonType("OK", ButtonBar.ButtonData.FINISH);
        alert.getButtonTypes().setAll(buttonTypeReportIssue, buttonTypeClose);
        GuiUtils.setDefaultButton(alert, buttonTypeReportIssue);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeReportIssue && Desktop.isDesktopSupported()) {
                ThreadUtil.getSingleThreadExecutor("report-issue").execute(() -> {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e) {
                        App.logger.catching(e);
                    }
                });
            }
        });
    }
}
