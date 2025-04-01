package com.blakersfield.gameagentsystem.panels;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import java.awt.*;

public class ChatPanel extends JPanel{
    private OllamaClient ollamaClient;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    public ChatPanel(CloseableHttpClient httpClient, String apiUrl){
        super(new BorderLayout());
        this.ollamaClient = new OllamaClient(httpClient, apiUrl);
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

                // Clear input immediately for UX
                inputField.setText("");

                // Prepare messages for Ollama
                
                chatMessages.add(new ChatMessage("user", userInput));

                // Start background task
                new Thread(() -> {
                    try {
                        // --- Send request to Ollama ---
                        ChatMessage response = ollamaClient.chat(chatMessages);
                        this.chatMessages.add(response);
                        String responseContent = response.getContent(); // your actual client

                        // --- Save to SQLite (pseudo-code, fill in later) ---
                        // saveToDatabase(userInput, response);

                        // --- Update GUI safely from background thread ---
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append("LLM: " + responseContent + "\n\n");
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append("LLM: (error occurred)\n\n");
                        });
                    }
                }).start();
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
