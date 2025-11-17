package com.xu.news.dto;

import com.xu.news.entity.NewsArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String url;
    private String source;
    private String author;
    private LocalDateTime publishedAt;
    private Set<String> tags;
    private NewsArticle.ContentType contentType;
    private LocalDateTime createdAt;
    
    public static NewsArticleDto fromEntity(NewsArticle article) {
        return NewsArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .url(article.getUrl())
                .source(article.getSource())
                .author(article.getAuthor())
                .publishedAt(article.getPublishedAt())
                .tags(article.getTags())
                .contentType(article.getContentType())
                .createdAt(article.getCreatedAt())
                .build();
    }
}

