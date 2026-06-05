package top.ortus.timemark.backend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AIClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public AIClient(RestTemplateBuilder restTemplateBuilder,
                    @Value("${AI_API_URL:}") String apiUrl,
                    @Value("${AI_API_KEY:}") String apiKey,
                    @Value("${AI_MODEL:deepseek-chat}") String model) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(45))
                .build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public Optional<String> chat(String prompt) {
        if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)) {
            // TODO Configure AI_API_URL and AI_API_KEY to replace this mock fallback with a real model call.
            log.info("AI config missing, skip external model call");
            return Optional.empty();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "你是拾光旅行的 AI 助手。请严格按照用户要求输出。"),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", 0.7
            );
            String response = restTemplate.postForObject(apiUrl, new HttpEntity<>(body, headers), String.class);
            return StringUtils.hasText(response) ? Optional.of(response) : Optional.empty();
        } catch (Exception ex) {
            log.warn("AI model call failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
