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
public class PythonServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.python.service.url}")
    private String pythonServiceUrl;
    
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl(pythonServiceUrl).build();
    }
    
    // Request/Response DTOs
    @Data
    public static class AddDocumentRequest {
        private String id;
        private String text;
        private Map<String, Object> metadata;
    }
    
    @Data
    public static class AddDocumentsRequest {
        private List<AddDocumentRequest> documents;
    }
    
    @Data
    public static class SearchRequest {
        private String query;
        private int top_k;
    }
    
    @Data
    public static class SearchResult {
        private String id;
        private String text;
        private double score;
        private Map<String, Object> metadata;
    }
    
    @Data
    public static class SearchResponse {
        private List<SearchResult> results;
    }
    
    @Data
    public static class RerankRequest {
        private String query;
        private List<Map<String, Object>> documents;
        private int top_k;
    }
    
    @Data
    public static class RerankResult {
        private String id;
        private String text;
        private double score;
        private Map<String, Object> metadata;
    }
    
    @Data
    public static class RerankResponse {
        private List<RerankResult> results;
    }
    
    @Data
    public static class ClusterRequest {
        private List<String> texts;
        private int n_clusters;
    }
    
    @Data
    public static class ClusterInfo {
        private int cluster_id;
        private List<String> keywords;
        private int count;
    }
    
    @Data
    public static class ClusterResponse {
        private List<ClusterInfo> clusters;
        private List<String> top_keywords;
    }
    
    @Data
    public static class DeleteDocumentRequest {
        private List<String> ids;
    }
    
    // Service methods
    public Mono<Map<String, Object>> addDocuments(List<AddDocumentRequest> documents) {
        AddDocumentsRequest request = new AddDocumentsRequest();
        request.setDocuments(documents);
        
        return getWebClient()
                .post()
                .uri("/documents/add")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Error adding documents to Python service", e));
    }
    
    public Mono<SearchResponse> search(String query, int topK) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setTop_k(topK);
        
        return getWebClient()
                .post()
                .uri("/search")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .doOnError(e -> log.error("Error searching in Python service", e));
    }
    
    public Mono<RerankResponse> rerank(String query, List<Map<String, Object>> documents, int topK) {
        RerankRequest request = new RerankRequest();
        request.setQuery(query);
        request.setDocuments(documents);
        request.setTop_k(topK);
        
        return getWebClient()
                .post()
                .uri("/rerank")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RerankResponse.class)
                .doOnError(e -> log.error("Error reranking in Python service", e));
    }
    
    public Mono<ClusterResponse> cluster(List<String> texts, int nClusters) {
        ClusterRequest request = new ClusterRequest();
        request.setTexts(texts);
        request.setN_clusters(nClusters);
        
        return getWebClient()
                .post()
                .uri("/cluster")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClusterResponse.class)
                .doOnError(e -> log.error("Error clustering in Python service", e));
    }
    
    public Mono<Map<String, Object>> deleteDocuments(List<String> ids) {
        DeleteDocumentRequest request = new DeleteDocumentRequest();
        request.setIds(ids);
        
        return getWebClient()
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri("/documents")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Error deleting documents from Python service", e));
    }
    
    public Mono<Map<String, Object>> getHealth() {
        return getWebClient()
                .get()
                .uri("/health")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Error checking Python service health", e));
    }
    
    public Mono<Map<String, Integer>> getDocumentCount() {
        return getWebClient()
                .get()
                .uri("/documents/count")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Error getting document count from Python service", e));
    }
}

