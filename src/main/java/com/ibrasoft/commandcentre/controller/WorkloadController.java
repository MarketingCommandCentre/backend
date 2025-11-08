package com.ibrasoft.commandcentre.controller;

import com.ibrasoft.commandcentre.model.Request;
import com.ibrasoft.commandcentre.service.CycleService;
import com.ibrasoft.commandcentre.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {
    
    private final RequestService requestService;
    private final CycleService cycleService;
    
    /**
     * Get workload for content creators (REEL requests in current development cycle)
     * These are requests that need content/captions created during this 2-week cycle
     */
    @GetMapping("/content-creators")
    public ResponseEntity<Map<String, Object>> getContentCreatorWorkload() {
        CycleService.CycleInfo currentCycle = cycleService.getCurrentDevelopmentCycle();
        List<Request> workload = requestService.getContentCreatorWorkload();
        
        Map<String, Object> response = new HashMap<>();
        response.put("cycleInfo", Map.of(
            "cycleNumber", currentCycle.getCycleNumber(),
            "developmentStart", currentCycle.getDevelopmentStart().toString(),
            "developmentEnd", currentCycle.getDevelopmentEnd().toString(),
            "postingStart", currentCycle.getPostingStart().toString(),
            "postingEnd", currentCycle.getPostingEnd().toString()
        ));
        response.put("requestType", "REEL");
        response.put("role", "Content Creator");
        response.put("totalRequests", workload.size());
        response.put("requests", workload);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get workload for graphic designers (POST requests in current development cycle)
     * These are requests that need graphics/visuals created during this 2-week cycle
     */
    @GetMapping("/graphic-designers")
    public ResponseEntity<Map<String, Object>> getGraphicDesignerWorkload() {
        CycleService.CycleInfo currentCycle = cycleService.getCurrentDevelopmentCycle();
        List<Request> workload = requestService.getGraphicDesignerWorkload();
        
        Map<String, Object> response = new HashMap<>();
        response.put("cycleInfo", Map.of(
            "cycleNumber", currentCycle.getCycleNumber(),
            "developmentStart", currentCycle.getDevelopmentStart().toString(),
            "developmentEnd", currentCycle.getDevelopmentEnd().toString(),
            "postingStart", currentCycle.getPostingStart().toString(),
            "postingEnd", currentCycle.getPostingEnd().toString()
        ));
        response.put("requestType", "POST");
        response.put("role", "Graphic Designer");
        response.put("totalRequests", workload.size());
        response.put("requests", workload);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get workload for social media managers (all requests in current posting cycle)
     * These are requests that need to be posted during this 2-week cycle
     */
    @GetMapping("/social-media-managers")
    public ResponseEntity<Map<String, Object>> getSocialMediaManagerWorkload() {
        CycleService.CycleInfo currentPostingCycle = cycleService.getCurrentPostingCycle();
        List<Request> workload = requestService.getSocialMediaManagerWorkload();
        
        Map<String, Object> response = new HashMap<>();
        response.put("cycleInfo", Map.of(
            "cycleNumber", currentPostingCycle.getCycleNumber(),
            "developmentStart", currentPostingCycle.getDevelopmentStart().toString(),
            "developmentEnd", currentPostingCycle.getDevelopmentEnd().toString(),
            "postingStart", currentPostingCycle.getPostingStart().toString(),
            "postingEnd", currentPostingCycle.getPostingEnd().toString()
        ));
        response.put("requestType", "ALL");
        response.put("role", "Social Media Manager");
        response.put("totalRequests", workload.size());
        response.put("requests", workload);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get cycle information
     */
    @GetMapping("/cycle-info")
    public ResponseEntity<Map<String, Object>> getCycleInfo() {
        CycleService.CycleInfo developmentCycle = cycleService.getCurrentDevelopmentCycle();
        CycleService.CycleInfo postingCycle = cycleService.getCurrentPostingCycle();
        
        Map<String, Object> response = new HashMap<>();
        response.put("currentDevelopmentCycle", Map.of(
            "cycleNumber", developmentCycle.getCycleNumber(),
            "developmentStart", developmentCycle.getDevelopmentStart().toString(),
            "developmentEnd", developmentCycle.getDevelopmentEnd().toString(),
            "postingStart", developmentCycle.getPostingStart().toString(),
            "postingEnd", developmentCycle.getPostingEnd().toString()
        ));
        response.put("currentPostingCycle", Map.of(
            "cycleNumber", postingCycle.getCycleNumber(),
            "developmentStart", postingCycle.getDevelopmentStart().toString(),
            "developmentEnd", postingCycle.getDevelopmentEnd().toString(),
            "postingStart", postingCycle.getPostingStart().toString(),
            "postingEnd", postingCycle.getPostingEnd().toString()
        ));
        
        return ResponseEntity.ok(response);
    }
}
