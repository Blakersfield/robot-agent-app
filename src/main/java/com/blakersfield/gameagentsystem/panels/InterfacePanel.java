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
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Rule;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import com.blakersfield.gameagentsystem.config.Configuration;

import java.util.List;

public class InterfacePanel extends ChatPanel {
    private static final Logger logger = LoggerFactory.getLogger(InterfacePanel.class);
    private NodeChainBuilder chain;
    private String interfacePrompt;
    private JTextArea promptTextArea;

    public InterfacePanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, LLMClient llmClient) {
        super(httpClient, sqlLiteDao, llmClient);
        interfacePrompt = sqlLiteDao.getConfigSetting(Configuration.INTERFACE_PROMPT);
        initializeLangChain();
        initializePromptPanel();
    }

    private void initializePromptPanel() {
        promptTextArea = new JTextArea(4, 40);
        promptTextArea.setEditable(false);
        promptTextArea.setLineWrap(true);
        promptTextArea.setWrapStyleWord(true);
        promptTextArea.setText(interfacePrompt != null ? interfacePrompt : "");
        promptTextArea.setBackground(new Color(240, 240, 240));
        promptTextArea.setRows(4);
        promptTextArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, promptTextArea.getPreferredSize().height));
        
        JPanel promptPanel = new JPanel(new BorderLayout());
        promptPanel.add(promptTextArea, BorderLayout.CENTER);
        
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(promptPanel, BorderLayout.NORTH);
        
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JScrollPane) {
                containerPanel.add(component, BorderLayout.CENTER);
                break;
            }
        }
        
        add(containerPanel, BorderLayout.CENTER);
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
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Interface Logs and Rules", true);
            dialog.setLayout(new BorderLayout());
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            //logs
            LogViewerPanel logViewer = new LogViewerPanel((Frame) SwingUtilities.getWindowAncestor(this), "logs/interface-panel.log");
            logViewer.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dialog.dispose();
                }
            });
            tabbedPane.addTab("Logs", logViewer.getContentPane());
            
            //rules
            JPanel rulesPanel = new JPanel(new BorderLayout());
            JTextArea rulesText = new JTextArea();
            rulesText.setEditable(false);
            JScrollPane rulesScroll = new JScrollPane(rulesText);
            
            List<Rule> rules = sqlLiteDao.getAllRules();
            StringBuilder rulesContent = new StringBuilder();
            for (Rule rule : rules) {
                rulesContent.append("Rule ").append(rule.getRuleId()).append(": ").append(rule.getContent()).append("\n");
            }
            rulesText.setText(rulesContent.toString());
            
            rulesPanel.add(rulesScroll, BorderLayout.CENTER);
            tabbedPane.addTab("Rules", rulesPanel);
            
            dialog.add(tabbedPane, BorderLayout.CENTER);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dialog.dispose();
                }
            });
            
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }

    @Override
    protected void handleInputSubmission(String userInput) {
        if (userInput.isEmpty()) return;

        // prepend the interface prompt if this is the first message in a new chat
        final String finalInput;
        if (chatMessages.isEmpty()) {
            String configPrompt = sqlLiteDao.getConfigSetting(Configuration.INTERFACE_PROMPT);
            if (configPrompt != null && !configPrompt.trim().isEmpty()) {
                finalInput = configPrompt + "\n\n" + userInput;
                logger.debug("Prepending interface prompt to first message. Final input: {}", finalInput);
            } else {
                finalInput = userInput;
            }
        } else {
            finalInput = userInput;
        }

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
