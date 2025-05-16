package com.blakersfield.gameagentsystem;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.blakersfield.gameagentsystem.panels.FlowPanel;
import com.blakersfield.gameagentsystem.panels.InterfacePanel;
import com.blakersfield.gameagentsystem.panels.SettingsPanel;
import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.clients.OpenAiClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.panels.ChatPanel;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.blakersfield.gameagentsystem.utility.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.server.ExportException;
import java.sql.*;

public class Main {
    private static final String DB_PATH = "app.db";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final String OLLAMA_LOCAL_URL = "http://127.0.0.1:11434/api/chat";
    private static Connection connection;
    private static SqlLiteDao sqlLiteDao;
    private static LLMClient llmClient;

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Agent Client");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
                frame.dispose();
                System.exit(0);
            }
        });

        initializeDatabase();
        
        // Show password dialog before proceeding
        if (!handlePasswordSetup()) {
            shutdown();
            frame.dispose();
            System.exit(1);
        }

        initializeLLMClient();
        
        // layout w/tabs 
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.addTab(null, IconProvider.get("chat"), new ChatPanel(HTTP_CLIENT, sqlLiteDao, llmClient), null);
        tabs.addTab(null, IconProvider.get("ros_llm"), new InterfacePanel(HTTP_CLIENT, sqlLiteDao, llmClient), null);
        tabs.addTab(null, IconProvider.get("settings"), new SettingsPanel(sqlLiteDao), null);
        tabs.addTab(null, IconProvider.get("flow"), new FlowPanel(), null);
        frame.add(tabs);
        frame.setVisible(true);
    }

    private static boolean handlePasswordSetup() {
        // Check if this is first run
        boolean isFirstRun = sqlLiteDao.getConfigSetting(Configuration.ENCRYPTION_CHECK_KEY) == null;
        
        if (isFirstRun) {
            return showNewPasswordDialog();
        } else {
            return showExistingPasswordDialog();
        }
    }

    private static boolean showNewPasswordDialog() {
        JDialog dialog = new JDialog((Window) null, "Initial Setup", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel instructionLabel = new JLabel("Please set an admin password:");
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);
        JButton submitButton = new JButton("Submit");
        
        panel.add(instructionLabel);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmField);
        panel.add(submitButton);
        
        final boolean[] result = {false};
        
        submitButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!isValidPassword(password)) {
                JOptionPane.showMessageDialog(dialog, "Password must be alphanumeric!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Initialize encryption and default settings
            sqlLiteDao.initializeEncryption(password);
            initializeDefaultSettings();
            result[0] = true;
            dialog.dispose();
        });
        
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        
        return result[0];
    }

    private static boolean showExistingPasswordDialog() {
        JDialog dialog = new JDialog((Window) null, "Authentication Required", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel instructionLabel = new JLabel("Please enter admin password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton submitButton = new JButton("Submit");
        
        panel.add(instructionLabel);
        panel.add(passwordField);
        panel.add(submitButton);
        
        final boolean[] result = {false};
        
        submitButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            
            if (!isValidPassword(password)) {
                JOptionPane.showMessageDialog(dialog, "Password must be alphanumeric!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SqlLiteDao.setEncryptionKey(password);
            if (sqlLiteDao.validateEncryptionKey()) {
                result[0] = true;
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        
        return result[0];
    }

    private static boolean isValidPassword(String password) {
        return password.matches("^[a-zA-Z0-9]+$");
    }

    private static void initializeDatabase() {
        Statement stmt = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            stmt = connection.createStatement();
            sqlLiteDao = new SqlLiteDao(connection);
            stmt.execute("CREATE TABLE IF NOT EXISTS chat_messages (chat_message_id INTEGER PRIMARY KEY AUTOINCREMENT, chat_id TEXT, role TEXT, content TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS lang_chain_nodes (node_id INTEGER PRIMARY KEY AUTOINCREMENT, lang_chain_id INTEGER, next_node_id INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS agents (agent_id INTEGER PRIMARY KEY AUTOINCREMENT, system_content TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS config_settings (setting_key TEXT PRIMARY KEY, setting_value TEXT)");
        } catch (SQLException e) {
            System.err.println("Database init failed: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch(Exception e) {
                System.err.println("Error closing statement connection");
            }
        }
    }

    private static void initializeDefaultSettings() {
        // Initialize default settings only after encryption is set up
        initConfigSetting(Configuration.OLLAMA_BASE_URL, Configuration.DEFAULT_OLLAMA_BASE_URL);
        initConfigSetting(Configuration.OLLAMA_MODEL, Configuration.DEFAULT_OLLAMA_MODEL);
        initConfigSetting(Configuration.OPENAI_API_KEY, Configuration.DEFAULT_OPENAI_API_KEY);
        initConfigSetting(Configuration.OPENAI_API_SECRET, Configuration.DEFAULT_OPENAI_API_SECRET);
        initConfigSetting(Configuration.OPENAI_MODEL, Configuration.DEFAULT_OPENAI_MODEL);
        initConfigSetting(Configuration.LLM_PROVIDER, Configuration.DEFAULT_LLM_PROVIDER);
    }

    private static void initConfigSetting(String key, String value) {
        if (sqlLiteDao.getConfigSetting(key) == null) {
            sqlLiteDao.saveConfigSetting(key, value);
        }
    }

    private static void initializeLLMClient() {
        String provider = sqlLiteDao.getConfigSetting(Configuration.LLM_PROVIDER);
        if ("OpenAI".equals(provider)) {
            String apiKey = sqlLiteDao.getConfigSetting(Configuration.OPENAI_API_KEY);
            String apiSecret = sqlLiteDao.getConfigSetting(Configuration.OPENAI_API_SECRET);
            String model = sqlLiteDao.getConfigSetting(Configuration.OPENAI_MODEL);
            llmClient = new OpenAiClient(HTTP_CLIENT, "https://api.openai.com/v1/chat/completions", model, apiKey);
        } else {
            String baseUrl = sqlLiteDao.getConfigSetting(Configuration.OLLAMA_BASE_URL);
            String model = sqlLiteDao.getConfigSetting(Configuration.OLLAMA_MODEL);
            llmClient = new OllamaClient(HTTP_CLIENT, baseUrl, model);
        }
    }

    public static void reinitializeLLMClient() {
        initializeLLMClient();
    }

    private static void shutdown() {
        try {
            HTTP_CLIENT.close();
        } catch(Exception e) {
            //add logging
        }
    }
}
