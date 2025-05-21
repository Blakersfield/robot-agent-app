package com.blakersfield.gameagentsystem.llm.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    private String model;
    private Boolean stream;
    private List<ChatMessage> messages;
    private Double temperature;
    private Integer maxTokens;
    @JsonProperty("top_p")
    private Double topP;
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    
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
    public Double getTemperature() {
        return temperature;
    }
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    public Integer getMaxTokens() {
        return maxTokens;
    }
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    public Double getTopP() {
        return topP;
    }
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }
    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    public Double getPresencePenalty() {
        return presencePenalty;
    }
    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
}
