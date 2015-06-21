package com.github.otbproject.otbproject.gui;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

public class Window extends JFrame implements ActionListener {
    private final JPanel pnlButtons = new JPanel();
    private final JPanel pnlText = new JPanel();
    private final JButton close = new JButton("Close Window");
    private final JButton exit = new JButton("Stop Bot");

    private final TextNote text = new TextNote("OTB does not currently have a graphical interface. \nAs such, if you close this window " +
            "without stopping the bot, you may find it difficult to stop later if you wish to do so. \nIf you are not familiar " +
            "and comfortable with using a terminal, you should probably leave this window open until you wish to stop the bot. \n\n" +
            "The PID of the bot is probably " + App.PID + ", if you are using an Oracle JVM, but it may be different, especially if you are using a " +
            "different JVM. Be careful stopping the bot using this PID.");

    public Window() {
        super("OTBProject");

        setSize(400, 280);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pnlText.add(text);
        pnlButtons.add(close);
        pnlButtons.add(exit);
        getContentPane().add(BorderLayout.CENTER, pnlText);
        getContentPane().add(BorderLayout.SOUTH, pnlButtons);
        setVisible(true);
        close.addActionListener(this);
        exit.addActionListener(this);
        addWindowListener(this.new Closing());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource().equals(close)) {
            hideDialog();
        } else if (event.getSource().equals(exit)) {
            stopDialog();
        }
    }

    private static class TextNote extends JTextArea {
        public TextNote(String text) {
            super(text);
            setBackground(null);
            setEditable(false);
            setBorder(null);
            setLineWrap(true);
            setWrapStyleWord(true);
            setFocusable(false);
            setOpaque(false);
            setPreferredSize(new Dimension(350, 200));
        }
    }

    private void hideDialog() {
        String confirmMsg = "WARNING: THIS DOES NOT STOP THE BOT.\nClosing this window may make it difficult\nto stop the bot.\nPress \"Cancel\" to keep the window open.";
        String title = "Confirm Close Without Stopping Bot";
        if (JOptionPane.showConfirmDialog(this, confirmMsg, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            this.setVisible(false);
        }
    }

    private void stopDialog() {
        String confirmMsg = "This will stop the bot from running.\nPress \"Cancel\" to continue running the bot.";
        String title = "Confirm Stop Bot";
        if (JOptionPane.showConfirmDialog(this, confirmMsg, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            if (Bot.getBot() != null && Bot.getBot().isConnected()) {
                Bot.getBot().shutdown();
            }
            System.exit(0);
        }
    }

    private class Closing extends WindowAdapter {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            hideDialog();
        }
    }
}
