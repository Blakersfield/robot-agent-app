package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import java.util.List;
import com.blakersfield.gameagentsystem.llm.model.node.LangNode;

public class GameActionAgent extends Agent<String, String> {
    private final OllamaClient ollamaClient;

    public GameActionAgent(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @Override
    public void act() {
        setProcessing();
        try {
            List<ChatMessage> prompt = List.of(
                ChatMessage.system("You are a game action interpreter. Based on the user's input, determine what action the game agent should take and output it as a ROS command. Format your response as a clear ROS command that can be executed."),
                ChatMessage.user(input)
            );

            ChatMessage response = ollamaClient.chat(prompt);
            this.output = response.getContent();
            setCompleted();
        } catch (Exception e) {
            setError(e);
        }
    }

    @Override
    public LangNode<?, ?> next() {
        return null; // This is the end of the chain
    }
}
