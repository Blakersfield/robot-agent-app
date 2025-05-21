package com.blakersfield.gameagentsystem.llm.clients;

import java.net.URI;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import com.blakersfield.gameagentsystem.llm.request.ChatRequest;
import com.blakersfield.gameagentsystem.llm.request.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiClient implements LLMClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiClient.class);
    private final CloseableHttpClient httpClient;
    private final String apiUrl;
    private final String apiKeyString;
    private final String modelName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiClient(CloseableHttpClient httpClient, String apiUrl, String modelName, String apiKeyString) {
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
        this.modelName = modelName;
        this.apiKeyString = apiKeyString;
    }

    @Override
    public ChatMessage chat(List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest();
        request.setMessages(messages);
        request.setModel(this.modelName);
        request.setStream(false);
        
        try {
            HttpPost httpPost = new HttpPost(new URI(apiUrl));
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "*/*");
            httpPost.setHeader("Authorization", "Bearer " + apiKeyString);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request)));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    ChatResponse chatResponse = objectMapper.readValue(responseBody, ChatResponse.class);
                    ChatMessage message = chatResponse.getMessage();
                    if (message == null) {
                        logger.error("Received null message from OpenAI API");
                        throw new RuntimeException("Received null message from OpenAI API");
                    }
                    return message;
                } else {
                    String errorBody = EntityUtils.toString(response.getEntity());
                    logger.error("OpenAI API request failed with status {}: {}", statusCode, errorBody);
                    throw new RuntimeException("OpenAI API request failed with status " + statusCode);
                }
            }
        } catch (Exception e) {
            logger.error("Error communicating with OpenAI API", e);
            throw new RuntimeException("Error communicating with OpenAI API: " + e.getMessage(), e);
        }
    }
}
