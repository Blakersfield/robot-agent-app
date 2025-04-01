package com.blakersfield.gameagentsystem.panels;

import java.sql.*;
import javax.swing.*;

import org.apache.http.impl.client.CloseableHttpClient;

import java.awt.*;

public class ChatPanel extends JPanel{
    public ChatPanel(CloseableHttpClient httpClient){
        super(new BorderLayout());
        this.add(new JLabel("Chat Client"));
    
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
