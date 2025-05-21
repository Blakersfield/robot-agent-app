package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.model.node.agent.data.Rule;

public class RagAgent extends Agent<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(RagAgent.class);
    private final LLMClient llmClient;
    private final SqlLiteDao sqlLiteDao;

    public RagAgent(LLMClient llmClient, SqlLiteDao sqlLiteDao) {
        this.llmClient = llmClient;
        this.sqlLiteDao = sqlLiteDao;
    }

    @Override
    public void act() {
        logger.debug("Processing input for RAG: {}", input);
        
        List<Rule> rules = sqlLiteDao.getAllRules();
        String rulesContext = rules.stream()
            .map(Rule::getContent)
            .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            Game Rules:
            %s
            
            User Input:
            %s
            """, rulesContext, input);

        this.output = prompt;
        this.propagateOutput();
    }

    @Override
    public void reset() {
        this.input = null;
        this.output = null;
    }
} 