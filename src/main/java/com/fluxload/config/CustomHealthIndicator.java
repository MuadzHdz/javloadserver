package com.fluxload.config;

import com.fluxload.FluxLoadApplication;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            var config = FluxLoadApplication.getServerConfig();
            String dir = config != null ? config.getDirectory() : System.getProperty("user.dir");
            File directory = new File(dir);
            
            // Check if directory exists and is writable
            boolean exists = directory.exists() && directory.isDirectory();
            boolean writable = exists && directory.canWrite();
            
            if (exists && writable) {
                long freeSpace = directory.getFreeSpace();
                long totalSpace = directory.getTotalSpace();
                
                return Health.up()
                    .withDetail("directory", directory.getAbsolutePath())
                    .withDetail("freeSpace", freeSpace)
                    .withDetail("totalSpace", totalSpace)
                    .withDetail("usable", true)
                    .build();
            } else {
                return Health.down()
                    .withDetail("directory", directory.getAbsolutePath())
                    .withDetail("exists", exists)
                    .withDetail("writable", writable)
                    .withDetail("error", exists ? "Directory is not writable" : "Directory does not exist")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}