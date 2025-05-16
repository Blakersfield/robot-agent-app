package com.blakersfield.gameagentsystem.llm.model.node.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.blakersfield.gameagentsystem.llm.clients.LLMClient;

public class GameActionAgent extends BasicTextAgent {
    private static final Logger logger = LoggerFactory.getLogger(GameActionAgent.class);
    static String prompt = "You are a game action interpreter. Based on the user's input, determine what action the game agent should take and output it as a ROS command. Format your response as a clear ROS command that can be executed.";

    public GameActionAgent(LLMClient llmClient) {
        super(llmClient, prompt);
    }

    @Override
    public void act() {
        logger.debug("Processing game action input: {}", input);
        super.act();
        logger.debug("Generated game action: {}", output);
    }
}
