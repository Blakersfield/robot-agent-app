package com.blakersfield.gameagentsystem.panels;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel{
    public SettingsPanel(){
        super();    
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
        JTextField ollamaUrl = new JTextField("http://localhost:11434");
        JComboBox<String> ollamaModel = new JComboBox<>(new String[] {
            "llama3:8b", "mistral", "phi3", "gemma:7b", "Other..."
        });
        JTextField ollamaCustomModel = new JTextField();
        ollamaCustomModel.setVisible(false);
    
        ollamaModel.addActionListener(e -> {
            String selected = (String) ollamaModel.getSelectedItem();
            ollamaCustomModel.setVisible("Other...".equals(selected));
        });
    
        ollamaFields.add(new JLabel("Ollama URL:"));
        ollamaFields.add(ollamaUrl);
        ollamaFields.add(new JLabel("Model:"));
        ollamaFields.add(ollamaModel);
        ollamaFields.add(new JLabel("Custom Model:"));
        ollamaFields.add(ollamaCustomModel);
    
        // OpenAI Fields
        JPanel openaiFields = new JPanel(new GridLayout(0, 2));
        JTextField openaiKey = new JTextField();
        JTextField openaiSecret = new JTextField();
        JComboBox<String> openaiModel = new JComboBox<>(new String[] {
            "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo"
        });
    
        openaiFields.add(new JLabel("API Key:"));
        openaiFields.add(openaiKey);
        openaiFields.add(new JLabel("API Secret:"));
        openaiFields.add(openaiSecret);
        openaiFields.add(new JLabel("Model:"));
        openaiFields.add(openaiModel);
    
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
