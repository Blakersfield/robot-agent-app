/* Main.java */
package com.blakersfield.gameagentsystem;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.blakersfield.gameagentsystem.panels.FlowPanel;
import com.blakersfield.gameagentsystem.panels.InterfacePanel;
import com.blakersfield.gameagentsystem.panels.SettingsPanel;
import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.panels.ChatPanel;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.blakersfield.gameagentsystem.utility.*;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
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
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Agent Client");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // set close behavior so client, other resources can be termed
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
                frame.dispose();
                System.exit(0);
            }
        });

        initializeDatabase();

        // layout w/tabs 
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        // tabs.addTab("Chat Client", IconProvider.get("chat"), new ChatPanel(HTTP_CLIENT, sqlLiteDao, OLLAMA_LOCAL_URL), "Chat Client");
        // tabs.addTab("ROS LLM", IconProvider.get("ros_llm"), new InterfacePanel(), "LLM ROS Client");
        // tabs.addTab("Settings", IconProvider.get("settings"), new SettingsPanel(sqlLiteDao), "Configuration");
        // tabs.addTab("Flow", IconProvider.get("flow"), new FlowPanel(), "Flowchart Builder");
        tabs.addTab(null, IconProvider.get("chat"), new ChatPanel(HTTP_CLIENT, sqlLiteDao, OLLAMA_LOCAL_URL), null);
        tabs.addTab(null, IconProvider.get("ros_llm"), new InterfacePanel(), null);
        tabs.addTab(null, IconProvider.get("settings"), new SettingsPanel(sqlLiteDao), null);
        tabs.addTab(null, IconProvider.get("flow"), new FlowPanel(), null);
        frame.add(tabs);
        frame.setVisible(true);
    }

    private static void initializeDatabase(){
        Statement stmt = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            stmt = connection.createStatement();
            sqlLiteDao = new SqlLiteDao(connection);
            stmt.execute("CREATE TABLE IF NOT EXISTS chat_messages (chat_message_id INTEGER PRIMARY KEY AUTOINCREMENT, chat_id TEXT, role TEXT, content TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS lang_chain_nodes (node_id INTEGER PRIMARY KEY AUTOINCREMENT, lang_chain_id INTEGER, next_node_id INTEGER)");
            //maybe edges should be represented separately for language graphs. 
            stmt.execute("CREATE TABLE IF NOT EXISTS agents (agent_id INTEGER PRIMARY KEY AUTOINCREMENT, system_content TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS config_settings (setting_key TEXT PRIMARY KEY, setting_value TEXT)");
            initConfigSetting(Configuration.OLLAMA_BASE_URL, Configuration.DEFAULT_OLLAMA_BASE_URL);
            initConfigSetting(Configuration.OLLAMA_MODEL, Configuration.DEFAULT_OLLAMA_MODEL);
            initConfigSetting(Configuration.OPENAI_API_KEY, Configuration.DEFAULT_OPENAI_API_KEY);
            initConfigSetting(Configuration.OPENAI_API_SECRET, Configuration.DEFAULT_OPENAI_API_SECRET);
            initConfigSetting(Configuration.OPENAI_MODEL, Configuration.DEFAULT_OPENAI_MODEL);
        } catch (SQLException e) {
            System.err.println("Database init failed: " + e.getMessage());
        } finally {
            try {
                stmt.close();
            } catch(Exception e) {
                System.err.println("Error closing statement connection");
            }
        }
    }
    private static void initConfigSetting(String key, String value){
        if (sqlLiteDao.getConfigSetting(key)==null) {sqlLiteDao.saveConfigSetting(key, value);}
    }

    // private static Component createTabComponent(String title, Icon icon) {
    //     // JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    //     // panel.setOpaque(false); // Transparent background
    
    //     // JLabel label = new JLabel(title, icon, JLabel.LEADING);
    //     // label.setHorizontalAlignment(SwingConstants.LEFT);
    //     // panel.add(label);
    
    //     // return panel;
    //     JPanel panel = new JPanel();
    //     panel.setOpaque(false); // So it blends with the background
    
    //     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    //     panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    //     JLabel iconLabel = new JLabel(icon);
    //     iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //     JLabel textLabel = new JLabel(title);
    //     textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //     panel.add(iconLabel);
    //     panel.add(textLabel);
    //     return panel;
    // }

    private static void shutdown(){
        try{
            HTTP_CLIENT.close();
        } catch(Exception e){
            //add logging
        }
    }
}
