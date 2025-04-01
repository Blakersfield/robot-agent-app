package com.blakersfield.gameagentsystem.llm.request;

import java.util.List;

public class ChatRequest {
    private String model;
    private Boolean stream;
    private List<ChatMessage> messages;
    
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public Boolean getStream() {
        return stream;
    }
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
    public List<ChatMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

}
