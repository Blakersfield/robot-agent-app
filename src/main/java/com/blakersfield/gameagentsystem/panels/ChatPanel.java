package com.blakersfield.gameagentsystem.panels;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;

import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.*;
import com.blakersfield.gameagentsystem.llm.model.node.InputNode;
import com.blakersfield.gameagentsystem.llm.model.node.NodeChainBuilder;
import com.blakersfield.gameagentsystem.llm.model.node.agent.BasicChatAgent;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class ChatPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChatPanel.class);
    protected LLMClient llmClient;
    protected SqlLiteDao sqlLiteDao;
    protected List<ChatMessage> chatMessages = new ArrayList<>();
    private NodeChainBuilder chain;
    protected JTextPane chatPane;
    protected JScrollPane chatScrollPane;
    protected JTextField inputField;
    protected JButton sendButton;
    protected JComboBox<String> chatSelector;
    protected JTextField chatNameField;
    protected JButton updateChatButton;
    protected JButton newChatButton;

    public ChatPanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, LLMClient llmClient) {
        super(new BorderLayout());
        this.llmClient = llmClient;
        this.sqlLiteDao = sqlLiteDao;
        this.chain = initChain();
        if (sqlLiteDao.getCurrentChatId() == null) {
            sqlLiteDao.setCurrentChatId(UUID.randomUUID().toString());
        }

        initializeLayout();
        initializeChatDisplay();
        initializeToolbar();
        initializeInputArea();
        initializeListeners();

        loadAndRenderChatHistory(sqlLiteDao.getCurrentChatId());
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
        JButton logButton = new JButton("Logs");

        toolPanel.add(chatNameField);
        toolPanel.add(updateChatButton);
        toolPanel.add(newChatButton);
        toolPanel.add(logButton);

        add(toolPanel, BorderLayout.NORTH);

        updateChatSelector();

        logButton.addActionListener(e -> {
            LogViewerPanel logViewer = new LogViewerPanel((Frame) SwingUtilities.getWindowAncestor(this), "logs/chat-panel.log");
            logViewer.setVisible(true);
        });
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
                sqlLiteDao.updateChatId(sqlLiteDao.getCurrentChatId(), newId);
                sqlLiteDao.setCurrentChatId(newId);
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
        try {
            sqlLiteDao.saveChatMessage(userMsg, sqlLiteDao.getCurrentChatId());
            logger.debug("Saved user message to chat {}", sqlLiteDao.getCurrentChatId());
        } catch (Exception e) {
            logger.error("Failed to save user message", e);
        }
        renderChatMessage(userMsg);
        inputField.setText("");

        new Thread(() -> {
            try {
                chain.execute(chatMessages);
                ChatMessage response = (ChatMessage) chain.getLastOutput();
                chatMessages.add(response);
                try {
                    sqlLiteDao.saveChatMessage(response, sqlLiteDao.getCurrentChatId());
                    logger.debug("Saved LLM response to chat {}", sqlLiteDao.getCurrentChatId());
                } catch (Exception e) {
                    logger.error("Failed to save LLM response", e);
                }
                SwingUtilities.invokeLater(() -> renderChatMessage(response));
            } catch (Exception ex) {
                logger.error("Error processing chat message", ex);
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
        try {
            List<ChatMessage> loadedMessages = sqlLiteDao.getChatMessagesById(chatId);
            chatPane.setText("");
            if (loadedMessages != null && !loadedMessages.isEmpty()) {
                sqlLiteDao.setCurrentChatId(chatId);
                this.chatMessages = loadedMessages;
                logger.info("Loaded chat history for chat {}", chatId);
                for (ChatMessage msg : chatMessages) {
                    renderChatMessage(msg);
                }
            }
            chatPane.repaint();
        } catch (Exception e) {
            logger.error("Failed to load chat history for chat {}", chatId, e);
            renderSystemMessage("Error loading chat history\n");
        }
    }

    protected void updateChatSelector() {
        Set<String> ids = new LinkedHashSet<>(sqlLiteDao.getChatIds());
        ids.remove(sqlLiteDao.getCurrentChatId());
        List<String> finalList = new ArrayList<>();
        finalList.add(sqlLiteDao.getCurrentChatId());
        finalList.addAll(ids);

        chatSelector.removeAllItems();
        for (String id : finalList) {
            chatSelector.addItem(id);
        }
        chatSelector.setSelectedItem(sqlLiteDao.getCurrentChatId());
    }

    protected void startNewChat() {
        this.chatMessages = new ArrayList<>();
        String newChatId = UUID.randomUUID().toString();
        sqlLiteDao.setCurrentChatId(newChatId);
        logger.info("Started new chat with ID: {}", newChatId);
        chatSelector.setSelectedItem(newChatId);
        updateChatSelector();
        chatPane.setText("");
        chatPane.repaint();
    }

    private NodeChainBuilder initChain(){
        this.chain = NodeChainBuilder.<List<ChatMessage>, ChatMessage>create();
        chain = chain.add(new InputNode<List<ChatMessage>, List<ChatMessage>>())
            .add(new BasicChatAgent(llmClient));            
        return chain;
    }
}