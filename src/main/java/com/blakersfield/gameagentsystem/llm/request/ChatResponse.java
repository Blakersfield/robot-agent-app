package com.blakersfield.gameagentsystem.llm.request;

public class ChatResponse {
    private String model;
    private String createdAt;
    private ChatMessage message;
    private String doneReason;
    private Boolean done;
    private Long totalDuration;
    private Long loadDuration;
    private Long promptEvalCount;
    private Long promptEvalDuration;
    private Long evalCount;
    private Long evalDuration;
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public ChatMessage getMessage() {
        return message;
    }
    public void setMessage(ChatMessage message) {
        this.message = message;
    }
    public String getDoneReason() {
        return doneReason;
    }
    public void setDoneReason(String doneReason) {
        this.doneReason = doneReason;
    }
    public Boolean getDone() {
        return done;
    }
    public void setDone(Boolean done) {
        this.done = done;
    }
    public Long getTotalDuration() {
        return totalDuration;
    }
    public void setTotalDuration(Long totalDuration) {
        this.totalDuration = totalDuration;
    }
    public Long getLoadDuration() {
        return loadDuration;
    }
    public void setLoadDuration(Long loadDuration) {
        this.loadDuration = loadDuration;
    }
    public Long getPromptEvalCount() {
        return promptEvalCount;
    }
    public void setPromptEvalCount(Long promptEvalCount) {
        this.promptEvalCount = promptEvalCount;
    }
    public Long getPromptEvalDuration() {
        return promptEvalDuration;
    }
    public void setPromptEvalDuration(Long promptEvalDuration) {
        this.promptEvalDuration = promptEvalDuration;
    }
    public Long getEvalCount() {
        return evalCount;
    }
    public void setEvalCount(Long evalCount) {
        this.evalCount = evalCount;
    }
    public Long getEvalDuration() {
        return evalDuration;
    }
    public void setEvalDuration(Long evalDuration) {
        this.evalDuration = evalDuration;
    }

    
}
