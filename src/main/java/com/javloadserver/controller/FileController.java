package com.javloadserver.controller;

import com.javloadserver.service.FileService;
import com.javloadserver.UploadServerApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
public class FileController {

    protected final FileService fileService;

    public FileController() {
        this.fileService = new FileService(UploadServerApplication.getServerConfig().getDirectory());
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/browse";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String path, Model model, HttpServletRequest request) {
        if (path == null) path = "";

        if (!fileService.directoryExists(path)) {
            model.addAttribute("error", "Error: Invalid or inaccessible directory.");
            return "redirect:/browse";
        }

        List<String> dirs = fileService.listDirectories(path);
        List<String> files = fileService.listFiles(path);
        String parentDir = fileService.getParentPath(path);
        String theme = getThemeFromCookie(request);

        model.addAttribute("files", files);
        model.addAttribute("dirs", dirs);
        model.addAttribute("current_path", path);
        model.addAttribute("parent_dir", parentDir);
        model.addAttribute("theme", theme);

        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, 
                         @RequestParam(required = false) String path, 
                         RedirectAttributes redirectAttributes) {
        if (path == null) path = "";

        try {
            String filename = fileService.uploadFile(file, path);
            redirectAttributes.addFlashAttribute("success", 
                "File \"" + filename + "\" uploaded successfully to " + (path.isEmpty() ? "/" : "/" + path) + "!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error saving file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/browse?path=" + path;
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Object> serveFile(@RequestParam String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filename not provided");
        }
        try {
            Path filePath = fileService.getFilePath(filename);
            String originalFilename = filePath.getFileName().toString();
            
            // Get specific content type based on file extension
            String contentType = getSpecificContentType(originalFilename);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + originalFilename + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Transfer-Encoding", "binary")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .body(new org.springframework.core.io.FileSystemResource(filePath.toFile()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/preview")
    @ResponseBody
    public ResponseEntity<Object> previewFile(@RequestParam String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filename not provided");
        }
        try {
            Path filePath = fileService.getFilePath(filename);
            String originalFilename = filePath.getFileName().toString();
            
            // Get specific content type based on file extension
            String contentType = getSpecificContentType(originalFilename);
            
            // Use inline disposition for preview
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + originalFilename + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Transfer-Encoding", "binary")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .body(new org.springframework.core.io.FileSystemResource(filePath.toFile()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        if (!UploadServerApplication.getServerConfig().hasPassword()) {
            return "redirect:/";
        }

        String theme = getThemeFromCookie(request);
        model.addAttribute("theme", theme);
        
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }

    private String getThemeFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("theme".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "tokyo-night";
    }
    
    private String getSpecificContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        
        // Images
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        }
        
        // Videos
        else if (lowerFilename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFilename.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lowerFilename.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFilename.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (lowerFilename.endsWith(".flv")) {
            return "video/x-flv";
        } else if (lowerFilename.endsWith(".webm")) {
            return "video/webm";
        }
        
        // Audio
        else if (lowerFilename.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerFilename.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerFilename.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (lowerFilename.endsWith(".m4a")) {
            return "audio/mp4";
        } else if (lowerFilename.endsWith(".flac")) {
            return "audio/flac";
        }
        
        // Documents
        else if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerFilename.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerFilename.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerFilename.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (lowerFilename.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerFilename.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        
        // Archives
        else if (lowerFilename.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerFilename.endsWith(".rar")) {
            return "application/x-rar-compressed";
        } else if (lowerFilename.endsWith(".7z")) {
            return "application/x-7z-compressed";
        } else if (lowerFilename.endsWith(".tar")) {
            return "application/x-tar";
        } else if (lowerFilename.endsWith(".gz")) {
            return "application/gzip";
        }
        
        // Text files
        else if (lowerFilename.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerFilename.endsWith(".json")) {
            return "application/json";
        } else if (lowerFilename.endsWith(".xml")) {
            return "application/xml";
        } else if (lowerFilename.endsWith(".csv")) {
            return "text/csv";
        }
        
        // Default to safe binary type
        else {
            return "application/download";
        }
    }
}