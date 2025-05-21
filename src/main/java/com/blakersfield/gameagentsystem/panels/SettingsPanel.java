package com.blakersfield.gameagentsystem.panels;

import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel{
    private static final Logger logger = LoggerFactory.getLogger(SettingsPanel.class);
    private SqlLiteDao sqlLiteDao;
    
    public SettingsPanel(SqlLiteDao sqlLiteDao){
        super();
        this.sqlLiteDao = sqlLiteDao;    
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    
        // check pw
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passPanel.add(new JLabel("Enter password:"));
        JPasswordField passField = new JPasswordField(10);
        JButton unlockBtn = new JButton("Unlock");
        JButton lockBtn = new JButton("Lock");
        lockBtn.setVisible(false);
        passPanel.add(passField);
        passPanel.add(unlockBtn);
        passPanel.add(lockBtn);
        form.add(passPanel);
    

        JPanel settingsFields = new JPanel();
        settingsFields.setLayout(new BoxLayout(settingsFields, BoxLayout.Y_AXIS));
        settingsFields.setVisible(false); // Initially hidden
    
        JComboBox<String> llmSelect = new JComboBox<>(new String[] { "Ollama", "OpenAI" });
        llmSelect.setSelectedItem(sqlLiteDao.getConfigSetting(Configuration.LLM_PROVIDER));
        settingsFields.add(new JLabel("Active LLM Provider:"));
        settingsFields.add(llmSelect);
        settingsFields.add(Box.createVerticalStrut(10));

        // game prompt section
        JPanel gamePromptPanel = new JPanel(new BorderLayout());
        gamePromptPanel.setBorder(BorderFactory.createTitledBorder("Game Prompt"));
        JTextArea gamePromptArea = new JTextArea(5, 40);
        gamePromptArea.setLineWrap(true);
        gamePromptArea.setWrapStyleWord(true);
        gamePromptArea.setText(sqlLiteDao.getGamePrompt());
        JScrollPane gamePromptScroll = new JScrollPane(gamePromptArea);
        JButton saveGamePromptButton = new JButton("Save Game Prompt");
        saveGamePromptButton.addActionListener(e -> {
            sqlLiteDao.saveGamePrompt(gamePromptArea.getText());
            JOptionPane.showMessageDialog(this, "Game prompt saved successfully!");
        });
        gamePromptPanel.add(gamePromptScroll, BorderLayout.CENTER);
        gamePromptPanel.add(saveGamePromptButton, BorderLayout.SOUTH);
        settingsFields.add(gamePromptPanel);
        settingsFields.add(Box.createVerticalStrut(10));

        JPanel ollamaFields = new JPanel(new GridLayout(0, 2));
        JTextField ollamaUrl = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OLLAMA_BASE_URL));
        JTextField ollamaPort = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OLLAMA_PORT));
        JComboBox<String> ollamaModel = new JComboBox<>(new String[] {
            sqlLiteDao.getConfigSetting(Configuration.OLLAMA_MODEL) , "llama3:8b", "mistral", "phi3", "gemma:7b", "Other..."
        });
        ollamaModel.setSelectedItem(sqlLiteDao.getConfigSetting(Configuration.OLLAMA_MODEL));
        JTextField ollamaCustomModel = new JTextField();
        ollamaCustomModel.setVisible(false);
        JLabel ollamaCustomModelLabel = new JLabel("Custom Model:");
        ollamaCustomModelLabel.setVisible(false);
        ollamaModel.addActionListener(e -> {
            String selected = (String) ollamaModel.getSelectedItem();
            ollamaCustomModel.setVisible("Other...".equals(selected));
            ollamaCustomModelLabel.setVisible("Other...".equals(selected));
        });

        ollamaFields.add(new JLabel("Ollama URL:"));
        ollamaFields.add(ollamaUrl);
        ollamaFields.add(new JLabel("Ollama Port:"));
        ollamaFields.add(ollamaPort);
        ollamaFields.add(new JLabel("Ollama Model:"));
        ollamaFields.add(ollamaModel);
        ollamaFields.add(ollamaCustomModelLabel);
        ollamaFields.add(ollamaCustomModel);

        JPanel openaiFields = new JPanel(new GridLayout(0, 2));
        JTextField openaiKey = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OPENAI_API_KEY));
        JTextField openaiSecret = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OPENAI_API_SECRET));
        JComboBox<String> openaiModel = new JComboBox<>(new String[] {
            "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo"
        });
        openaiModel.setSelectedItem(sqlLiteDao.getConfigSetting(Configuration.OPENAI_MODEL));

        openaiFields.add(new JLabel("OpenAI API Key:"));
        openaiFields.add(openaiKey);
        openaiFields.add(new JLabel("OpenAI API Secret:"));
        openaiFields.add(openaiSecret);
        openaiFields.add(new JLabel("OpenAI Model:"));
        openaiFields.add(openaiModel);

        settingsFields.add(new JLabel("Ollama Settings:"));
        settingsFields.add(ollamaFields);
        settingsFields.add(Box.createVerticalStrut(20)); 
        settingsFields.add(new JSeparator(JSeparator.HORIZONTAL)); 
        settingsFields.add(Box.createVerticalStrut(20)); 
        settingsFields.add(new JLabel("OpenAI Settings:"));
        settingsFields.add(openaiFields);
        settingsFields.add(Box.createVerticalStrut(20)); 

        JButton saveButton = new JButton("Save All Settings");
        saveButton.addActionListener(e -> {
            try {
                sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_BASE_URL, ollamaUrl.getText().trim());
                sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_PORT, ollamaPort.getText().trim());
                String selectedOllamaModel = ollamaModel.getSelectedItem().toString();
                String ollamaModelToSave = "Other...".equals(selectedOllamaModel) ? ollamaCustomModel.getText().trim() : selectedOllamaModel;
                sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_MODEL, ollamaModelToSave);

                sqlLiteDao.updateConfigSetting(Configuration.OPENAI_API_KEY, openaiKey.getText().trim());
                sqlLiteDao.updateConfigSetting(Configuration.OPENAI_API_SECRET, openaiSecret.getText().trim());
                sqlLiteDao.updateConfigSetting(Configuration.OPENAI_MODEL, openaiModel.getSelectedItem().toString());

                sqlLiteDao.updateConfigSetting(Configuration.LLM_PROVIDER, llmSelect.getSelectedItem().toString());
                Main.reinitializeLLMClient();
                
                logger.info("Settings updated successfully - LLM Provider: {}", llmSelect.getSelectedItem());
                JOptionPane.showMessageDialog(this, "All settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                logger.error("Failed to save settings", ex);
                JOptionPane.showMessageDialog(this, "Failed to save settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        settingsFields.add(saveButton);
    
        llmSelect.addActionListener(e -> {
            String selected = (String) llmSelect.getSelectedItem();
            try {
                sqlLiteDao.updateConfigSetting(Configuration.LLM_PROVIDER, selected);
                Main.reinitializeLLMClient();
                logger.info("LLM provider changed to: {}", selected);
            } catch (Exception ex) {
                logger.error("Failed to update LLM provider", ex);
                JOptionPane.showMessageDialog(this, "Failed to update LLM provider: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        unlockBtn.addActionListener(e -> {
            String password = new String(passField.getPassword());
            if (!isValidPassword(password)) {
                logger.warn("Invalid password format attempted");
                JOptionPane.showMessageDialog(this, "Password must be alphanumeric!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SqlLiteDao.setEncryptionKey(password);
            if (sqlLiteDao.validateEncryptionKey()) {
                logger.info("Settings panel unlocked successfully");
                settingsFields.setVisible(true);
                unlockBtn.setVisible(false);
                lockBtn.setVisible(true);
                passField.setText("");
            } else {
                logger.warn("Failed to unlock settings panel - invalid password");
                JOptionPane.showMessageDialog(this, "Invalid password", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        lockBtn.addActionListener(e -> {
            settingsFields.setVisible(false);
            unlockBtn.setVisible(true);
            lockBtn.setVisible(false);
            passField.setText("");
            logger.info("Settings panel locked");
        });
    
        this.add(form, BorderLayout.NORTH);
        this.add(settingsFields, BorderLayout.CENTER);
    }

    private boolean isValidPassword(String password) {
        return password.matches("^[a-zA-Z0-9]+$");
    }
}
