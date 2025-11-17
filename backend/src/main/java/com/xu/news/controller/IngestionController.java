package com.xu.news.controller;

import com.xu.news.service.EmailService;
import com.xu.news.service.RssIngestionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class IngestionController {
    
    private final RssIngestionService rssIngestionService;
    private final EmailService emailService;
    
    @Value("${app.email.from}")
    private String adminEmail;
    
    @Data
    public static class IngestionRequest {
        private List<String> feedUrls;
    }
    
    @PostMapping("/rss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> ingestRss(@RequestBody IngestionRequest request) {
        log.info("Manual RSS ingestion triggered for {} feeds", request.getFeedUrls().size());
        
        try {
            int count = rssIngestionService.ingestFromRssFeeds(request.getFeedUrls());
            
            // Send notification
            if (count > 0) {
                String summary = String.format("手动触发抓取成功入库 %d 条新闻", count);
                emailService.sendIngestionNotification(adminEmail, count, summary);
            }
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "ingestedCount", count,
                    "message", "成功入库 " + count + " 条新闻"
            ));
            
        } catch (Exception e) {
            log.error("RSS ingestion failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}

