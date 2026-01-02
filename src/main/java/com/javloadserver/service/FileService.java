package com.javloadserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileService {

    private final Path baseDirectory;
    private final long maxFileSize;

    public FileService() {
        this.baseDirectory = Paths.get(System.getProperty("user.dir"));
        this.maxFileSize = 100 * 1024 * 1024; // 100MB default
    }

    public FileService(String directory) {
        this.baseDirectory = Paths.get(directory);
        this.maxFileSize = 100 * 1024 * 1024; // 100MB default
    }

    public List<String> listDirectories(String relativePath) {
        try {
            Path currentDir = baseDirectory.resolve(relativePath).normalize();
            validatePath(currentDir);
            
            if (!Files.exists(currentDir) || !Files.isDirectory(currentDir)) {
                return Collections.emptyList();
            }

            try (Stream<Path> stream = Files.list(currentDir)) {
                return stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .map(path -> path.getFileName().toString())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            }
        } catch (IOException | SecurityException e) {
            return Collections.emptyList();
        }
    }

    public List<String> listFiles(String relativePath) {
        try {
            Path currentDir = baseDirectory.resolve(relativePath).normalize();
            validatePath(currentDir);
            
            if (!Files.exists(currentDir) || !Files.isDirectory(currentDir)) {
                return Collections.emptyList();
            }

            try (Stream<Path> stream = Files.list(currentDir)) {
                return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .filter(this::isFileAccessible)
                    .map(path -> path.getFileName().toString())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            }
        } catch (IOException | SecurityException e) {
            return Collections.emptyList();
        }
    }

    public String uploadFile(MultipartFile file, String relativePath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Validate file name
        String filename = sanitizeFilename(file.getOriginalFilename());
        if (filename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // Check for potentially dangerous files
        if (isPotentiallyDangerousFile(filename)) {
            throw new IllegalArgumentException("File type not allowed for security reasons");
        }

        Path uploadDir = baseDirectory.resolve(relativePath).normalize();
        validatePath(uploadDir);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path targetPath = uploadDir.resolve(filename).normalize();
        validatePath(targetPath);

        // Check if file already exists
        if (Files.exists(targetPath)) {
            String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            String extension = filename.substring(filename.lastIndexOf('.'));
            int counter = 1;
            
            while (Files.exists(uploadDir.resolve(nameWithoutExt + "_" + counter + extension))) {
                counter++;
            }
            filename = nameWithoutExt + "_" + counter + extension;
            targetPath = uploadDir.resolve(filename).normalize();
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    public Path getFilePath(String relativePath) {
        Path filePath = baseDirectory.resolve(relativePath).normalize();
        validatePath(filePath);
        
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("File not found: " + relativePath);
        }
        
        return filePath;
    }

    public boolean directoryExists(String relativePath) {
        try {
            Path dir = baseDirectory.resolve(relativePath).normalize();
            validatePath(dir);
            return Files.exists(dir) && Files.isDirectory(dir) && isFileAccessible(dir);
        } catch (SecurityException e) {
            return false;
        }
    }

    public String getParentPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        
        Path path = Paths.get(relativePath);
        Path parent = path.getParent();
        return parent != null ? parent.toString() : null;
    }

    private void validatePath(Path path) {
        if (!path.startsWith(baseDirectory)) {
            throw new SecurityException("Access denied: Path is outside base directory");
        }
    }

    private boolean isFileAccessible(Path path) {
        try {
            return Files.isReadable(path);
        } catch (SecurityException e) {
            return false;
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        // Remove dangerous characters but allow spaces, international chars, and common file characters
        String sanitized = filename.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        // Remove control characters
        sanitized = sanitized.replaceAll("[\\p{Cntrl}]", "");
        
        // Ensure filename is not too long
        if (sanitized.length() > 255) {
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            String extension = sanitized.substring(sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, 255 - extension.length()) + extension;
        }
        
        return sanitized.trim();
    }

    private boolean isPotentiallyDangerousFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        
        // Block executable files and scripts
        String[] dangerousExtensions = {
            ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar",
            ".app", ".deb", ".pkg", ".dmg", ".rpm", ".sh", ".ps1", ".php", ".asp",
            ".aspx", ".jsp", ".py", ".rb", ".pl", ".cgi"
        };
        
        for (String ext : dangerousExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        
        // Block files with suspicious names
        String[] suspiciousNames = {
            ".htaccess", "web.config", "php.ini", ".bashrc", ".profile", "authorized_keys"
        };
        
        for (String name : suspiciousNames) {
            if (lowerFilename.contains(name)) {
                return true;
            }
        }
        
        return false;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }
}