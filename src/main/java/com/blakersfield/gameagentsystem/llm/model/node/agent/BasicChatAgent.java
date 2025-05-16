package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.model.node.Node;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class BasicChatAgent extends Agent<List<ChatMessage>, ChatMessage> {
    private static final Logger logger = LoggerFactory.getLogger(BasicChatAgent.class);
    private final LLMClient llmClient;
    // private final ChatMessage systemMessage;
    private Agent<ChatMessage,?> next;

    public BasicChatAgent(LLMClient llmClient) {
        this.llmClient = Objects.requireNonNull(llmClient, "LLMClient cannot be null");
        // this.systemMessage = ChatMessage.system(Objects.requireNonNull(systemPrompt, "System prompt cannot be null")); 
    }

    protected ChatMessage chat(List<ChatMessage> messages){
        return this.llmClient.chat(messages);
    }

    @Override
    public void act() {
        // ChatMessage inputMessage = ChatMessage.user(input);
        try {
            if (input == null) {
                logger.error("Input is null");
                throw new IllegalStateException("Input cannot be null");
            }
            
            logger.debug("Processing chat with {} messages", input.size());
            output = this.chat(input);
            
            if (output == null) {
                logger.error("LLM returned null response");
                throw new IllegalStateException("LLM returned null response");
            }
            
            logger.debug("Generated response: {}", output.getContent());
        } catch (Exception e) {
            logger.error("Failed to process chat", e);
            throw new RuntimeException("Failed to process text: " + e.getMessage(), e);
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