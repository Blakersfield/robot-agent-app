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
import com.blakersfield.gameagentsystem.llm.model.node.agent.GameAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.InputTypeInterpreterAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.RagAgent;
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
        GameAgent gameAgent = new GameAgent(llmClient, sqlLiteDao);
        RagAgent ragAgent = new RagAgent(llmClient, sqlLiteDao);
        RuleExtractionAgent ruleExtractionAgent = new RuleExtractionAgent(llmClient, sqlLiteDao);

        List<Choice> choices = List.of(
            new Choice("ACTION", "Choose this if there is no rule to be extracted", ragAgent),
            new Choice("RULE", "Choose this option if there is a rule to be extracted", ruleExtractionAgent)
        );

        InputTypeInterpreterAgent interpreterAgent = new InputTypeInterpreterAgent(choices, llmClient,0);

        chain = NodeChainBuilder.<String, String>create()
            .add(new InputNode<String, String>())
            .add(interpreterAgent)
            .connect(ruleExtractionAgent, ragAgent)
            .connect(ragAgent, gameAgent);
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

        String finalInput = chatMessages.size() == 0 && sqlLiteDao.getGamePrompt() != null && !sqlLiteDao.getGamePrompt().trim().isEmpty()
            ? sqlLiteDao.getGamePrompt() + "\n\n" + userInput
            : userInput;

        ChatMessage userMsg = new ChatMessage("user", finalInput);
        chatMessages.add(userMsg);
        sqlLiteDao.saveChatMessage(userMsg, sqlLiteDao.getCurrentChatId());
        renderChatMessage(userMsg);
        inputField.setText("");

        new Thread(() -> {
            try {
                logger.debug("InterfacePanel: Processing user input: {}", finalInput);
                chain.execute(finalInput);
                String response = (String) chain.getLastOutput();
                logger.debug("InterfacePanel: Generated response: {}", response);
                
                // Create and display system response
                ChatMessage systemMsg = new ChatMessage("system", response);
                chatMessages.add(systemMsg);
                sqlLiteDao.saveChatMessage(systemMsg, sqlLiteDao.getCurrentChatId());
                SwingUtilities.invokeLater(() -> renderChatMessage(systemMsg));
            } catch (Exception ex) {
                logger.error("InterfacePanel: Error processing input", ex);
                SwingUtilities.invokeLater(() -> renderSystemMessage("Error processing input: " + ex.getMessage() + "\n"));
            }
        }).start();
    }
}
