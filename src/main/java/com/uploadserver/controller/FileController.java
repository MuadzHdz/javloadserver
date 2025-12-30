package com.uploadserver.controller;

import com.uploadserver.service.FileService;
import com.uploadserver.UploadServerApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    private final FileService fileService;

    public FileController() {
        this.fileService = new FileService(UploadServerApplication.getServerConfig().getDirectory());
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/browse/";
    }

    @GetMapping("/browse/")
    public String browseEmpty(Model model, HttpServletRequest request) {
        return browse("", model, request);
    }

    @GetMapping("/browse/{path:**}")
    public String browse(@PathVariable String path, Model model, HttpServletRequest request) {
        if (path == null) path = "";

        if (!fileService.directoryExists(path)) {
            model.addAttribute("error", "Error: Invalid or inaccessible directory.");
            return "redirect:/browse/";
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

    @PostMapping("/upload/")
    public String uploadToRoot(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        return uploadFile(file, "", redirectAttributes);
    }

    @PostMapping("/upload/{path:**}")
    public String uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String path, RedirectAttributes redirectAttributes) {
        if (path == null) path = "";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No file selected.");
            return "redirect:/browse/" + (path.isEmpty() ? "" : path);
        }

        try {
            String filename = fileService.uploadFile(file, path);
            redirectAttributes.addFlashAttribute("success", 
                "File \"" + filename + "\" uploaded successfully to /" + path + "!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error saving file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/browse/" + (path.isEmpty() ? "" : path);
    }

    @GetMapping("/download/{filename:**}")
    @ResponseBody
    public ResponseEntity<Object> serveFile(@PathVariable String filename) {
        try {
            Path filePath = fileService.getFilePath(filename);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
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
}