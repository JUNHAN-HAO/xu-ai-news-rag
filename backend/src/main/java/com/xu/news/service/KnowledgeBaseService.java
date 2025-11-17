package com.xu.news.service;

import com.xu.news.entity.NewsArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {
    
    private final PythonServiceClient pythonClient;
    private final NewsService newsService;
    
    /**
     * Add article to knowledge base (ChromaDB)
     */
    public void addArticleToKnowledgeBase(NewsArticle article) {
        try {
            PythonServiceClient.AddDocumentRequest docRequest = new PythonServiceClient.AddDocumentRequest();
            docRequest.setId(String.valueOf(article.getId()));
            
            // Combine title and content for better semantic search
            String text = article.getTitle() + "\n\n" + 
                         (article.getContent() != null ? article.getContent() : article.getSummary());
            docRequest.setText(text);
            
            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", article.getTitle());
            metadata.put("url", article.getUrl());
            metadata.put("source", article.getSource());
            metadata.put("published_at", article.getPublishedAt() != null ? article.getPublishedAt().toString() : "");
            metadata.put("content_type", article.getContentType().name());
            if (article.getAuthor() != null) {
                metadata.put("author", article.getAuthor());
            }
            if (!article.getTags().isEmpty()) {
                metadata.put("tags", String.join(",", article.getTags()));
            }
            docRequest.setMetadata(metadata);
            
            pythonClient.addDocuments(List.of(docRequest)).block();
            log.info("Added article {} to knowledge base", article.getId());
        } catch (Exception e) {
            log.error("Failed to add article to knowledge base: {}", article.getId(), e);
            throw new RuntimeException("Failed to add article to knowledge base", e);
        }
    }
    
    /**
     * Add multiple articles to knowledge base
     */
    public int addArticlesToKnowledgeBase(List<NewsArticle> articles) {
        try {
            List<PythonServiceClient.AddDocumentRequest> docRequests = articles.stream()
                    .map(article -> {
                        PythonServiceClient.AddDocumentRequest docRequest = new PythonServiceClient.AddDocumentRequest();
                        docRequest.setId(String.valueOf(article.getId()));
                        
                        String text = article.getTitle() + "\n\n" + 
                                     (article.getContent() != null ? article.getContent() : article.getSummary());
                        docRequest.setText(text);
                        
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("title", article.getTitle());
                        metadata.put("url", article.getUrl());
                        metadata.put("source", article.getSource());
                        metadata.put("published_at", article.getPublishedAt() != null ? article.getPublishedAt().toString() : "");
                        metadata.put("content_type", article.getContentType().name());
                        if (article.getAuthor() != null) {
                            metadata.put("author", article.getAuthor());
                        }
                        if (!article.getTags().isEmpty()) {
                            metadata.put("tags", String.join(",", article.getTags()));
                        }
                        docRequest.setMetadata(metadata);
                        
                        return docRequest;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = pythonClient.addDocuments(docRequests).block();
            int count = result != null ? (int) result.getOrDefault("count", 0) : 0;
            log.info("Added {} articles to knowledge base", count);
            return count;
        } catch (Exception e) {
            log.error("Failed to add articles to knowledge base", e);
            throw new RuntimeException("Failed to add articles to knowledge base", e);
        }
    }
    
    /**
     * Semantic search in knowledge base
     */
    public List<PythonServiceClient.SearchResult> search(String query, int topK) {
        try {
            PythonServiceClient.SearchResponse response = pythonClient.search(query, topK).block();
            return response != null ? response.getResults() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to search in knowledge base", e);
            throw new RuntimeException("Failed to search in knowledge base", e);
        }
    }
    
    /**
     * Semantic search with reranking
     */
    public List<PythonServiceClient.RerankResult> searchWithRerank(String query, int topK, int rerankTopK) {
        try {
            // First, get initial results
            PythonServiceClient.SearchResponse searchResponse = pythonClient.search(query, topK).block();
            if (searchResponse == null || searchResponse.getResults().isEmpty()) {
                return new ArrayList<>();
            }
            
            // Prepare documents for reranking
            List<Map<String, Object>> documents = searchResponse.getResults().stream()
                    .map(result -> {
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("id", result.getId());
                        doc.put("text", result.getText());
                        doc.put("metadata", result.getMetadata());
                        return doc;
                    })
                    .collect(Collectors.toList());
            
            // Rerank
            PythonServiceClient.RerankResponse rerankResponse = pythonClient.rerank(query, documents, rerankTopK).block();
            return rerankResponse != null ? rerankResponse.getResults() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to search with rerank", e);
            throw new RuntimeException("Failed to search with rerank", e);
        }
    }
    
    /**
     * Remove article from knowledge base
     */
    public void removeArticleFromKnowledgeBase(Long articleId) {
        try {
            pythonClient.deleteDocuments(List.of(String.valueOf(articleId))).block();
            log.info("Removed article {} from knowledge base", articleId);
        } catch (Exception e) {
            log.error("Failed to remove article from knowledge base: {}", articleId, e);
            throw new RuntimeException("Failed to remove article from knowledge base", e);
        }
    }
    
    /**
     * Remove multiple articles from knowledge base
     */
    public void removeArticlesFromKnowledgeBase(List<Long> articleIds) {
        try {
            List<String> ids = articleIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            pythonClient.deleteDocuments(ids).block();
            log.info("Removed {} articles from knowledge base", articleIds.size());
        } catch (Exception e) {
            log.error("Failed to remove articles from knowledge base", e);
            throw new RuntimeException("Failed to remove articles from knowledge base", e);
        }
    }
    
    /**
     * Get clustering analysis
     */
    public PythonServiceClient.ClusterResponse getClusterAnalysis(int nClusters) {
        try {
            // Get all articles
            List<NewsArticle> articles = newsService.getAllArticles(
                    org.springframework.data.domain.PageRequest.of(0, 1000)
            ).getContent();
            
            if (articles.isEmpty()) {
                return new PythonServiceClient.ClusterResponse();
            }
            
            // Extract texts
            List<String> texts = articles.stream()
                    .map(article -> article.getTitle() + " " + 
                         (article.getSummary() != null ? article.getSummary() : ""))
                    .collect(Collectors.toList());
            
            // Perform clustering
            return pythonClient.cluster(texts, nClusters).block();
        } catch (Exception e) {
            log.error("Failed to get cluster analysis", e);
            throw new RuntimeException("Failed to get cluster analysis", e);
        }
    }
}

