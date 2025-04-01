/* Main.java */
package com.blakersfield.gameagentsystem;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.blakersfield.gameagentsystem.panels.FlowPanel;
import com.blakersfield.gameagentsystem.panels.InterfacePanel;
import com.blakersfield.gameagentsystem.panels.SettingsPanel;
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
import java.sql.*;

public class Main {
    private static final String DB_PATH = "app.db";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final String OLLAMA_LOCAL_URL = "http://127.0.0.1:11434/api/chat";
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Game Agent System");
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

        // layout w/tabs 
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        // UIManager.put("TabbedPane.tabHeight", 40);
        // UIManager.put("TabbedPane.tabInsets", new Insets(4, 8, 4, 8));
        // UIManager.put("TabbedPane.tabAlignment", SwingConstants.LEFT);
        tabs.addTab("Chat Client", IconProvider.get("chat"), new ChatPanel(HTTP_CLIENT, OLLAMA_LOCAL_URL), "Chat Client");
        tabs.addTab("ROS LLM", IconProvider.get("ros_llm"), new InterfacePanel(), "LLM ROS Client");
        tabs.addTab("Settings", IconProvider.get("settings"), new SettingsPanel(), "Configuration");
        tabs.addTab("Flow", IconProvider.get("flow"), new FlowPanel(), "Flowchart Builder");
        // tabs.addTab(null, new ChatPanel(HTTP_CLIENT));
        // tabs.addTab(null, new InterfacePanel());
        // tabs.addTab(null, new SettingsPanel());
        // tabs.addTab(null, new FlowPanel());
        // tabs.setTabComponentAt(0, createTabComponent("Chat Client", IconProvider.get("chat")));
        // tabs.setTabComponentAt(1, createTabComponent("LLM ROS Client", IconProvider.get("ros_llm")));
        // tabs.setTabComponentAt(2, createTabComponent("Configuration", IconProvider.get("settings")));
        // tabs.setTabComponentAt(3, createTabComponent("Flowchart Builder", IconProvider.get("flow")));


        frame.add(tabs);
        frame.setVisible(true);
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY, content TEXT)");
        } catch (SQLException e) {
            System.err.println("Database init failed: " + e.getMessage());
        }
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
