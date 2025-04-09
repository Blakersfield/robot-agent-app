package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.*;

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

public class RuleExtractionAgent extends Agent<String, List<Rule>> {
    private final OllamaClient ollamaClient;
    private final SqlLiteDao sqliteDao;
    private final String extractionPrompt;
    private final String comparisonPrompt;
    private final String refinementPrompt;
    private ObjectMapper objectMapper;
    private Agent<?, ?> nextAgent; // optional: if you want to chain further

    public RuleExtractionAgent(OllamaClient ollamaClient, SqlLiteDao sqliteDao) {
        this.ollamaClient = ollamaClient;
        this.sqliteDao = sqliteDao;
        this.objectMapper = new ObjectMapper();
        // this.objectMapper.
        this.extractionPrompt = """
            You are a rule extraction agent. From the user's input, extract any game rules. 
            Return a JSON array of objects like:
            [{"content": "Players must draw one card each turn."}]
            If no rules are found, return [].
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
        // Step 1: Extract rules from input
        List<ChatMessage> extractMessages = List.of(
            ChatMessage.system(extractionPrompt),
            ChatMessage.user(input)
        );

        ChatMessage extractResponse = ollamaClient.chat(extractMessages);
        List<Rule> extractedRules = parseRuleListFromJson(extractResponse.getContent());

        if (extractedRules.isEmpty()) {
            this.output = List.of(); // nothing to do
            return;
        }

        // Step 2: Retrieve existing rules from DB
        List<Rule> existingRules = sqliteDao.getAllRules();

        // Step 3: Compare rules using LLM
        List<ChatMessage> compareMessages = List.of(
            ChatMessage.system(comparisonPrompt),
            ChatMessage.user("Existing Rules:\n" + toJson(existingRules)),
            ChatMessage.user("New Rules:\n" + toJson(extractedRules))
        );

        ChatMessage compareResponse = ollamaClient.chat(compareMessages);
        RuleComparisonResult comparison = parseComparisonResult(compareResponse.getContent());

        // Step 4: Insert truly new rules
        for (Rule rule : comparison.getNewRules()) {
            sqliteDao.saveRule(rule);
        }

        // Step 5: Handle modified rules (refine with LLM)
        for (RuleModification mod : comparison.getModifiedRules()) {
            List<ChatMessage> refineMessages = List.of(
                ChatMessage.system(refinementPrompt),
                ChatMessage.user("Original: " + mod.getOriginal().getContent()),
                ChatMessage.user("Modified: " + mod.getModified().getContent())
            );

            ChatMessage refinedResponse = ollamaClient.chat(refineMessages);
            Rule refined = parseRuleFromJson(refinedResponse.getContent());

            sqliteDao.updateRule(mod.getOriginal(), refined);
        }

        this.output = extractedRules;
    }

    @Override
    public LangNode<?, ?> next() {
        return nextAgent;
    }

    public void setNextAgent(Agent<?, ?> nextAgent) {
        this.nextAgent = nextAgent;
    }

    private List<Rule> parseRuleListFromJson(String json){
        try {
            return this.objectMapper.readValue(json, new TypeReference<List<Rule>>() {});
        } catch (Exception e){
            //logger.error("Could not map rule response: {}",json);
            return null;
        }
    }

    private RuleComparisonResult parseComparisonResult(String json) {
        return new RuleComparisonResult(); // stub
    }

    private Rule parseRuleFromJson(String json) {
        try {
            return this.objectMapper.readValue(json, Rule.class);
        } catch (Exception e){
            //logger.error("Could not map rule response: {}",json);
            return null;
        }
    }

    private String toJson(Object obj) {
        return ""; // Use Jackson or Gson
    }
}
