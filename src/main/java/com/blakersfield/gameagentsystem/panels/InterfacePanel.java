package com.blakersfield.gameagentsystem.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.model.LangChain;
import com.blakersfield.gameagentsystem.llm.model.LangChainBuilder;
import com.blakersfield.gameagentsystem.llm.model.node.InputNode;
import com.blakersfield.gameagentsystem.llm.model.node.agent.GameActionAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.InputTypeInterpreterAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.RuleExtractionAgent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Choice;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

import java.util.List;

public class InterfacePanel extends ChatPanel {
    private LangChain<String> langChain;
    private OllamaClient ollamaClient;

    public InterfacePanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, String apiUrl) {
        super(httpClient, sqlLiteDao, apiUrl);
        this.ollamaClient = new OllamaClient(httpClient, apiUrl);
        initializeLangChain();
    }

    private void initializeLangChain() {
        // Create agents
        GameActionAgent gameActionAgent = new GameActionAgent(ollamaClient);
        RuleExtractionAgent ruleExtractionAgent = new RuleExtractionAgent(ollamaClient, sqlLiteDao);

        // Create choices for input type interpreter
        List<Choice> choices = List.of(
            new Choice("action", "Process a game action", gameActionAgent),
            new Choice("rule", "Extract game rules", ruleExtractionAgent)
        );

        InputTypeInterpreterAgent interpreterAgent = new InputTypeInterpreterAgent(choices, ollamaClient);
        InputNode inputNode = new InputNode(interpreterAgent);

        // Build the langchain
        langChain = new LangChainBuilder<String>()
            .addNode(inputNode)
            .addNode(interpreterAgent)
            .addNode(gameActionAgent)
            .addNode(ruleExtractionAgent)
            .connect(inputNode, interpreterAgent)
            .connect(interpreterAgent, gameActionAgent)
            .connect(interpreterAgent, ruleExtractionAgent)
            .build();
    }

    @Override
    protected void handleInputSubmission(String userInput) {
        if (userInput.isEmpty()) return;

        // Add user message to chat
        ChatMessage userMsg = new ChatMessage("user", userInput);
        chatMessages.add(userMsg);
        sqlLiteDao.saveChatMessage(userMsg, chatId);
        renderChatMessage(userMsg);
        inputField.setText("");

        // Process input through langchain
        new Thread(() -> {
            try {
                langChain.run(userInput);
                String response = langChain.output();
                
                // Create and display system response
                ChatMessage systemMsg = new ChatMessage("system", response);
                chatMessages.add(systemMsg);
                sqlLiteDao.saveChatMessage(systemMsg, chatId);
                SwingUtilities.invokeLater(() -> renderChatMessage(systemMsg));
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> renderSystemMessage("Error processing input: " + ex.getMessage() + "\n"));
            }
        }).start();
    }
}
