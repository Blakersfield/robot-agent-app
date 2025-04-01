package com.blakersfield.gameagentsystem.llm.clients;

import org.apache.http.impl.client.CloseableHttpClient;

public class OllamaClient extends LLMClient{
    public OllamaClient(CloseableHttpClient httpClient, String apiUrl){
        super(httpClient, apiUrl); 
    }
}
