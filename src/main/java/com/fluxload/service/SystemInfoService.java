package com.fluxload.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class SystemInfoService {

    private final Path baseDirectory;

    public SystemInfoService() {
        this.baseDirectory = Paths.get(System.getProperty("user.dir"));
    }

    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Basic system info
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        // Memory info
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        info.put("maxMemory", formatBytes(maxMemory));
        info.put("totalMemory", formatBytes(totalMemory));
        info.put("usedMemory", formatBytes(usedMemory));
        info.put("freeMemory", formatBytes(freeMemory));
        info.put("memoryUsagePercent", (double) usedMemory / maxMemory * 100);
        
        // Disk info
        try {
            File diskPartition = new File(baseDirectory.toString());
            long totalSpace = diskPartition.getTotalSpace();
            long freeSpace = diskPartition.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            info.put("totalDiskSpace", formatBytes(totalSpace));
            info.put("usedDiskSpace", formatBytes(usedSpace));
            info.put("freeDiskSpace", formatBytes(freeSpace));
            info.put("diskUsagePercent", (double) usedSpace / totalSpace * 100);
        } catch (Exception e) {
            info.put("diskInfo", "Unavailable");
        }
        
        // File count
        try {
            long fileCount = Files.walk(baseDirectory)
                .filter(Files::isRegularFile)
                .count();
            info.put("totalFiles", fileCount);
        } catch (IOException e) {
            info.put("totalFiles", "Unavailable");
        }
        
        return info;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}