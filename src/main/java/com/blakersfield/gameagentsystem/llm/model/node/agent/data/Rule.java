package com.blakersfield.gameagentsystem.llm.model.node.agent.data;

public class Rule {
    private Long ruleId;
    private String chatId;
    private String content;

    public Rule(Long ruleId, String chatId, String content) {
        this.ruleId = ruleId;
        this.chatId = chatId;
        this.content = content;
    }
    public Long getRuleId() {
        return ruleId;
    }
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
    public String getChatId() {
        return chatId;
    }
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
}
