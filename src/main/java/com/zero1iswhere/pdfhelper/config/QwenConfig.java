package com.zero1iswhere.pdfhelper.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
public class QwenConfig {

    @Bean
    public ChatClient QwenChatClient(OpenAiChatModel openAiChatModel, ChatMemory mongoChatMemory) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(new ClassPathResource("systemPrompt.txt"))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(mongoChatMemory)
                        .build())
                .build();
    }
}