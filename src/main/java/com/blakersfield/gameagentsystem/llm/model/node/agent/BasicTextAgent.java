package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class BasicTextAgent extends Agent<String, String> {
    private String systemPrompt;
    private LLMClient llmClient;
    public BasicTextAgent(LLMClient llmClient, String systemPrompt) {
        this.llmClient = llmClient;
        this.systemPrompt = systemPrompt;
    }

    private ChatMessage chat(List<ChatMessage> messages){
        return this.llmClient.chat(messages);
    }

    @Override
    public void act() {
        try {
            List<ChatMessage> messages = Stream.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(input)).collect(Collectors.toList());
            output = chat(messages).getContent();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process text", e);
        }
    }

    @Override
    public void reset() {
        this.input = null;
        this.output = null;
    }
    
}
