package com.blakersfield.gameagentsystem.llm.clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.blakersfield.gameagentsystem.llm.request.ChatMessage;
import com.blakersfield.gameagentsystem.llm.request.ChatRequest;
import com.blakersfield.gameagentsystem.llm.request.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAiClient implements LLMClient {
    private CloseableHttpClient httpClient;
    private String apiUrl;
    private String apiKeyString;
    private String modelName;
    private ObjectMapper objectMapper = new ObjectMapper();

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
    request.setModel(this.modelName); //TODO fix, enums, etc
    request.setStream(false);
    try{
        HttpPost httpPost = new HttpPost(new URI(apiUrl)); 
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("Accept","*/*");
        httpPost.setHeader("Authorization", "Bearer " + apiKeyString);
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request)));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)){
            if (response.getStatusLine().getStatusCode()>=200 && response.getStatusLine().getStatusCode()<300){
                return objectMapper.readValue(EntityUtils.toString(response.getEntity()),ChatResponse.class).getMessage();
            }
        }
    } catch (Exception e){
        e.printStackTrace();
    }
    return null;
    }
    
}
