package com.xu.news.service;

import com.xu.news.entity.NewsArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssIngestionService {
    
    private final NewsService newsService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final EmailService emailService;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.rss.feeds-file:../feeds.yaml}")
    private String feedsFile;
    
    /**
     * Fetch and ingest RSS feeds
     */
    public int ingestFromRssFeeds(List<String> feedUrls) {
        int totalIngested = 0;
        List<NewsArticle> newArticles = new ArrayList<>();
        
        for (String feedUrl : feedUrls) {
            try {
                log.info("Fetching RSS feed: {}", feedUrl);
                List<NewsArticle> articles = fetchRssFeed(feedUrl);
                
                // Save to database
                List<NewsArticle> saved = newsService.saveArticles(articles);
                newArticles.addAll(saved);
                totalIngested += saved.size();
                
                log.info("Ingested {} articles from {}", saved.size(), feedUrl);
                
                // Rate limiting
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Failed to fetch RSS feed: {}", feedUrl, e);
            }
        }
        
        // Add to knowledge base
        if (!newArticles.isEmpty()) {
            try {
                knowledgeBaseService.addArticlesToKnowledgeBase(newArticles);
                log.info("Added {} articles to knowledge base", newArticles.size());
            } catch (Exception e) {
                log.error("Failed to add articles to knowledge base", e);
            }
        }
        
        return totalIngested;
    }
    
    /**
     * Fetch articles from a single RSS feed
     */
    private List<NewsArticle> fetchRssFeed(String feedUrl) {
        try {
            WebClient webClient = webClientBuilder.build();
            String xml = webClient.get()
                    .uri(feedUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (xml == null) {
                log.warn("Empty response from RSS feed: {}", feedUrl);
                return Collections.emptyList();
            }
            
            return parseRssFeed(xml, feedUrl);
            
        } catch (Exception e) {
            log.error("Failed to fetch RSS feed: {}", feedUrl, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Parse RSS XML
     */
    private List<NewsArticle> parseRssFeed(String xml, String feedUrl) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            
            // Get feed source name
            String sourceName = extractSourceName(doc);
            
            // Parse items
            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() == 0) {
                items = doc.getElementsByTagName("entry");  // Atom format
            }
            
            for (int i = 0; i < items.getLength(); i++) {
                try {
                    Element item = (Element) items.item(i);
                    NewsArticle article = parseRssItem(item, sourceName);
                    if (article != null) {
                        articles.add(article);
                    }
                } catch (Exception e) {
                    log.error("Failed to parse RSS item", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse RSS XML", e);
        }
        
        return articles;
    }
    
    /**
     * Parse single RSS item
     */
    private NewsArticle parseRssItem(Element item, String source) {
        try {
            String title = getElementText(item, "title");
            String link = getElementText(item, "link");
            String description = getElementText(item, "description");
            String content = getElementText(item, "content:encoded");
            String author = getElementText(item, "author");
            String pubDateStr = getElementText(item, "pubDate");
            
            // Atom format fallbacks
            if (title == null) title = getElementText(item, "title");
            if (link == null) {
                Element linkElement = (Element) item.getElementsByTagName("link").item(0);
                if (linkElement != null) {
                    link = linkElement.getAttribute("href");
                }
            }
            if (description == null) description = getElementText(item, "summary");
            if (content == null) content = getElementText(item, "content");
            if (pubDateStr == null) pubDateStr = getElementText(item, "published");
            
            // Validation
            if (title == null || title.isEmpty() || link == null || link.isEmpty()) {
                return null;
            }
            
            // Parse date
            LocalDateTime publishedAt = parseDate(pubDateStr);
            
            // Extract tags/categories
            Set<String> tags = new HashSet<>();
            NodeList categories = item.getElementsByTagName("category");
            for (int i = 0; i < categories.getLength(); i++) {
                String category = categories.item(i).getTextContent();
                if (category != null && !category.isEmpty()) {
                    tags.add(category.trim());
                }
            }
            
            return NewsArticle.builder()
                    .title(title)
                    .content(content != null ? content : description)
                    .summary(description)
                    .url(link)
                    .source(source)
                    .author(author)
                    .publishedAt(publishedAt)
                    .tags(tags)
                    .contentType(NewsArticle.ContentType.RSS)
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to parse RSS item", e);
            return null;
        }
    }
    
    private String extractSourceName(Document doc) {
        try {
            Element channel = (Element) doc.getElementsByTagName("channel").item(0);
            if (channel != null) {
                return getElementText(channel, "title");
            }
            // Atom format
            return getElementText((Element) doc.getDocumentElement(), "title");
        } catch (Exception e) {
            return "Unknown Source";
        }
    }
    
    private String getElementText(Element parent, String tagName) {
        try {
            NodeList nodes = parent.getElementsByTagName(tagName);
            if (nodes.getLength() > 0) {
                String text = nodes.item(0).getTextContent();
                return text != null ? text.trim() : null;
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Try RFC 822 format (RSS 2.0)
            ZonedDateTime zdt = ZonedDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME);
            return zdt.toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                // Try ISO 8601 format (Atom)
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e2) {
                log.warn("Failed to parse date: {}", dateStr);
                return LocalDateTime.now();
            }
        }
    }
}

