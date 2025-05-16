package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.model.node.Node;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

import java.util.List;
import java.util.Objects;

public class BasicChatAgent extends Agent<List<ChatMessage>, ChatMessage> {
    private final LLMClient llmClient;
    // private final ChatMessage systemMessage;
    private Agent<ChatMessage,?> next;

    public BasicChatAgent(LLMClient llmClient) {
        this.llmClient = Objects.requireNonNull(llmClient, "LLMClient cannot be null");
        // this.systemMessage = ChatMessage.system(Objects.requireNonNull(systemPrompt, "System prompt cannot be null")); 
    }

    private ChatMessage chat(List<ChatMessage> messages){
        return this.llmClient.chat(messages);
    }

    @Override
    public void act() {
        // ChatMessage inputMessage = ChatMessage.user(input);
        try {
            output = this.chat(input);
            // output = this.chat(Stream.of(
            //     systemMessage, inputMessage).collect(Collectors.toList())).getContent();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process text", e);
        }
    }

    @Override
    public Node<ChatMessage, ?> next() {
        return this.next;
    }

    public void setNext(Agent<ChatMessage, ?> next) {
        this.next = next;
    }

    @Override
    public void reset() {
        this.input=null;
        this.output=null;
    }
}