package com.blakersfield.gameagentsystem.llm.model.node.agent.data;

import com.blakersfield.gameagentsystem.llm.model.node.agent.Agent;

public class Choice {
    private final String key;                // Label used in LLM output
    private final String description;        // Shown in prompt
    private final Agent<String, ?> agent;    // The agent to invoke next

    public Choice(String key, String description, Agent<String, ?> agent) {
        this.key = key;
        this.description = description;
        this.agent = agent;
    }

    public String key() {
        return key;
    }

    public String description() {
        return description;
    }

    public Agent<String, ?> agent() {
        return agent;
    }

    @Override
    public String toString() {
        return key + ": " + description;
    }
}
