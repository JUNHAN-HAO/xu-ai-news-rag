package com.xu.news.controller;

import com.xu.news.service.KnowledgeBaseService;
import com.xu.news.service.PythonServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class AnalyticsController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    
    @GetMapping("/clusters")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PythonServiceClient.ClusterResponse> getClusters(
            @RequestParam(defaultValue = "10") int nClusters
    ) {
        log.info("Getting cluster analysis with {} clusters", nClusters);
        try {
            PythonServiceClient.ClusterResponse response = 
                knowledgeBaseService.getClusterAnalysis(nClusters);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get cluster analysis", e);
            return ResponseEntity.status(500).build();
        }
    }
}

