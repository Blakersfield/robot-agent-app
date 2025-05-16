package com.blakersfield.gameagentsystem.panels;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class LogViewerPanel extends JDialog {
    private JTextArea logArea;
    private Timer refreshTimer;
    private String logFilePath;

    public LogViewerPanel(Frame parent, String logFilePath) {
        super(parent, "Log Viewer", false);
        this.logFilePath = logFilePath;
        initializeUI();
    }

    private void initializeUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        refreshButton.addActionListener(e -> refreshLogs());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // check logfile periodically
        refreshTimer = new Timer(5000, e -> refreshLogs());
        refreshTimer.start();

        refreshLogs();

        // stop timer when window is lcosed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                refreshTimer.stop();
            }
        });

        add(mainPanel);
    }

    private void refreshLogs() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath));
            String content = lines.stream()
                .collect(Collectors.joining("\n"));
            logArea.setText(content);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } catch (IOException e) {
            logArea.setText("Error reading log file: " + e.getMessage());
        }
    }
} 