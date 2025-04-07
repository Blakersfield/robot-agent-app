package com.blakersfield.gameagentsystem.panels;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.*;
import javax.swing.text.*;

import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class ChatPanel extends JPanel {
    protected OllamaClient ollamaClient;
    protected SqlLiteDao sqlLiteDao;
    protected List<ChatMessage> chatMessages = new ArrayList<>();
    protected String chatId;

    protected JTextPane chatPane;
    protected JScrollPane chatScrollPane;
    protected JTextField inputField;
    protected JButton sendButton;
    protected JComboBox<String> chatSelector;
    protected JTextField chatNameField;
    protected JButton updateChatButton;
    protected JButton newChatButton;

    public ChatPanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, String apiUrl) {
        super(new BorderLayout());
        this.ollamaClient = new OllamaClient(httpClient, apiUrl);
        this.sqlLiteDao = sqlLiteDao;

        if (chatId == null) {
            chatId = UUID.randomUUID().toString();
        }

        initializeLayout();
        initializeChatDisplay();
        initializeToolbar();
        initializeInputArea();
        initializeListeners();

        loadAndRenderChatHistory(chatId);
    }

    protected void initializeLayout() {
        setLayout(new BorderLayout());
    }

    protected void initializeChatDisplay() {
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatScrollPane = new JScrollPane(chatPane);
        add(chatScrollPane, BorderLayout.CENTER);
    }

    protected void initializeToolbar() {
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        chatSelector = new JComboBox<>();
        toolPanel.add(new JLabel("Select Chat:"));
        toolPanel.add(chatSelector);

        chatNameField = new JTextField(15);
        updateChatButton = new JButton("Change Chat Name");
        newChatButton = new JButton("New Chat");

        toolPanel.add(chatNameField);
        toolPanel.add(updateChatButton);
        toolPanel.add(newChatButton);

        add(toolPanel, BorderLayout.NORTH);

        updateChatSelector();
    }

    protected void initializeInputArea() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    protected void initializeListeners() {
        Runnable sendAction = () -> handleInputSubmission(inputField.getText().trim());

        sendButton.addActionListener(e -> sendAction.run());
        inputField.addActionListener(e -> sendAction.run());

        updateChatButton.addActionListener(e -> {
            String newId = chatNameField.getText().trim();
            if (!newId.isEmpty()) {
                sqlLiteDao.updateChatId(chatId, newId);
                chatId = newId;
                updateChatSelector();
            }
        });

        newChatButton.addActionListener(e -> startNewChat());

        chatSelector.addActionListener(e -> {
            String selected = (String) chatSelector.getSelectedItem();
            loadAndRenderChatHistory(selected);
        });
    }

    protected void handleInputSubmission(String userInput) {
        if (userInput.isEmpty()) return;

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

    protected void renderChatMessage(ChatMessage message) {
        StyledDocument doc = chatPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();

        if ("user".equals(message.getRole())) {
            StyleConstants.setForeground(style, new Color(0, 51, 153));
            appendToPane(doc, "You: " + message.getContent() + "\n\n", style);
        } else {
            StyleConstants.setForeground(style, Color.BLACK);
            appendToPane(doc, "LLM: " + message.getContent() + "\n\n", style);
        }
    }

    protected void renderSystemMessage(String message) {
        StyledDocument doc = chatPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, Color.RED);
        appendToPane(doc, message, style);
    }

    protected void appendToPane(StyledDocument doc, String msg, AttributeSet style) {
        try {
            doc.insertString(doc.getLength(), msg, style);
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    protected void loadAndRenderChatHistory(String chatId) {
        List<ChatMessage> loadedMessages = sqlLiteDao.getChatMessagesById(chatId);
        chatPane.setText("");
        if (loadedMessages != null && !loadedMessages.isEmpty()) {
            this.chatId = chatId;
            this.chatMessages = loadedMessages;
            for (ChatMessage msg : chatMessages) {
                renderChatMessage(msg);
            }
        }
        chatPane.repaint();
    }

    protected void updateChatSelector() {
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

    protected void startNewChat() {
        this.chatMessages = new ArrayList<>();
        this.chatId = UUID.randomUUID().toString();
        chatSelector.setSelectedItem(this.chatId);
        updateChatSelector();
        chatPane.setText("");
        chatPane.repaint();
    }
}