package com.uploadserver.service;

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

    public FileService() {
        this.baseDirectory = Paths.get(System.getProperty("user.dir"));
    }

    public FileService(String directory) {
        this.baseDirectory = Paths.get(directory);
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

        String filename = sanitizeFilename(file.getOriginalFilename());
        if (filename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Path uploadDir = baseDirectory.resolve(relativePath).normalize();
        validatePath(uploadDir);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path targetPath = uploadDir.resolve(filename).normalize();
        validatePath(targetPath);

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
            return Files.exists(dir) && Files.isDirectory(dir);
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

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        return filename.replaceAll("[^a-zA-Z0-9.-_]", "_");
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }
}