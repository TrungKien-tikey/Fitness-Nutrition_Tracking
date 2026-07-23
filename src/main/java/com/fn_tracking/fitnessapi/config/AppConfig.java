package com.fn_tracking.fitnessapi.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model.name:gemini-1.5-flash}") String modelName
    ) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
