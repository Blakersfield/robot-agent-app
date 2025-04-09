package com.blakersfield.gameagentsystem.llm.model.node.agent.data;

import java.util.List;

public class GameActionInput {
    private List<Rule> rules;
    private GameState gameState;
    public List<Rule> getRules() {
        return rules;
    }
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
    public GameState getGameState() {
        return gameState;
    }
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
}
