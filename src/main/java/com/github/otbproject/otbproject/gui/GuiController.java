package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.cli.commands.CmdParser;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;
import com.github.otbproject.otbproject.util.Util;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class GuiController {

    @FXML
    public TextArea logOutput;
    @FXML
    public TextArea commandsOutput;
    @FXML
    public TextField commandsInput;
    @FXML
    public TextArea cliOutput;

    protected final List<String> history = new ArrayList<>();
    protected int historyPointer = 0;
    private final ExecutorService executorService = Util.getSingleThreadExecutor("CLI Command Processor");

    @FXML
    public void command(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                String input = commandsInput.getText();
                if (input.isEmpty()) {
                    break;
                }
                cliOutput.appendText(input + "\n");
                commandsInput.clear();
                commandsInput.setEditable(false);
                commandsInput.setPromptText("Command executing, please wait...");
                executorService.execute(() -> {
                    CmdParser.from(InternalMessageSender.CLI);
                    String output = CmdParser.processLine(input);
                    GuiUtils.runSafe(() -> cliOutput.appendText((output.isEmpty() ? "" : (output + "\n")) + ">  "));
                    GuiApplication.setInputActive();
                });
                if (history.isEmpty() || !history.get(history.size() - 1).equals(input)) {
                    history.add(input);
                }
                while (history.size() > 1000) {
                    history.remove(0);
                }
                historyPointer = history.size();
                break;
            case UP:
                if (historyPointer == 0) {
                    break;
                }
                --historyPointer;
                commandsInput.setText(history.get(historyPointer));
                commandsInput.positionCaret(commandsInput.getText().length());
                event.consume();
                break;
            case DOWN:
                if (historyPointer == history.size()) {
                    break;
                } else if (historyPointer == history.size() - 1) {
                    commandsInput.clear();
                    historyPointer = history.size();
                    break;
                }
                historyPointer++;
                commandsInput.setText(history.get(historyPointer));
                break;
            case TAB:
                input = commandsInput.getText();
                if (input.isEmpty()) {
                    break;
                }
                String[] parts = input.split(" ");
                if (parts.length == 1) {
                    CmdParser.getCommands().forEach(s -> commandsInput.setText(s.startsWith(parts[0]) ? (s + " ") : commandsInput.getText()));
                }
                if (parts.length == 2 && CmdParser.getCommands().contains(parts[0])) {
                    switch (parts[0]) {
                        case CmdParser.CLEAR:
                            CmdParser.ClearTargets.targets.forEach(s -> commandsInput.setText(s.startsWith(parts[1]) ? (parts[0] + " " + s + " ") : commandsInput.getText()));
                            break;
                        case CmdParser.EXEC:
                        case CmdParser.LEAVECHANNEL:
                        case CmdParser.RESET:
                            Channels.list().forEach(s -> commandsInput.setText(s.startsWith(parts[1]) ? (parts[0] + " " + s + " ") : commandsInput.getText()));
                            break;
                        case CmdParser.HELP:
                            CmdParser.getCommands().forEach(s -> commandsInput.setText(s.startsWith(parts[1]) ? (parts[0] + " " + s + " ") : commandsInput.getText()));
                            break;
                    }
                }
                commandsInput.positionCaret(commandsInput.getText().length());
                break;
            case ESCAPE:
                commandsInput.clear();
                historyPointer = history.size();
                break;
        }
    }
}
