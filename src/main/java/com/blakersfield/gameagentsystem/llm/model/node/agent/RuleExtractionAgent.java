package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Rule;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RuleExtractionAgent extends Agent<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(RuleExtractionAgent.class);
    
    private final LLMClient llmClient;
    private final SqlLiteDao sqliteDao;
    private final String extractionPrompt;
    private final ObjectMapper objectMapper;

    public RuleExtractionAgent(LLMClient llmClient, SqlLiteDao sqliteDao) {
        this.llmClient = Objects.requireNonNull(llmClient, "OllamaClient cannot be null");
        this.sqliteDao = Objects.requireNonNull(sqliteDao, "SqlLiteDao cannot be null");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        this.extractionPrompt = """
            You are a rule extraction agent. Extract game rules from the user's input and format them as a JSON object. 
            The JSON should contain clear, structured rules that can be used by the game system.
            Format the response as a JSON object with the following structure:
            "rules": [
                "this is the first rule",
                "this is the second rule"
            ]
            """;
    }

    @Override
    public void act() {
        try{
        List<String> extractedRules = extractRules(input);
        List<Rule> existingRules = sqliteDao.getAllRules();
        List<Rule> ruleUpdates = extractedRules.parallelStream()
            .flatMap(newRule -> deconflictRules(newRule, existingRules).stream())
            .toList();
        ruleUpdates.stream().forEach(rule -> {
            rule.setChatId(sqliteDao.getCurrentChatId());
            sqliteDao.updateRule(rule);
        });
        } catch (Exception e){
            logger.error("Problem extracting rules!");
        }
        this.output = this.input;
        this.propagateOutput();
    }

    private List<String> extractRules(String inputText) throws JsonMappingException, JsonProcessingException{
        List<ChatMessage> prompt = List.of(
            ChatMessage.system(extractionPrompt),
            ChatMessage.user(input)
        );
        ChatMessage response = llmClient.chat(prompt);
        String responseString = response.getContent();
        return objectMapper.readValue(responseString, new TypeReference<List<String>>() {});
    }

    public List<Rule> deconflictRules(String newRule, List<Rule> existingRules) {
        if (existingRules==null || existingRules.isEmpty() || newRule ==null || newRule.isEmpty()){
            return Collections.emptyList();
        }
        try {
            StringBuilder existingRulesStr = new StringBuilder();
            for (Rule rule : existingRules) {
                existingRulesStr.append(String.format("- ruleId: %d\n  content: \"%s\"\n",
                    rule.getRuleId(), rule.getContent()));
            }

            String systemPrompt = """
                You are a rule consistency checker. A new game rule is being considered. Compare it against the list of existing rules.

                A rule may conflict in two ways:
                1. It overlaps in meaning or purpose with an existing rule.
                2. It contradicts an existing rule in logic or outcome.

                For each conflict, revise the existing rule's content to integrate or resolve the conflict with the new rule.
                Return ONLY a JSON array of revised rule objects with fields:
                - "ruleId": (number)
                - "content": (string with the updated rule text)

                If there are no conflicts, return an empty JSON list: []
            """;

            String userPrompt = String.format("""
                New rule:
                "%s"

                Existing rules:
                %s
            """, newRule, existingRulesStr);

            List<ChatMessage> prompt = List.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(userPrompt)
            );

            ChatMessage response = llmClient.chat(prompt);
            String responseContent = response.getContent().trim();

            if (responseContent.startsWith("{")) {
                // LLM occasionally wraps array in an object — try to extract array if needed
                JsonNode json = objectMapper.readTree(responseContent);
                if (json.has("rules")) {
                    responseContent = json.get("rules").toString();
                }
            }

            return objectMapper.readValue(
                responseContent, new TypeReference<List<Rule>>() {});
        } catch (Exception e) {
            logger.error("Error during rule deconfliction", e);
            return Collections.emptyList();
        }
    }
}
