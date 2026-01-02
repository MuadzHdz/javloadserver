package com.javloadserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SystemInfoServiceTest {

    private SystemInfoService systemInfoService;

    @BeforeEach
    void setUp() {
        this.systemInfoService = new SystemInfoService();
    }

    @Test
    void testGetSystemInfo() {
        var info = systemInfoService.getSystemInfo();
        
        assertNotNull(info);
        assertTrue(info.containsKey("javaVersion"));
        assertTrue(info.containsKey("osName"));
        assertTrue(info.containsKey("availableProcessors"));
        assertTrue(info.containsKey("maxMemory"));
        assertTrue(info.containsKey("totalMemory"));
        assertTrue(info.containsKey("usedMemory"));
        assertTrue(info.containsKey("freeMemory"));
        assertTrue(info.containsKey("memoryUsagePercent"));
    }

    @Test
    void testMemoryInfoFormatting() {
        var info = systemInfoService.getSystemInfo();
        
        // Check that memory values are formatted strings
        String maxMemory = (String) info.get("maxMemory");
        String totalMemory = (String) info.get("totalMemory");
        String usedMemory = (String) info.get("usedMemory");
        String freeMemory = (String) info.get("freeMemory");
        
        assertNotNull(maxMemory);
        assertNotNull(totalMemory);
        assertNotNull(usedMemory);
        assertNotNull(freeMemory);
        
        // Should end with B suffix
        assertTrue(maxMemory.endsWith("B"));
        assertTrue(totalMemory.endsWith("B"));
        assertTrue(usedMemory.endsWith("B"));
        assertTrue(freeMemory.endsWith("B"));
    }

    @Test
    void testMemoryUsagePercent() {
        var info = systemInfoService.getSystemInfo();
        
        Double memoryUsagePercent = (Double) info.get("memoryUsagePercent");
        assertNotNull(memoryUsagePercent);
        assertTrue(memoryUsagePercent >= 0.0);
        assertTrue(memoryUsagePercent <= 100.0);
    }
}