package com.cfs.bms.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Client for xAI Grok API.
 * Enhances chatbot responses with natural language processing.
 * Falls back gracefully if API is unavailable.
 */
@Component
public class GrokApiClient {

    private static final Logger logger = LoggerFactory.getLogger(GrokApiClient.class);

    @Value("${grok.api.key:}")
    private String apiKey;

    @Value("${grok.api.url:https://api.x.ai/v1/chat/completions}")
    private String apiUrl;

    private final WebClient webClient;

    public GrokApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Calls Grok API with a system prompt + user message + conversation context.
     * Returns the AI response text, or null if unavailable.
     */
    public String enhanceResponse(String systemPrompt, String userMessage, List<String> conversationHistory) {
        if (!isAvailable()) {
            logger.debug("Grok API key not set, skipping AI enhancement");
            return null;
        }

        try {
            // Build message list
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // Add last few exchanges for context (max 6 messages)
            int historyStart = Math.max(0, conversationHistory.size() - 6);
            for (int i = historyStart; i < conversationHistory.size(); i++) {
                String entry = conversationHistory.get(i);
                if (entry.startsWith("user: ")) {
                    messages.add(Map.of("role", "user", "content", entry.substring(6)));
                } else if (entry.startsWith("assistant: ")) {
                    messages.add(Map.of("role", "assistant", "content", entry.substring(11)));
                }
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = Map.of(
                    "model", "grok-beta",
                    "messages", messages,
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            Map<?, ?> response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> messageMap = (Map<?, ?>) choice.get("message");
                    if (messageMap != null) {
                        return (String) messageMap.get("content");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Grok API call failed: {} - using fallback", e.getMessage());
        }

        return null;
    }
}
