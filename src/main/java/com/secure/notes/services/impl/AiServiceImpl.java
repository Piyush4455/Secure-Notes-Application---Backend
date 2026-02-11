package com.secure.notes.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.secure.notes.services.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.chat.options.model:llama3-70b-8192}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double temperature;

    public AiServiceImpl(
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public String convertToHinglish(String text) {
        // Prompt for conversion
        String prompt = "Convert the following text into Hinglish (Hindi words in English script).\n" +
                "Rules:\n" +
                "- Output ONLY the converted sentence. Nothing else.\n" +
                "- Do not add any explanation or notes.\n" +
                "- Do not add quotation marks around the output.\n\n" +
                "Text: " + text + "\nHinglish:";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("stream", false);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        body.set("messages", messages);

        String requestBody = body.toString();
        logger.info("Groq request body: {}", requestBody);

        try {
            Mono<String> response = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class).map(errorBody -> {
                                logger.error("Groq API error - status: {}, body: {}",
                                        clientResponse.statusCode(), errorBody);
                                return new RuntimeException("Groq API returned " +
                                        clientResponse.statusCode() + ": " + errorBody);
                            })
                    )
                    .bodyToMono(String.class);

            String result = response.block();
            logger.info("Groq response: {}", result);

            // Extract the actual text from JSON response
            String output = objectMapper.readTree(result)
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText().trim();

            return output;

        } catch (Exception e) {
            logger.error("Groq API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }
}