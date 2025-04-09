package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.List;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Choice;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import java.util.Optional;

public class InputTypeInterpreterAgent extends Agent<String, String> {
    private final List<Choice> choices;
    private final OllamaClient ollamaClient;
    private Agent<String, ?> chosenNextAgent;

    public InputTypeInterpreterAgent(List<Choice> choices, OllamaClient ollamaClient) {
        this.choices = choices;
        this.ollamaClient = ollamaClient;
    }

    @Override
    public void act() {
        List<ChatMessage> prompt = List.of(
            createPrompt(),
            ChatMessage.user(input)
        );

        ChatMessage response = ollamaClient.chat(prompt);
        String selectedKey = extractChoiceKey(response.getContent());

        Optional<Choice> selected = choices.stream()
            .filter(c -> c.key().equalsIgnoreCase(selectedKey.trim()))
            .findFirst();

        if (selected.isEmpty()) {
            throw new IllegalStateException("LLM selected unknown option: " + selectedKey);
        }

        this.chosenNextAgent = selected.get().agent();
        this.chosenNextAgent.setInput(input);
        this.output = selectedKey; // just store label for now
    }

    @Override
    public Agent<String, ?> next() {
        return chosenNextAgent;
    }

    private ChatMessage createPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a routing agent. Based on the user's input, select the most appropriate option from the list below:\n\n");

        for (Choice choice : choices) {
            sb.append("- ").append(choice.toString()).append("\n");
        }

        sb.append("""
        
            Respond with ONLY the key of the best choice (e.g., "RULE", "QUESTION", "ACTION"). Do not explain.

            Example:

            User: "What are the rules again?"
            Response: "QUESTION"
        """);

        return ChatMessage.system(sb.toString());
    }

    private String extractChoiceKey(String rawOutput) {
        // Strip quotes, whitespace, etc.
        return rawOutput.replaceAll("[\"\\n\\r]", "").trim();
    }
}