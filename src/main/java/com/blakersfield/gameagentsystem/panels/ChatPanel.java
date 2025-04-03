package com.blakersfield.gameagentsystem.panels;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class ChatPanel extends JPanel {
    private OllamaClient ollamaClient;
    private SqlLiteDao sqlLiteDao;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private String chatId;
    private JTextPane chatPane;
    private JComboBox<String> chatSelector;

    public ChatPanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, String apiUrl) {
        super(new BorderLayout());
        this.ollamaClient = new OllamaClient(httpClient, apiUrl);
        this.sqlLiteDao = sqlLiteDao;

        this.add(new JLabel("Chat Client"), BorderLayout.NORTH);

        // --- Chat Output ---
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatPane);
        this.add(chatScrollPane, BorderLayout.CENTER);

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        this.add(inputPanel, BorderLayout.SOUTH);

        // --- Chat Control Panel (Top) ---
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        chatSelector = new JComboBox<>();
        toolPanel.add(new JLabel("Select Chat:"));
        toolPanel.add(chatSelector);

        JTextField chatNameField = new JTextField(15);
        JButton updateChatButton = new JButton("Change Chat Name");
        toolPanel.add(chatNameField);
        toolPanel.add(updateChatButton);
        this.add(toolPanel, BorderLayout.NORTH);

        updateChatSelector(); //refresh selector each time
        if (chatId == null) {
            chatId = UUID.randomUUID().toString();
        }

        // --- Input Logic ---
        Runnable sendAction = () -> {
            String userInput = inputField.getText().trim();
            if (!userInput.isEmpty()) {
                ChatMessage userMsg = new ChatMessage("user", userInput);
                chatMessages.add(userMsg);
                sqlLiteDao.saveChatMessage(userMsg, chatId);
                renderChatMessage(userMsg);
                inputField.setText("");

                new Thread(() -> {
                    try {
                        ChatMessage response = ollamaClient.chat(chatMessages);
                        chatMessages.add(response);
                        sqlLiteDao.saveChatMessage(response, chatId);

                        SwingUtilities.invokeLater(() -> renderChatMessage(response));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> renderSystemMessage("LLM: (error occurred)\n"));
                    }
                }).start();
            }
        };

        sendButton.addActionListener(e -> sendAction.run());
        inputField.addActionListener(e -> sendAction.run());

        // --- Chat ID Rename Logic ---
        updateChatButton.addActionListener(e -> {
            String newId = chatNameField.getText().trim();
            if (!newId.isEmpty()) {
                sqlLiteDao.updateChatId(chatId, newId);
                chatId = newId;
                updateChatSelector();
            }
        });

        // --- Chat Selection Logic ---
        chatSelector.addActionListener(e -> {
            String selected = (String) chatSelector.getSelectedItem();
            if (selected != null && !selected.equals(chatId)) {
                loadAndRenderChatHistory(selected);
            }
        });

        // Load initial chat
        loadAndRenderChatHistory(chatId);
    }

    private void renderChatMessage(ChatMessage message) {
        StyledDocument doc = chatPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();

        if ("user".equals(message.getRole())) {
            StyleConstants.setForeground(style, new Color(0, 51, 153)); //  blue text for user
            appendToPane(doc, "You: " + message.getContent() + "\n\n", style);
        } else {
            StyleConstants.setForeground(style, Color.BLACK);
            appendToPane(doc, "LLM: " + message.getContent() + "\n\n", style);
        }
    }

    private void appendToPane(StyledDocument doc, String msg, AttributeSet style) {
        try {
            doc.insertString(doc.getLength(), msg, style);
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void renderSystemMessage(String message) {
        StyledDocument doc = chatPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, Color.GRAY);
        appendToPane(doc, message, style);
    }

    private void loadAndRenderChatHistory(String chatId) {
        this.chatId = chatId;
        this.chatMessages = this.sqlLiteDao.getChatMessagesById(chatId);
        chatPane.setText("");
        for (ChatMessage msg : chatMessages) {
            renderChatMessage(msg);
        }
    }

    private void updateChatSelector() {
        Set<String> ids = new LinkedHashSet<>(sqlLiteDao.getChatIds());
        ids.remove(chatId); 
        List<String> finalList = new ArrayList<>();
        finalList.add(chatId);
        finalList.addAll(ids);
    
        chatSelector.removeAllItems();
        for (String id : finalList) {
            chatSelector.addItem(id);
        }
        chatSelector.setSelectedItem(chatId);
    }
}
