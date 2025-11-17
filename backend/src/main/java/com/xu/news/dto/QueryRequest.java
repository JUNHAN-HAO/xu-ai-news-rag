package com.xu.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    private String query;
    private Integer topK = 5;
    private Boolean useRerank = true;
    private Boolean allowWebSearch = true;
}

