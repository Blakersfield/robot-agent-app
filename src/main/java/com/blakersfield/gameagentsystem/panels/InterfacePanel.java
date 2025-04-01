package com.blakersfield.gameagentsystem.panels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class InterfacePanel extends JPanel{
    public InterfacePanel(){
        super(new BorderLayout());
        this.add(new JLabel("LLM ROS Interface Panel"));
    
        // --- Chat Output Area ---
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        this.add(chatScrollPane, BorderLayout.CENTER);
    
        // --- Input Field (Bottom) ---
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
    
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        this.add(inputPanel, BorderLayout.SOUTH);
    
        // --- Side Tools (Top or Side) ---
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton dbQueryButton = new JButton("Run DB Query");
        toolPanel.add(dbQueryButton);
        this.add(toolPanel, BorderLayout.NORTH);
    
        // --- Logic ---
        sendButton.addActionListener(e -> {
            String userInput = inputField.getText().trim();
            if (!userInput.isEmpty()) {
                chatArea.append("You: " + userInput + "\n");
                // Placeholder LLM response
                chatArea.append("LLM: (pretend response here)\n\n");
                inputField.setText("");
            }
        });
    
        dbQueryButton.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:app.db");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT sqlite_version();")) {
    
                while (rs.next()) {
                    String version = rs.getString(1);
                    chatArea.append("System: SQLite version is " + version + "\n\n");
                }
            } catch (SQLException ex) {
                chatArea.append("Error querying DB: " + ex.getMessage() + "\n\n");
            }
        });
    }
}
