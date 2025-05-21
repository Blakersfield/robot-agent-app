package com.blakersfield.gameagentsystem.llm.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    
    // OpenAI specific fields
    private String id;
    private String object;
    private List<Choice> choices;
    private Usage usage;
    
    public ChatResponse() {}
    
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
        if (message != null) {
            return message;
        }
        // For OpenAI responses, extract message from choices
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getMessage();
        }
        return null;
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
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getObject() {
        return object;
    }
    public void setObject(String object) {
        this.object = object;
    }
    public List<Choice> getChoices() {
        return choices;
    }
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    public Usage getUsage() {
        return usage;
    }
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {
        private ChatMessage message;
        @JsonProperty("finish_reason")
        private String finishReason;
        private Integer index;
        
        public ChatMessage getMessage() {
            return message;
        }
        public void setMessage(ChatMessage message) {
            this.message = message;
        }
        public String getFinishReason() {
            return finishReason;
        }
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
        public Integer getIndex() {
            return index;
        }
        public void setIndex(Integer index) {
            this.index = index;
        }
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
        
        public Integer getPromptTokens() {
            return promptTokens;
        }
        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }
        public Integer getCompletionTokens() {
            return completionTokens;
        }
        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }
        public Integer getTotalTokens() {
            return totalTokens;
        }
        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}
