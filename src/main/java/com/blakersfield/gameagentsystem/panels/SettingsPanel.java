package com.blakersfield.gameagentsystem.panels;

import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;

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
        settingsFields.add(new JLabel("LLM Provider:"));
        settingsFields.add(llmSelect);



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
        JButton saveButtonOllama = new JButton("Save");
        saveButtonOllama.addActionListener(e -> {
            sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_BASE_URL, ollamaUrl.getText().trim());
            String selectedModel =  ollamaModel.getSelectedItem().toString();
            String modelToSave = "Other...".equals(selectedModel) ? ollamaCustomModel.getText().trim() : selectedModel;
            sqlLiteDao.updateConfigSetting(Configuration.OLLAMA_MODEL, modelToSave);
        });

        ollamaFields.add(new JLabel("Ollama URL:"));
        ollamaFields.add(ollamaUrl);
        ollamaFields.add(new JLabel("Model:"));
        ollamaFields.add(ollamaModel);
        ollamaFields.add(ollamaCustomModelLabel);
        ollamaFields.add(ollamaCustomModel);
        ollamaFields.add(saveButtonOllama);

        // OpenAI Fields
        JPanel openaiFields = new JPanel(new GridLayout(0, 2));
        JTextField openaiKey = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OPENAI_API_KEY));
        JTextField openaiSecret = new JTextField(sqlLiteDao.getConfigSetting(Configuration.OPENAI_API_SECRET));
        JComboBox<String> openaiModel = new JComboBox<>(new String[] {
            "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo"
        });
        openaiModel.setSelectedItem(sqlLiteDao.getConfigSetting(Configuration.OPENAI_MODEL));
        JButton saveButtonOpenAi = new JButton("Save");
        saveButtonOpenAi.addActionListener(e -> {
            sqlLiteDao.updateConfigSetting(Configuration.OPENAI_API_KEY, openaiKey.getText().trim().toString());
            sqlLiteDao.updateConfigSetting(Configuration.OPENAI_API_SECRET, openaiSecret.getText().trim().toString());
            sqlLiteDao.updateConfigSetting(Configuration.OPENAI_MODEL, openaiModel.getSelectedItem().toString());
        });
        openaiFields.add(new JLabel("API Key:"));
        openaiFields.add(openaiKey);
        openaiFields.add(new JLabel("API Secret:"));
        openaiFields.add(openaiSecret);
        openaiFields.add(new JLabel("Model:"));
        openaiFields.add(openaiModel);
        openaiFields.add(saveButtonOpenAi);
        // Dynamic display based on LLM choice
        JPanel dynamicPanel = new JPanel(new BorderLayout());
        dynamicPanel.add(ollamaFields, BorderLayout.CENTER);
        llmSelect.addActionListener(e -> {
            dynamicPanel.removeAll();
            dynamicPanel.add("Ollama".equals(llmSelect.getSelectedItem()) ? ollamaFields : openaiFields);
            dynamicPanel.revalidate();
            dynamicPanel.repaint();
        });
    
        settingsFields.add(dynamicPanel);
    
        // Unlock button logic
        unlockBtn.addActionListener(e -> {
            String pass = new String(passField.getPassword());
            if ("admin".equals(pass)) { // Replace with real check
                settingsFields.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        this.add(form, BorderLayout.NORTH);
        this.add(settingsFields, BorderLayout.CENTER);
    }
}
