package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.model.node.LangNode;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BasicTextAgent extends Agent<String, String> {
    private final String systemPrompt;
    private final OllamaClient ollamaClient;
    private final List<ChatMessage> messages;
    private Agent<String,?> next;

    public BasicTextAgent(OllamaClient ollamaClient, String systemPrompt, List<ChatMessage> messages) {
        this.ollamaClient = Objects.requireNonNull(ollamaClient, "OllamaClient cannot be null");
        this.systemPrompt = Objects.requireNonNull(systemPrompt, "System prompt cannot be null");
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
    }

    @Override
    public void act() {
        try {
            setProcessing();
            List<ChatMessage> currentMessages = new ArrayList<>(messages);
            
            if (!systemPrompt.isEmpty()) {
                currentMessages.add(ChatMessage.system(systemPrompt));
            }
            currentMessages.add(ChatMessage.user(input));

            ChatMessage response = ollamaClient.chat(Collections.unmodifiableList(currentMessages));
            output = response.getContent();
            setCompleted();
        } catch (Exception e) {
            setError(e);
            throw new RuntimeException("Failed to process text", e);
        }
    }

    @Override
    public LangNode<String, ?> next() {
        return this.next;
    }

    public void setNext(Agent<String, ?> next) {
        this.next = next;
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}