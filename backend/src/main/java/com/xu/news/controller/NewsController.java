package com.xu.news.controller;

import com.xu.news.dto.NewsArticleDto;
import com.xu.news.dto.UpdateArticleRequest;
import com.xu.news.entity.NewsArticle;
import com.xu.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping
    public ResponseEntity<Page<NewsArticleDto>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) NewsArticle.ContentType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NewsArticle> articles;
        
        if (tag != null) {
            articles = newsService.getArticlesByTag(tag, pageRequest);
        } else if (type != null && startDate != null && endDate != null) {
            articles = newsService.getArticlesByTypeAndDateRange(type, startDate, endDate, pageRequest);
        } else if (type != null) {
            articles = newsService.getArticlesByType(type, pageRequest);
        } else if (startDate != null && endDate != null) {
            articles = newsService.getArticlesByDateRange(startDate, endDate, pageRequest);
        } else {
            articles = newsService.getAllArticles(pageRequest);
        }
        
        Page<NewsArticleDto> dtoPage = articles.map(NewsArticleDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleDto> getArticle(@PathVariable Long id) {
        return newsService.getArticleById(id)
                .map(NewsArticleDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<NewsArticleDto> updateArticle(
            @PathVariable Long id,
            @RequestBody UpdateArticleRequest request
    ) {
        NewsArticle article = new NewsArticle();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setTags(request.getTags());
        article.setSource(request.getSource());
        article.setAuthor(request.getAuthor());
        
        NewsArticle updated = newsService.updateArticle(id, article);
        return ResponseEntity.ok(NewsArticleDto.fromEntity(updated));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        newsService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteArticles(@RequestBody List<Long> ids) {
        newsService.deleteArticles(ids);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        return ResponseEntity.ok(newsService.getAllSources());
    }
    
    @GetMapping("/tags/top")
    public ResponseEntity<List<String>> getTopTags(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(newsService.getTopTags(limit));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalArticles = newsService.countArticles();
        List<String> sources = newsService.getAllSources();
        List<String> topTags = newsService.getTopTags(10);
        
        return ResponseEntity.ok(Map.of(
                "totalArticles", totalArticles,
                "totalSources", sources.size(),
                "topTags", topTags
        ));
    }
}

