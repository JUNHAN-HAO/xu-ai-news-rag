package com.xu.news.service;

import com.xu.news.entity.NewsArticle;
import com.xu.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    
    private final NewsRepository newsRepository;
    
    @Transactional
    public NewsArticle saveArticle(NewsArticle article) {
        // Check for duplicates
        if (newsRepository.existsByUrl(article.getUrl())) {
            log.debug("Article already exists: {}", article.getUrl());
            return newsRepository.findByUrl(article.getUrl()).orElse(null);
        }
        
        return newsRepository.save(article);
    }
    
    @Transactional
    public List<NewsArticle> saveArticles(List<NewsArticle> articles) {
        return articles.stream()
                .filter(article -> !newsRepository.existsByUrl(article.getUrl()))
                .map(newsRepository::save)
                .toList();
    }
    
    public Page<NewsArticle> getAllArticles(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }
    
    public Page<NewsArticle> getArticlesByType(NewsArticle.ContentType type, Pageable pageable) {
        return newsRepository.findByContentType(type, pageable);
    }
    
    public Page<NewsArticle> getArticlesByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        return newsRepository.findByPublishedAtBetween(start, end, pageable);
    }
    
    public Page<NewsArticle> getArticlesByTypeAndDateRange(
            NewsArticle.ContentType type,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        return newsRepository.findByContentTypeAndPublishedAtBetween(type, start, end, pageable);
    }
    
    public Page<NewsArticle> getArticlesByTag(String tag, Pageable pageable) {
        return newsRepository.findByTag(tag, pageable);
    }
    
    public Optional<NewsArticle> getArticleById(Long id) {
        return newsRepository.findById(id);
    }
    
    @Transactional
    public NewsArticle updateArticle(Long id, NewsArticle updatedArticle) {
        NewsArticle article = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
        
        if (updatedArticle.getTitle() != null) {
            article.setTitle(updatedArticle.getTitle());
        }
        if (updatedArticle.getContent() != null) {
            article.setContent(updatedArticle.getContent());
        }
        if (updatedArticle.getSummary() != null) {
            article.setSummary(updatedArticle.getSummary());
        }
        if (updatedArticle.getTags() != null) {
            article.setTags(updatedArticle.getTags());
        }
        if (updatedArticle.getSource() != null) {
            article.setSource(updatedArticle.getSource());
        }
        if (updatedArticle.getAuthor() != null) {
            article.setAuthor(updatedArticle.getAuthor());
        }
        
        return newsRepository.save(article);
    }
    
    @Transactional
    public void deleteArticle(Long id) {
        newsRepository.deleteById(id);
    }
    
    @Transactional
    public void deleteArticles(List<Long> ids) {
        ids.forEach(newsRepository::deleteById);
    }
    
    public List<String> getAllSources() {
        return newsRepository.findAllDistinctSources();
    }
    
    public List<String> getTopTags(int limit) {
        return newsRepository.findTopTags(Pageable.ofSize(limit));
    }
    
    public long countArticles() {
        return newsRepository.count();
    }
}

