package com.blakersfield.gameagentsystem.llm.clients;

import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import com.blakersfield.gameagentsystem.llm.request.*;
import com.fasterxml.jackson.databind.ObjectMapper;

abstract class LLMClient {
    private CloseableHttpClient httpClient;
    private String apiUrl;
    private ObjectMapper objectMapper = new ObjectMapper();
    //todo add model fields etc in child classes with enums

    public LLMClient(CloseableHttpClient httpClient, String apiUrl){
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
    }
    public ChatMessage chat(List<ChatMessage> messages){
        ChatRequest request = new ChatRequest();
        request.setMessages(messages);
        request.setModel(apiUrl);
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
            //TODO return error message
        }
        return null;
    };
    // public ChatMessage generate(ChatMessage message){};
}
