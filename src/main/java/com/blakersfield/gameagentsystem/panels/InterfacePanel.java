package com.blakersfield.gameagentsystem.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.model.node.InputNode;
import com.blakersfield.gameagentsystem.llm.model.node.NodeChainBuilder;
import com.blakersfield.gameagentsystem.llm.model.node.agent.GameActionAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.InputTypeInterpreterAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.RuleExtractionAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Choice;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

import java.util.List;

public class InterfacePanel extends ChatPanel {
    private static final Logger logger = LoggerFactory.getLogger(InterfacePanel.class);
    private NodeChainBuilder chain;

    public InterfacePanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, LLMClient llmClient) {
        super(httpClient, sqlLiteDao, llmClient);
        initializeLangChain();
    }

    private void initializeLangChain() {
        // Create agents
        GameActionAgent gameActionAgent = new GameActionAgent(llmClient);
        RuleExtractionAgent ruleExtractionAgent = new RuleExtractionAgent(llmClient, sqlLiteDao);

        // Create choices for input type interpreter
        List<Choice> choices = List.of(
            new Choice("action", "Process a game action", gameActionAgent),
            new Choice("rule", "Extract game rules", ruleExtractionAgent)
        );

        InputTypeInterpreterAgent interpreterAgent = new InputTypeInterpreterAgent(choices, llmClient);

        // Build the langchain
        chain = NodeChainBuilder.<String, String>create()
            .add(new InputNode<String, String>())
            .add(interpreterAgent);
    }

    @Override
    protected void initializeToolbar() {
        super.initializeToolbar();
        JButton logButton = new JButton("View Logs");
        logButton.addActionListener(e -> {
            LogViewerPanel logViewer = new LogViewerPanel((Frame) SwingUtilities.getWindowAncestor(this), "logs/interface-panel.log");
            logViewer.setVisible(true);
        });
        
        // Get the toolbar panel which is the first component in the NORTH position
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getLayout() instanceof FlowLayout) {
                ((JPanel) comp).add(logButton);
                break;
            }
        }
    }

    @Override
    protected void handleInputSubmission(String userInput) {
        if (userInput.isEmpty()) return;

        ChatMessage userMsg = new ChatMessage("user", userInput);
        chatMessages.add(userMsg);
        sqlLiteDao.saveChatMessage(userMsg, chatId);
        renderChatMessage(userMsg);
        inputField.setText("");

        new Thread(() -> {
            try {
                // this.chain.build().setInput(userInput);
                logger.debug("InterfacePanel: Processing user input: {}", userInput);
                chain.execute(userInput);
                String response = (String) chain.getLastOutput();
                logger.debug("InterfacePanel: Generated response: {}", response);
                
                // Create and display system response
                ChatMessage systemMsg = new ChatMessage("system", response);
                chatMessages.add(systemMsg);
                sqlLiteDao.saveChatMessage(systemMsg, chatId);
                SwingUtilities.invokeLater(() -> renderChatMessage(systemMsg));
            } catch (Exception ex) {
                logger.error("InterfacePanel: Error processing input", ex);
                SwingUtilities.invokeLater(() -> renderSystemMessage("Error processing input: " + ex.getMessage() + "\n"));
            }
        }).start();
    }
}
