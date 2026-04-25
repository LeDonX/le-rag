package com.le.rag.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelConfig {

    @Bean
    @Primary
    public ChatModel primaryChatModel(ChatModel ollamaChatModel) {
        return ollamaChatModel;
    }
}
