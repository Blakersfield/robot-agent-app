package com.blakersfield.gameagentsystem.llm.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatResponse {
    private String model;
    @JsonProperty("created_at")
    private String createdAt;
    private ChatMessage message;
    @JsonProperty("done_reason")
    private String doneReason;
    private Boolean done;
    @JsonProperty("total_duration")
    private Long totalDuration;
    @JsonProperty("load_duration")
    private Long loadDuration;
    @JsonProperty("prompt_eval_count")
    private Long promptEvalCount;
    @JsonProperty("prompt_eval_duration")
    private Long promptEvalDuration;
    @JsonProperty("eval_count")
    private Long evalCount;
    @JsonProperty("eval_duration")
    private Long evalDuration;
    public ChatResponse(){}
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
