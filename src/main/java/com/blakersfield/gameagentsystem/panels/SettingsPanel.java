package com.blakersfield.gameagentsystem.panels;

import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.Main;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel{
    private SqlLiteDao sqlLiteDao;
    
    public SettingsPanel(SqlLiteDao sqlLiteDao){
        super();
        this.sqlLiteDao = sqlLiteDao;    
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    
        // Password check
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passPanel.add(new JLabel("Enter password:"));
        JPasswordField passField = new JPasswordField(10);
        JButton unlockBtn = new JButton("Unlock");
        passPanel.add(passField);
        passPanel.add(unlockBtn);
        form.add(passPanel);
    
        // Settings fields
        JPanel settingsFields = new JPanel();
        settingsFields.setLayout(new BoxLayout(settingsFields, BoxLayout.Y_AXIS));
        settingsFields.setVisible(false); // Initially hidden
    
        // LLM Selector
        JComboBox<String> llmSelect = new JComboBox<>(new String[] { "Ollama", "OpenAI" });
        llmSelect.setSelectedItem(sqlLiteDao.getConfigSetting(Configuration.LLM_PROVIDER));
        settingsFields.add(new JLabel("Active LLM Provider:"));
        settingsFields.add(llmSelect);
        settingsFields.add(Box.createVerticalStrut(10));

        // Ollama Fields
        JPanel ollamaFields = new JPanel(new GridLayout(0, 2));
        JTextField ollamaUrl = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OLLAMA_BASE_URL));
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
        ollamaFields.add(new JLabel("Ollama Model:"));
        ollamaFields.add(ollamaModel);
        ollamaFields.add(ollamaCustomModelLabel);
        ollamaFields.add(ollamaCustomModel);

        // OpenAI Fields
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

        // Add both settings panels with separation
        settingsFields.add(new JLabel("Ollama Settings:"));
        settingsFields.add(ollamaFields);
        settingsFields.add(Box.createVerticalStrut(20)); // Add spacing between sections
        settingsFields.add(new JSeparator(JSeparator.HORIZONTAL)); // Add a line separator
        settingsFields.add(Box.createVerticalStrut(20)); // Add more spacing
        settingsFields.add(new JLabel("OpenAI Settings:"));
        settingsFields.add(openaiFields);
        settingsFields.add(Box.createVerticalStrut(20)); // Add spacing before save button

        // Single save button for all settings
        JButton saveButton = new JButton("Save All Settings");
        saveButton.addActionListener(e -> {
            // Save Ollama settings
            sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_BASE_URL, ollamaUrl.getText().trim());
            String selectedOllamaModel = ollamaModel.getSelectedItem().toString();
            String ollamaModelToSave = "Other...".equals(selectedOllamaModel) ? ollamaCustomModel.getText().trim() : selectedOllamaModel;
            sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_MODEL, ollamaModelToSave);

            // Save OpenAI settings
            sqlLiteDao.updateConfigSetting(Configuration.OPENAI_API_KEY, openaiKey.getText().trim());
            sqlLiteDao.updateConfigSetting(Configuration.OPENAI_API_SECRET, openaiSecret.getText().trim());
            sqlLiteDao.updateConfigSetting(Configuration.OPENAI_MODEL, openaiModel.getSelectedItem().toString());

            // Update active provider
            sqlLiteDao.updateConfigSetting(Configuration.LLM_PROVIDER, llmSelect.getSelectedItem().toString());
            Main.reinitializeLLMClient();
            
            JOptionPane.showMessageDialog(this, "All settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        settingsFields.add(saveButton);
    
        // LLM Selector change handler
        llmSelect.addActionListener(e -> {
            String selected = (String) llmSelect.getSelectedItem();
            sqlLiteDao.updateConfigSetting(Configuration.LLM_PROVIDER, selected);
            Main.reinitializeLLMClient();
        });
    
        // Unlock button logic
        unlockBtn.addActionListener(e -> {
            String password = new String(passField.getPassword());
            if (!isValidPassword(password)) {
                JOptionPane.showMessageDialog(this, "Password must be alphanumeric!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SqlLiteDao.setEncryptionKey(password);
            if (sqlLiteDao.validateEncryptionKey()) {
                settingsFields.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        this.add(form, BorderLayout.NORTH);
        this.add(settingsFields, BorderLayout.CENTER);
    }

    private boolean isValidPassword(String password) {
        return password.matches("^[a-zA-Z0-9]+$");
    }
}
