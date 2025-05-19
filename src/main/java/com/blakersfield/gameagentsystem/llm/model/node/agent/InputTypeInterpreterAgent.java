package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Choice;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class InputTypeInterpreterAgent extends Agent<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(InputTypeInterpreterAgent.class);
    private final List<Choice> choices;
    private final LLMClient llmClient;
    private Agent<String, ?> chosenNextAgent;

    public InputTypeInterpreterAgent(List<Choice> choices, LLMClient llmClient) {
        //TODO add default agent which is selected after failure
        //if default agent is null use the forst
        this.choices = choices;
        this.llmClient = llmClient;
    }

    @Override
    public void act() {
        logger.debug("Processing input for type interpretation: {}", input);
        List<ChatMessage> prompt = List.of(
            createPrompt(),
            ChatMessage.user(input)
        );

        ChatMessage response = llmClient.chat(prompt);
        String selectedKey = extractChoiceKey(response.getContent());
        logger.debug("LLM selected action type: {}", selectedKey);

        Optional<Choice> selected = choices.stream()
            .filter(c -> c.key().equalsIgnoreCase(selectedKey.trim()))
            .findFirst();

        if (selected.isEmpty()) {
            logger.error("LLM selected unknown option: {}", selectedKey);
            throw new IllegalStateException("LLM selected unknown option: " + selectedKey);
        }

        this.next = selected.get().agent();
        logger.debug("Selected agent for type: {}", selectedKey);

        this.output = this.input;
        this.propagateOutput();
    }


    private ChatMessage createPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a routing agent. Based on the user's input, select the most appropriate option from the list below:\n\n");

        for (Choice choice : choices) {
            sb.append("- ").append(choice.toString()).append("\n");
        }

        sb.append("""
        
            Respond with ONLY the key of the best choice. Do not explain.

        """);

        return ChatMessage.system(sb.toString());
    }

    private String extractChoiceKey(String rawOutput) {
        // Strip quotes, whitespace, etc.
        return rawOutput.replaceAll("[\"\\n\\r]", "").trim();
    }

    @Override
    public void reset() {
        this.input = null;
        this.output = null;
        this.chosenNextAgent = null;
    }
}