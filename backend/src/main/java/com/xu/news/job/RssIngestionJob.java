package com.xu.news.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.xu.news.service.EmailService;
import com.xu.news.service.RssIngestionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RssIngestionJob {
    
    private final RssIngestionService rssIngestionService;
    private final EmailService emailService;
    
    @Value("${app.rss.feeds-file:../feeds.yaml}")
    private String feedsFile;
    
    @Value("${app.email.from}")
    private String adminEmail;
    
    /**
     * Scheduled RSS ingestion job
     * Runs every 6 hours by default
     */
    @Scheduled(cron = "${app.rss.schedule.cron:0 0 */6 * * *}")
    public void runRssIngestion() {
        log.info("Starting scheduled RSS ingestion job");
        
        try {
            List<String> feedUrls = loadFeedUrls();
            if (feedUrls.isEmpty()) {
                log.warn("No RSS feeds configured");
                return;
            }
            
            log.info("Loaded {} RSS feeds", feedUrls.size());
            
            int ingestedCount = rssIngestionService.ingestFromRssFeeds(feedUrls);
            
            log.info("RSS ingestion completed. Ingested {} articles", ingestedCount);
            
            // Send email notification
            if (ingestedCount > 0) {
                String summary = String.format("成功抓取并入库 %d 条新闻", ingestedCount);
                emailService.sendIngestionNotification(adminEmail, ingestedCount, summary);
            }
            
        } catch (Exception e) {
            log.error("RSS ingestion job failed", e);
            // Send error notification
            emailService.sendCustomNotification(
                adminEmail,
                "RSS抓取任务失败",
                "RSS新闻抓取任务执行失败：" + e.getMessage()
            );
        }
    }
    
    /**
     * Load RSS feed URLs from YAML config file
     */
    private List<String> loadFeedUrls() {
        try {
            File file = new File(feedsFile);
            if (!file.exists()) {
                log.warn("Feeds file not found: {}", feedsFile);
                return new ArrayList<>();
            }
            
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> config = mapper.readValue(file, Map.class);
            
            Object feedsObj = config.get("feeds");
            if (feedsObj instanceof List) {
                return (List<String>) feedsObj;
            }
            
        } catch (Exception e) {
            log.error("Failed to load feeds file: {}", feedsFile, e);
        }
        
        return new ArrayList<>();
    }
}

