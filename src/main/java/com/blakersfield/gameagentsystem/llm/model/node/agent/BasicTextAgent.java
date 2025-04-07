package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import java.util.ArrayList;
import java.util.List;


public class BasicTextAgent extends Agent<String, String> {
    private final String systemPrompt;
    private final OllamaClient ollamaClient;

    public BasicTextAgent(String systemPrompt, OllamaClient ollamaClient) {
        this.systemPrompt = systemPrompt;
        this.ollamaClient = ollamaClient;
    }

    @Override
    public void act() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        messages.add(ChatMessage.user(input));

        ChatMessage response = ollamaClient.chat(messages);
        output = response.getContent();
    }
}