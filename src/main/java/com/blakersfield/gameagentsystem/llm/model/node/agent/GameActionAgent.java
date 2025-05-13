package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;

public class GameActionAgent extends BasicTextAgent {
    static String prompt = "You are a game action interpreter. Based on the user's input, determine what action the game agent should take and output it as a ROS command. Format your response as a clear ROS command that can be executed.";

    public GameActionAgent(LLMClient llmClient) {
        super(llmClient,prompt);
    }

}
