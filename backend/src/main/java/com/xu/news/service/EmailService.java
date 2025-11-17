package com.xu.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.notification.enabled}")
    private boolean notificationEnabled;
    
    @Async
    public void sendIngestionNotification(String to, int articleCount, String summary) {
        if (!notificationEnabled) {
            log.info("Email notification is disabled");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("新闻入库通知 - " + articleCount + " 条新闻已成功入库");
            message.setText(buildIngestionEmailBody(articleCount, summary));
            
            mailSender.send(message);
            log.info("Ingestion notification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    @Async
    public void sendCustomNotification(String to, String subject, String body) {
        if (!notificationEnabled) {
            log.info("Email notification is disabled");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Custom notification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    private String buildIngestionEmailBody(int articleCount, String summary) {
        return String.format("""
                尊敬的用户，
                
                您的XU-News-AI-RAG系统已成功完成新闻抓取任务。
                
                📊 本次抓取统计：
                - 成功入库新闻数量：%d 条
                - 入库时间：%s
                
                📝 内容摘要：
                %s
                
                您可以登录系统查看详细内容并进行智能查询。
                
                ---
                XU-News-AI-RAG 智能新闻知识库系统
                """,
                articleCount,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                summary != null ? summary : "暂无摘要"
        );
    }
}

