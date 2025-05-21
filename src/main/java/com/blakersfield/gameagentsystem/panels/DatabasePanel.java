package com.blakersfield.gameagentsystem.panels;

import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DatabasePanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePanel.class);
    private final SqlLiteDao sqlLiteDao;
    private final JTextArea sqlInput;
    private final JTable resultTable;
    private final DefaultTableModel tableModel;
    private final JTextArea historyArea;
    private final JFileChooser fileChooser;

    public DatabasePanel(SqlLiteDao sqlLiteDao) {
        this.sqlLiteDao = sqlLiteDao;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        fileChooser = new JFileChooser(sqlLiteDao.getConfigSetting(Configuration.EXPORT_PATH));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton exportRulesBtn = new JButton("Export Rules");
        JButton exportChatBtn = new JButton("Export Chat");
        JButton clearRulesBtn = new JButton("Clear Rules");
        JButton clearChatBtn = new JButton("Clear Chat");

        topPanel.add(exportRulesBtn);
        topPanel.add(exportChatBtn);
        topPanel.add(clearRulesBtn);
        topPanel.add(clearChatBtn);

        JPanel sqlPanel = new JPanel(new BorderLayout(5, 5));
        sqlPanel.setBorder(BorderFactory.createTitledBorder("SQL Query"));
        sqlInput = new JTextArea(5, 40);
        sqlInput.setLineWrap(true);
        sqlInput.setWrapStyleWord(true);
        JScrollPane sqlScroll = new JScrollPane(sqlInput);
        sqlScroll.setPreferredSize(new Dimension(600, 100));
        JButton executeBtn = new JButton("Execute SQL");
        sqlPanel.add(sqlScroll, BorderLayout.CENTER);
        sqlPanel.add(executeBtn, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.setPreferredSize(new Dimension(600, 200));
        tableScroll.setBorder(BorderFactory.createTitledBorder("Query Results"));

        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyArea = new JTextArea(10, 30);
        historyArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setPreferredSize(new Dimension(300, 400));
        historyPanel.setBorder(BorderFactory.createTitledBorder("SQL History"));
        historyPanel.add(historyScroll);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(sqlPanel, BorderLayout.NORTH);
        contentPanel.add(tableScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.EAST);

        executeBtn.addActionListener(e -> executeSQL());
        exportRulesBtn.addActionListener(e -> exportTable("game_rules"));
        exportChatBtn.addActionListener(e -> exportTable("chat_messages"));
        clearRulesBtn.addActionListener(e -> clearTable("game_rules"));
        clearChatBtn.addActionListener(e -> clearTable("chat_messages"));
    }

    private void executeSQL() {
        String sql = sqlInput.getText().trim();
        if (sql.isEmpty()) return;

        try {
            if (sql.toLowerCase().startsWith("select")) {
                List<Map<String, Object>> results = sqlLiteDao.executeQuery(sql);
                displayResults(results);
                historyArea.append("Executed: " + sql + "\n");
                if (!results.isEmpty()) {
                    historyArea.append("Returned " + results.size() + " rows\n");
                }
                historyArea.append("\n");
            } else {
                int affected = sqlLiteDao.executeUpdate(sql);
                historyArea.append("Executed: " + sql + "\n");
                historyArea.append("Affected " + affected + " rows\n\n");
                JOptionPane.showMessageDialog(this, 
                    "Query executed successfully. Rows affected: " + affected,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
        } catch (Exception e) {
            logger.error("Failed to execute SQL", e);
            historyArea.append("Error: " + e.getMessage() + "\n\n");
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
            JOptionPane.showMessageDialog(this,
                "Error executing SQL: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            return;
        }

        Vector<String> columns = new Vector<>(results.get(0).keySet());
        tableModel.setColumnIdentifiers(columns);

        tableModel.setRowCount(0);
        for (Map<String, Object> row : results) {
            Vector<Object> rowData = new Vector<>();
            for (String column : columns) {
                Object value = row.get(column);
                rowData.add(value != null ? value : "");
            }
            tableModel.addRow(rowData);
        }

        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
    }

    private void exportTable(String tableName) {
        fileChooser.setSelectedFile(new File(tableName + ".csv"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // Ensure the file has .csv extension
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try {
                sqlLiteDao.exportTableToCSV(tableName, filePath);
                sqlLiteDao.updateConfigSetting(Configuration.EXPORT_PATH, selectedFile.getParent());
                JOptionPane.showMessageDialog(this,
                    "Table exported successfully to: " + filePath,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                logger.error("Failed to export table", e);
                JOptionPane.showMessageDialog(this,
                    "Error exporting table: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearTable(String tableName) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear the " + tableName + " table? This cannot be undone.",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                sqlLiteDao.clearTable(tableName);
                JOptionPane.showMessageDialog(this,
                    "Table cleared successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                logger.error("Failed to clear table", e);
                JOptionPane.showMessageDialog(this,
                    "Error clearing table: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 