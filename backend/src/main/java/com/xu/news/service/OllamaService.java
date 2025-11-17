package com.xu.news.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.ollama.url}")
    private String ollamaUrl;
    
    @Value("${app.ollama.model}")
    private String model;
    
    @Data
    public static class GenerateRequest {
        private String model;
        private String prompt;
        private boolean stream = false;
        private Map<String, Object> options;
    }
    
    @Data
    public static class GenerateResponse {
        private String model;
        private String response;
        private boolean done;
    }
    
    /**
     * Generate answer using Ollama
     */
    public String generateAnswer(String query, List<String> context) {
        try {
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("基于以下信息回答问题。如果信息不足，请说明。\n\n");
            promptBuilder.append("问题：").append(query).append("\n\n");
            promptBuilder.append("参考信息：\n");
            
            for (int i = 0; i < context.size(); i++) {
                promptBuilder.append(i + 1).append(". ").append(context.get(i)).append("\n\n");
            }
            
            promptBuilder.append("请用中文回答：");
            
            GenerateRequest request = new GenerateRequest();
            request.setModel(model);
            request.setPrompt(promptBuilder.toString());
            request.setStream(false);
            
            WebClient client = webClientBuilder.baseUrl(ollamaUrl).build();
            
            GenerateResponse response = client.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GenerateResponse.class)
                    .block();
            
            return response != null ? response.getResponse() : "无法生成回答";
            
        } catch (Exception e) {
            log.error("Failed to generate answer with Ollama", e);
            return "生成回答时出错：" + e.getMessage();
        }
    }
    
    /**
     * Summarize search results
     */
    public String summarize(String query, List<String> contents) {
        try {
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("请总结以下搜索结果，针对问题提供简洁的回答。\n\n");
            promptBuilder.append("问题：").append(query).append("\n\n");
            promptBuilder.append("搜索结果：\n");
            
            for (String content : contents) {
                promptBuilder.append("- ").append(content).append("\n");
            }
            
            promptBuilder.append("\n请用中文提供总结：");
            
            GenerateRequest request = new GenerateRequest();
            request.setModel(model);
            request.setPrompt(promptBuilder.toString());
            request.setStream(false);
            
            WebClient client = webClientBuilder.baseUrl(ollamaUrl).build();
            
            GenerateResponse response = client.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GenerateResponse.class)
                    .block();
            
            return response != null ? response.getResponse() : "无法生成总结";
            
        } catch (Exception e) {
            log.error("Failed to summarize with Ollama", e);
            return "生成总结时出错：" + e.getMessage();
        }
    }
    
    /**
     * Check if Ollama is available
     */
    public boolean isAvailable() {
        try {
            WebClient client = webClientBuilder.baseUrl(ollamaUrl).build();
            Map<String, Object> response = client.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null;
        } catch (Exception e) {
            log.warn("Ollama service is not available", e);
            return false;
        }
    }
}

