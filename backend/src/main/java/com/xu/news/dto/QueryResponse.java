package com.xu.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    private String query;
    private List<SearchResultDto> results;
    private String answer;
    private Boolean fromWeb;
    private Integer resultCount;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SearchResultDto {
    private String id;
    private String title;
    private String content;
    private Double score;
    private String url;
    private String source;
    private Map<String, Object> metadata;
}

