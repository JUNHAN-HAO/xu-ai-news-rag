package com.xu.news.repository;

import com.xu.news.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<NewsArticle, Long> {
    
    Optional<NewsArticle> findByUrl(String url);
    
    boolean existsByUrl(String url);
    
    Page<NewsArticle> findByContentType(NewsArticle.ContentType contentType, Pageable pageable);
    
    Page<NewsArticle> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<NewsArticle> findByContentTypeAndPublishedAtBetween(
        NewsArticle.ContentType contentType, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    @Query("SELECT a FROM NewsArticle a WHERE :tag MEMBER OF a.tags")
    Page<NewsArticle> findByTag(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a WHERE a.source = :source ORDER BY a.publishedAt DESC")
    List<NewsArticle> findBySourceOrderByPublishedAtDesc(@Param("source") String source);
    
    @Query("SELECT DISTINCT a.source FROM NewsArticle a")
    List<String> findAllDistinctSources();
    
    @Query("SELECT t FROM NewsArticle a JOIN a.tags t GROUP BY t ORDER BY COUNT(t) DESC")
    List<String> findTopTags(Pageable pageable);
}

