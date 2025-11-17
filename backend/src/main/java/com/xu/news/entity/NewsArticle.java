package com.xu.news.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "news_articles", indexes = {
    @Index(name = "idx_article_url", columnList = "url"),
    @Index(name = "idx_article_published", columnList = "publishedAt"),
    @Index(name = "idx_article_source", columnList = "source")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(nullable = false, unique = true, length = 2048)
    private String url;
    
    @Column(nullable = false)
    private String source;
    
    @Column
    private String author;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContentType contentType = ContentType.RSS;
    
    @Column(name = "vector_id")
    private String vectorId;  // ChromaDB document ID
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ContentType {
        RSS,
        WEB_SCRAPE,
        EXCEL,
        PDF,
        MANUAL,
        TEXT
    }
}

