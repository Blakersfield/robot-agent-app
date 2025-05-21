package com.blakersfield.gameagentsystem.panels;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerPanel extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(LogViewerPanel.class);
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

        // stop timer when window is closed
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
            Path logPath = Paths.get(logFilePath);
            Path logDir = logPath.getParent();
            
            // Create logs directory if it doesn't exist
            if (logDir != null && !Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("Created logs directory: {}", logDir);
            }
            
            // Create empty log file if it doesn't exist
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
                logger.info("Created new log file: {}", logPath);
            }
            
            // Check if file is empty or too small
            if (Files.size(logPath) <= 1) {
                logArea.setText("Log file is empty. New logs will appear here.");
                return;
            }
            
            try {
                List<String> lines = Files.readAllLines(logPath);
                if (lines.isEmpty()) {
                    logArea.setText("Log file is empty. New logs will appear here.");
                } else {
                    String content = lines.stream()
                        .collect(Collectors.joining("\n"));
                    logArea.setText(content);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            } catch (IOException e) {
                // If we can't read the file, try to recreate it
                logger.warn("Error reading log file, attempting to recreate: {}", logFilePath, e);
                Files.deleteIfExists(logPath);
                Files.createFile(logPath);
                logArea.setText("Log file was recreated. New logs will appear here.");
            }
        } catch (IOException e) {
            logger.error("Error accessing log file: {}", logFilePath, e);
            logArea.setText("Error accessing log file: " + e.getMessage() + 
                "\n\nPlease ensure the logs directory exists and is writable." +
                "\nPath: " + logFilePath);
        }
    }
} 