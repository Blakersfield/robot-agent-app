package com.blakersfield.gameagentsystem.llm.clients;

import java.util.List;
import com.blakersfield.gameagentsystem.llm.request.*;

public interface LLMClient {
    public ChatMessage chat(List<ChatMessage> messages);
}
