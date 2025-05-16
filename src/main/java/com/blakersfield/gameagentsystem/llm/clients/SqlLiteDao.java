package com.blakersfield.gameagentsystem.llm.clients;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Rule;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlLiteDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlLiteDao.class);
    private final Connection connection;
    private static final String ALGORITHM = "AES";
    private static SecretKeySpec currentKeySpec = null;
    private static boolean encryptionEnabled = false;

    private static final String URL_PATTERN = "^(https?://)(localhost|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,})(:\\d+)?(/[\\w-./?%&=]*)?$";
    private static final String PORT_PATTERN = "^\\d{1,5}$";

    private boolean isValidUrl(String url) {
        return url != null && url.matches(URL_PATTERN);
    }

    private boolean isValidPort(String port) {
        if (port == null || !port.matches(PORT_PATTERN)) {
            return false;
        }
        int portNum = Integer.parseInt(port);
        return portNum > 0 && portNum < 65536;
    }

    private void validateSetting(String key, String value) {
        if (key.equals(Configuration.OLLAMA_BASE_URL)) {
            if (!isValidUrl(value)) {
                throw new IllegalArgumentException("Invalid Ollama base URL format");
            }
        } else if (key.equals(Configuration.OLLAMA_PORT)) {
            if (!isValidPort(value)) {
                throw new IllegalArgumentException("Invalid Ollama port number");
            }
        }
    }

    public SqlLiteDao(Connection connection) {
        this.connection = connection;
    }

    public static void setEncryptionKey(String password) {
        try {
            // Hash the password using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            
            // Use first 16 bytes of hash for AES-128 key
            byte[] keyBytes = new byte[16];
            System.arraycopy(hash, 0, keyBytes, 0, 16);
            
            currentKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            encryptionEnabled = true;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String encrypt(String plainText) {
        if (!encryptionEnabled) {
            return plainText;
        }
        if (currentKeySpec == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, currentKeySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting string", e);
        }
    }

    private String decrypt(String encryptedBase64) {
        if (!encryptionEnabled) {
            return encryptedBase64;
        }
        if (currentKeySpec == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, currentKeySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting string", e);
        }
    }

    public boolean validateEncryptionKey() {
        String encryptedCheck = this.getConfigSetting(Configuration.ENCRYPTION_CHECK_KEY);
        if (encryptedCheck == null) {
            return false;
        }
        try {
            String decrypted = this.decrypt(encryptedCheck);
            return Configuration.ENCRYPTION_CHECK_TRUTH.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }

    public void initializeEncryption(String password) {
        setEncryptionKey(password);
        // Encrypt and store the validation value
        String encryptedTruth = encrypt(Configuration.ENCRYPTION_CHECK_TRUTH);
        saveConfigSetting(Configuration.ENCRYPTION_CHECK_KEY, encryptedTruth);
    }

    public void saveChatMessage(ChatMessage chatMessage, String chatId){
        String sql = "INSERT INTO chat_messages(chat_id, role, content) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, chatId);
            stmt.setString(2, chatMessage.getRole());
            stmt.setString(3, chatMessage.getContent());
            stmt.executeUpdate();
        } catch (SQLException e){
            logger.error("SqlLiteDao: Error saving chat message", e);
        }
    }

    public List<ChatMessage> getChatMessagesById(String chatId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT role, content FROM chat_messages WHERE chat_id = ? ORDER BY chat_message_id ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, chatId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String role = rs.getString("role");
                    String content = rs.getString("content");
                    messages.add(new ChatMessage(role, content));
                }
            }
        } catch (SQLException e) {
            logger.error("SqlLiteDao: Error getting chat messages", e);
        }
        return messages;
    }

    public void saveLangChainNode(int langChainId, Integer nextNodeId) {
        String sql = "INSERT INTO lang_chain_nodes(lang_chain_id, next_node_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, langChainId);
            if (nextNodeId != null) {
                stmt.setInt(2, nextNodeId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("SqlLiteDao: Error saving lang chain node", e);
        }
    }

    // public void saveLangChainNodes(List<LangChainNode> nodes) {
    //     for (LangChainNode node : nodes) {
    //         saveLangChainNode(node.getLangChainId(), node.getNextNodeId());
    //     }
    // }

    // public List<LangChainNode> getLangChainNodesByLangChainId(int langChainId) {
    //     List<LangChainNode> nodes = new ArrayList<>();
    //     String sql = "SELECT node_id, lang_chain_id, next_node_id FROM lang_chain_nodes WHERE lang_chain_id = ?";
    //     try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    //         stmt.setInt(1, langChainId);
    //         try (ResultSet rs = stmt.executeQuery()) {
    //             while (rs.next()) {
    //                 int nodeId = rs.getInt("node_id");
    //                 int nextNodeId = rs.getInt("next_node_id");
    //                 if (rs.wasNull()) {
    //                     nodes.add(new LangChainNode(nodeId, langChainId, null));
    //                 } else {
    //                     nodes.add(new LangChainNode(nodeId, langChainId, nextNodeId));
    //                 }
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace(); // TODO: replace with logger
    //     }
    //     return nodes;
    // }

    // public LangChainNode getLangChainNodeById(int nodeId) {
    //     String sql = "SELECT node_id, lang_chain_id, next_node_id FROM lang_chain_nodes WHERE node_id = ?";
    //     try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    //         stmt.setInt(1, nodeId);
    //         try (ResultSet rs = stmt.executeQuery()) {
    //             if (rs.next()) {
    //                 int langChainId = rs.getInt("lang_chain_id");
    //                 int nextNodeId = rs.getInt("next_node_id");
    //                 if (rs.wasNull()) {
    //                     return new LangChainNode(nodeId, langChainId, null);
    //                 } else {
    //                     return new LangChainNode(nodeId, langChainId, nextNodeId);
    //                 }
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace(); // TODO: replace with logger
    //     }
    //     return null;
    // }

    public void saveAgent(String systemContent) {
        String sql = "INSERT INTO agents(system_content) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, systemContent);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("SqlLiteDao: Error saving agent", e);
        }
    }

    public String getAgentById(int agentId) {
        String sql = "SELECT system_content FROM agents WHERE agent_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, agentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("system_content");
                }
            }
        } catch (SQLException e) {
            logger.error("SqlLiteDao: Error getting agent by id", e);
        }
        return null;
    }
    public List<String> getChatIds(){
        String sql = "select distinct chat_id from chat_messages";
        List<String> result = new ArrayList<String>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                result.add(rs.getString(1));
            }
        } catch (Exception e){
            logger.error("SqlLiteDao: Error getting chat ids", e);
        }
        return result;
    }

    public void saveRule(Rule rule){
        String sql = "insert into game_rules(chat_id, content) values (?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1,rule.getChatId());
            stmt.setString(2,rule.getContent());
            stmt.executeUpdate();
        } catch (Exception e){
            logger.error("SqlLiteDao: Error saving rule", e);
        }
    }

    public void updateRule(Rule rule){
        String sql = "update game_rules set content= ? where rule_id = ? and chat_id =?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1,rule.getContent());
            stmt.setLong(2,rule.getRuleId());
            stmt.setString(3,rule.getChatId());
            stmt.executeUpdate();
        } catch (Exception e){
            logger.error("SqlLiteDao: Error updating rule", e);
        }
    }

    public void updateRule(Rule oldRule, Rule newRule){
        String sql = "update game_rules set content= ? where rule_id = ? and chat_id =?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1,newRule.getContent());
            stmt.setLong(2,oldRule.getRuleId());
            stmt.setString(3,oldRule.getChatId());
            stmt.executeUpdate();
        } catch (Exception e){
            logger.error("SqlLiteDao: Error updating rule", e);
        }
    }
    public List<Rule> getAllRules(){
        String sql = "select rule_id, chat_id, content from game_rules";
        List<Rule> result = new ArrayList<Rule>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                Rule rule = new Rule(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3)
                );
                result.add(rule);
            }
        } catch (Exception e){
            logger.error("SqlLiteDao: Error getting all rules", e);
        }
        return result;
    }

    public List<Rule> getAllRules(String chatId){
        String sql = "select rule_id, chat_id, content from game_rules where chat_id = ?";
        List<Rule> result = new ArrayList<Rule>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                Rule rule = new Rule(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3)
                );
                result.add(rule);
            }
        } catch (Exception e){
            logger.error("SqlLiteDao: Error getting rules for chat id", e);
        }
        return result;
    }

    public void updateChatId(String oldId, String newId){
        String sql = "update chat_messages set chat_id = ? where chat_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, newId);
            stmt.setString(2, oldId);
            stmt.executeUpdate();
        } catch (Exception e){
            logger.error("SqlLiteDao: Error updating chat id", e);
        }
    }
    public String getConfigSetting(String settingKey) {
        String sql = "select setting_value from config_settings where setting_key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, settingKey);
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String value = rs.getString("setting_value");
                    return encryptionEnabled ? this.decrypt(value) : value;
                }
            }
        } catch (Exception e) {
            logger.error("SqlLiteDao: Error getting config setting", e);
        }
        return null;
    }
    public void saveConfigSetting(String settingKey, String settingValue) {
        validateSetting(settingKey, settingValue);
        String valueToStore = encryptionEnabled ? this.encrypt(settingValue) : settingValue;
        String sql = "insert into config_settings (setting_key, setting_value) values (?,?)";
        try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, settingKey);
            stmt.setString(2, valueToStore);
            stmt.executeUpdate();
        } catch (Exception e) {
            logger.error("SqlLiteDao: Error saving config setting", e);
        }
    }
    public void updateConfigSetting(String settingKey, String settingValue) {
        validateSetting(settingKey, settingValue);
        String valueToStore = encryptionEnabled ? this.encrypt(settingValue) : settingValue;
        String sql = "update config_settings set setting_value=? where setting_key=?";
        try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, valueToStore);
            stmt.setString(2, settingKey);
            stmt.executeUpdate();
        } catch (Exception e) {
            logger.error("SqlLiteDao: Error updating config setting", e);
        }
    }
}