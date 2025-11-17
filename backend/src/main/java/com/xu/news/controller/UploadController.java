package com.xu.news.controller;

import com.xu.news.entity.NewsArticle;
import com.xu.news.service.KnowledgeBaseService;
import com.xu.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class UploadController {
    
    private final NewsService newsService;
    private final KnowledgeBaseService knowledgeBaseService;
    
    @PostMapping("/excel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        log.info("Uploading Excel file: {}", file.getOriginalFilename());
        
        try {
            List<NewsArticle> articles = parseExcelFile(file);
            
            // Save to database
            List<NewsArticle> saved = newsService.saveArticles(articles);
            
            // Add to knowledge base
            if (!saved.isEmpty()) {
                knowledgeBaseService.addArticlesToKnowledgeBase(saved);
            }
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "count", saved.size(),
                    "message", "成功上传 " + saved.size() + " 条数据"
            ));
            
        } catch (Exception e) {
            log.error("Failed to upload Excel file", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/text")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadText(@RequestBody Map<String, String> request) {
        log.info("Uploading text content");
        
        try {
            String title = request.get("title");
            String content = request.get("content");
            String url = request.getOrDefault("url", "manual-" + UUID.randomUUID());
            String source = request.getOrDefault("source", "Manual Upload");
            
            NewsArticle article = NewsArticle.builder()
                    .title(title)
                    .content(content)
                    .summary(content.length() > 200 ? content.substring(0, 200) + "..." : content)
                    .url(url)
                    .source(source)
                    .publishedAt(LocalDateTime.now())
                    .contentType(NewsArticle.ContentType.MANUAL)
                    .tags(new HashSet<>())
                    .build();
            
            NewsArticle saved = newsService.saveArticle(article);
            
            if (saved != null) {
                knowledgeBaseService.addArticleToKnowledgeBase(saved);
                
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "id", saved.getId(),
                        "message", "成功上传文本内容"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "status", "duplicate",
                        "message", "内容已存在"
                ));
            }
            
        } catch (Exception e) {
            log.error("Failed to upload text", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Parse Excel file to NewsArticle list
     */
    private List<NewsArticle> parseExcelFile(MultipartFile file) throws Exception {
        List<NewsArticle> articles = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String title = getCellValue(row.getCell(0));
                    String content = getCellValue(row.getCell(1));
                    String url = getCellValue(row.getCell(2));
                    String source = getCellValue(row.getCell(3));
                    
                    // Validation
                    if (title == null || title.isEmpty()) continue;
                    if (url == null || url.isEmpty()) {
                        url = "excel-" + UUID.randomUUID();
                    }
                    if (source == null || source.isEmpty()) {
                        source = "Excel Upload";
                    }
                    
                    NewsArticle article = NewsArticle.builder()
                            .title(title)
                            .content(content)
                            .summary(content != null && content.length() > 200 ? 
                                    content.substring(0, 200) + "..." : content)
                            .url(url)
                            .source(source)
                            .publishedAt(LocalDateTime.now())
                            .contentType(NewsArticle.ContentType.EXCEL)
                            .tags(new HashSet<>())
                            .build();
                    
                    articles.add(article);
                    
                } catch (Exception e) {
                    log.warn("Failed to parse Excel row {}", i, e);
                }
            }
        }
        
        return articles;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}

