package com.xu.news.controller;

import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.dto.SearchResultDto;
import com.xu.news.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class QueryController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    private final WebSearchService webSearchService;
    private final OllamaService ollamaService;
    
    private static final double MIN_SCORE_THRESHOLD = 0.5;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
        log.info("Query request: {}", request.getQuery());
        
        try {
            List<SearchResultDto> results = new ArrayList<>();
            boolean fromWeb = false;
            
            // Step 1: Search in knowledge base
            if (request.getUseRerank()) {
                // Search with reranking
                List<PythonServiceClient.RerankResult> rerankResults = 
                    knowledgeBaseService.searchWithRerank(request.getQuery(), 20, request.getTopK());
                
                // Check if results are good enough
                boolean hasGoodResults = !rerankResults.isEmpty() && 
                                        rerankResults.get(0).getScore() >= MIN_SCORE_THRESHOLD;
                
                if (hasGoodResults) {
                    results = rerankResults.stream()
                            .map(result -> SearchResultDto.builder()
                                    .id(result.getId())
                                    .title((String) result.getMetadata().get("title"))
                                    .content(result.getText())
                                    .score(result.getScore())
                                    .url((String) result.getMetadata().get("url"))
                                    .source((String) result.getMetadata().get("source"))
                                    .metadata(result.getMetadata())
                                    .build())
                            .collect(Collectors.toList());
                }
            } else {
                // Simple semantic search
                List<PythonServiceClient.SearchResult> searchResults = 
                    knowledgeBaseService.search(request.getQuery(), request.getTopK());
                
                boolean hasGoodResults = !searchResults.isEmpty() && 
                                        searchResults.get(0).getScore() >= MIN_SCORE_THRESHOLD;
                
                if (hasGoodResults) {
                    results = searchResults.stream()
                            .map(result -> SearchResultDto.builder()
                                    .id(result.getId())
                                    .title((String) result.getMetadata().get("title"))
                                    .content(result.getText())
                                    .score(result.getScore())
                                    .url((String) result.getMetadata().get("url"))
                                    .source((String) result.getMetadata().get("source"))
                                    .metadata(result.getMetadata())
                                    .build())
                            .collect(Collectors.toList());
                }
            }
            
            // Step 2: If no good results, try web search
            if (results.isEmpty() && request.getAllowWebSearch() && webSearchService.isAvailable()) {
                log.info("No good results in knowledge base, trying web search");
                fromWeb = true;
                
                List<WebSearchService.WebSearchResult> webResults = 
                    webSearchService.search(request.getQuery(), 3);
                
                results = webResults.stream()
                        .map(result -> SearchResultDto.builder()
                                .title(result.getTitle())
                                .content(result.getSnippet())
                                .score(0.0)
                                .url(result.getUrl())
                                .source(result.getSource())
                                .build())
                        .collect(Collectors.toList());
            }
            
            // Step 3: Generate answer using LLM
            String answer = "";
            if (!results.isEmpty() && ollamaService.isAvailable()) {
                List<String> contexts = results.stream()
                        .map(SearchResultDto::getContent)
                        .limit(3)
                        .collect(Collectors.toList());
                
                if (fromWeb) {
                    answer = ollamaService.summarize(request.getQuery(), contexts);
                } else {
                    answer = ollamaService.generateAnswer(request.getQuery(), contexts);
                }
            }
            
            // Build response
            QueryResponse response = QueryResponse.builder()
                    .query(request.getQuery())
                    .results(results)
                    .answer(answer)
                    .fromWeb(fromWeb)
                    .resultCount(results.size())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Query failed", e);
            return ResponseEntity.status(500)
                    .body(QueryResponse.builder()
                            .query(request.getQuery())
                            .results(new ArrayList<>())
                            .answer("查询失败：" + e.getMessage())
                            .fromWeb(false)
                            .resultCount(0)
                            .build());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "ollama", ollamaService.isAvailable(),
                "webSearch", webSearchService.isAvailable()
        ));
    }
}

