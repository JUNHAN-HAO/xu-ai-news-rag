package com.xu.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleRequest {
    private String title;
    private String content;
    private String summary;
    private Set<String> tags;
    private String source;
    private String author;
}

