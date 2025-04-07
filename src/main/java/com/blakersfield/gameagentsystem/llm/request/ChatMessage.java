package com.blakersfield.gameagentsystem.llm.request;

public class ChatMessage {
    private String role;
    private String content;
    public ChatMessage(){}
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
    public static ChatMessage user(String content){
        return new ChatMessage("user",content);
    }
    public static ChatMessage system(String content){
        return new ChatMessage("system",content);
    }
    public static ChatMessage agent(String content){
        return new ChatMessage("agent",content);
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
}
