package com.blakersfield.gameagentsystem.llm.clients;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.blakersfield.gameagentsystem.config.Configuration;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class SqlLiteDao {
    private final Connection connection;
    private static final String KEY = "s3cr3tKey1234567"; //TODO replace with input value/admin key
    private static final String ALGORITHM = "AES";

    public SqlLiteDao(Connection connection){
        this.connection = connection;
    }

    private static SecretKeySpec getKeySpec() {
        return new SecretKeySpec(KEY.getBytes(), ALGORITHM);
    }

    private String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec());
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting string", e);
        }
    }

    private String decrypt(String encryptedBase64) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec());
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting string", e);
        }
    }

    public void saveChatMessage(ChatMessage chatMessage, String chatId){
        String sql = "INSERT INTO chat_messages(chat_id, role, content) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, chatId);
            stmt.setString(2, chatMessage.getRole());
            stmt.setString(3, chatMessage.getContent());
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace(); // TODO: replace with logger
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
            e.printStackTrace(); // TODO: replace with logger
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
            e.printStackTrace(); // TODO: replace with logger
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
            e.printStackTrace(); // TODO: replace with logger
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
            e.printStackTrace(); // TODO: replace with logger
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
            e.printStackTrace(); // TODO: replace with logger
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
        }
    }
    public String getConfigSetting(String settingKey){
        String sql = "select setting_value from config_settings where setting_key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, settingKey);
            try(ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return this.decrypt(rs.getString("setting_value"));
                }
            }
        } catch (Exception e){
            e.printStackTrace(); // TODO: replace with logger
        }
        return null;
    }
    public void saveConfigSetting(String settingKey, String settingValue){
        String encryptedValue = this.encrypt(settingValue);
        String sql = "insert into config_settings (setting_key, setting_value) values (?,?)";
        try (PreparedStatement stmt = this.connection.prepareStatement(sql)){
            stmt.setString(1, settingKey);
            stmt.setString(2, encryptedValue);
            stmt.executeUpdate();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void updateConfigSetting(String settingKey, String settingValue){
        String encryptedValue = this.encrypt(settingValue);
        String sql = "update config_settings set setting_value=? where setting_key=?";
        try (PreparedStatement stmt = this.connection.prepareStatement(sql)){
            stmt.setString(1, encryptedValue);
            stmt.setString(2, settingKey);
            stmt.executeUpdate();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isDecryptionKeyValue(String key){
        String encryptedCheck = this.getConfigSetting(Configuration.ENCRYPTION_CHECK_KEY);
        return Configuration.ENCRYPTION_CHECK_TRUTH.equals(this.decrypt(encryptedCheck));
    }
}