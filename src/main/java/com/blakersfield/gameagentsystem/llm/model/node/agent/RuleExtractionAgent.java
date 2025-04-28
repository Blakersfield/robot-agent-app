package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blakersfield.gameagentsystem.llm.clients.OllamaClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.model.node.LangNode;
import com.blakersfield.gameagentsystem.llm.model.node.agent.Agent;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Rule;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.RuleComparisonResult;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.RuleModification;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RuleExtractionAgent extends Agent<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(RuleExtractionAgent.class);
    private static final int MAX_RETRIES = 3;
    
    private final OllamaClient ollamaClient;
    private final SqlLiteDao sqliteDao;
    private final String extractionPrompt;
    private final String comparisonPrompt;
    private final String refinementPrompt;
    private final ObjectMapper objectMapper;
    private Agent<?, ?> nextAgent;

    public RuleExtractionAgent(OllamaClient ollamaClient, SqlLiteDao sqliteDao) {
        this.ollamaClient = Objects.requireNonNull(ollamaClient, "OllamaClient cannot be null");
        this.sqliteDao = Objects.requireNonNull(sqliteDao, "SqlLiteDao cannot be null");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        this.extractionPrompt = """
            You are a rule extraction agent. Extract game rules from the user's input and format them as a JSON object. 
            The JSON should contain clear, structured rules that can be used by the game system.
            Format the response as a JSON object with the following structure:
            {
                "rules": [
                    {
                        "content": "rule description",
                        "type": "rule type",
                        "conditions": ["condition1", "condition2"]
                    }
                ]
            }
            """;

        this.comparisonPrompt = """
            Compare the new rules against the existing rule set.
            Return a JSON object with fields:
            {
              "newRules": [...],
              "duplicateRules": [...],
              "modifiedRules": [{"original": ..., "modified": ...}]
            }
        """;

        this.refinementPrompt = """
            For each modified rule, decide what the correct version should be.
            Return a list of updated rules to replace the originals.
        """;
    }

    @Override
    public void act() {
        setProcessing();
        try {
            List<ChatMessage> prompt = List.of(
                ChatMessage.system(extractionPrompt),
                ChatMessage.user(input)
            );

            ChatMessage response = ollamaClient.chat(prompt);
            this.output = response.getContent();
            setCompleted();
        } catch (Exception e) {
            setError(e);
            logger.error("Error processing rules", e);
            throw new RuntimeException("Failed to process rules", e);
        }
    }

    @Override
    public LangNode<?, ?> next() {
        return null; // This is the end of the chain
    }

    public void setNextAgent(Agent<?, ?> nextAgent) {
        this.nextAgent = nextAgent;
    }

    private List<Rule> parseRuleListFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Rule>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse rule list from JSON: {}", json, e);
            return null;
        }
    }

    private RuleComparisonResult parseComparisonResult(String json) {
        try {
            return objectMapper.readValue(json, RuleComparisonResult.class);
        } catch (Exception e) {
            logger.error("Failed to parse comparison result from JSON: {}", json, e);
            return new RuleComparisonResult();
        }
    }

    private Rule parseRuleFromJson(String json) {
        try {
            return objectMapper.readValue(json, Rule.class);
        } catch (Exception e) {
            logger.error("Failed to parse rule from JSON: {}", json, e);
            return null;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to convert object to JSON", e);
            return "{}";
        }
    }
}
