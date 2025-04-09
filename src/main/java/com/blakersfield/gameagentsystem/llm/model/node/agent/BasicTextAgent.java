package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.model.node.LangNode;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import java.util.ArrayList;
import java.util.List;


public class BasicTextAgent extends Agent<String, String> {
    private final String systemPrompt;
    private final OllamaClient ollamaClient;
    private Agent<String,?> next;
    private List<ChatMessage> messages;
    public BasicTextAgent(OllamaClient ollamaClient, String systemPrompt, List<ChatMessage> messages) {
        this.systemPrompt = systemPrompt;
        this.ollamaClient = ollamaClient;
        this.messages = messages;
    }

    @Override
    public void act() {
        if (this.messages==null){
            this.messages = new ArrayList<ChatMessage>();
        }
        if (systemPrompt!=null && !"".equals(systemPrompt)){
            messages.add(ChatMessage.system(systemPrompt));
        }
        messages.add(ChatMessage.user(this.input));

        ChatMessage response = ollamaClient.chat(messages);
        output = response.getContent();
    }

    @Override
    public LangNode<String, ?> next() {
        return this.next;
    }
}