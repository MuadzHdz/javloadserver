package com.javloadserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "JavloadServer");
        response.put("version", "1.1.0");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/")
    public Map<String, Object> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "JavloadServer");
        response.put("version", "1.1.0");
        response.put("description", "A modern file sharing server with upload capabilities");
        response.put("endpoints", Map.of(
            "browse", "/browse",
            "download", "/download",
            "upload", "/upload",
            "preview", "/preview",
            "health", "/health"
        ));
        return response;
    }
}