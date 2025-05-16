package com.blakersfield.gameagentsystem.llm.clients;

import java.net.URI;
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

public class OllamaClient implements LLMClient{
    private CloseableHttpClient httpClient;
    private String apiUrl;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String modelName;

    public OllamaClient(CloseableHttpClient httpClient, String baseUrl, Integer port, String modelName){
        if (port == null || port == 0){
            this.apiUrl = baseUrl + "/api/chat";
        } else {
            this.apiUrl = baseUrl + ":" + port + "/api/chat";
        }
        this.httpClient = httpClient;
        this.modelName = modelName;
    }

    public ChatMessage chat(List<ChatMessage> messages){
    ChatRequest request = new ChatRequest();
    request.setMessages(messages);
    request.setModel(modelName); //TODO fix, enums, etc
    request.setStream(false);
    try{
        HttpPost httpPost = new HttpPost(new URI(apiUrl)); //this will need to be extracted for the different auth etc. 
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("Accept","*/*");
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
    };
}
