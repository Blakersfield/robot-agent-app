package com.blakersfield.gameagentsystem.llm.model.node.agent;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blakersfield.gameagentsystem.llm.clients.LLMClient;
import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;
import com.blakersfield.gameagentsystem.llm.request.ChatMessage;

public class GameAgent extends Agent<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(GameAgent.class);
    private final LLMClient llmClient;
    private final SqlLiteDao sqlLiteDao;

    public GameAgent(LLMClient llmClient, SqlLiteDao sqlLiteDao) {
        this.llmClient = llmClient;
        this.sqlLiteDao = sqlLiteDao;
    }

    @Override
    public void act() {
        logger.debug("Processing game input: {}", input);
        
        List<ChatMessage> chatHistory = sqlLiteDao.getChatMessagesById(sqlLiteDao.getCurrentChatId());
        
        ChatMessage systemMessage = ChatMessage.system(String.format("""
            You are the Game Agent. Your role is to analyze the chat history and the processed input from previous agents 
            to determine the next game action. Consider all the context and rules provided to make an appropriate decision.
            %s""",input));
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(chatHistory);
        messages.add(ChatMessage.user(input));
        
        ChatMessage response = llmClient.chat(messages);
        
        this.output = response.getContent();
        this.propagateOutput();
    }

    @Override
    public void reset() {
        this.input = null;
        this.output = null;
    }
} 