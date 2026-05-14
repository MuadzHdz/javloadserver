package com.fluxload.controller;

import com.fluxload.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileControllerTest {

    private FileController fileController;
    private FileService fileService;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        this.fileService = new FileService(tempDir.toString());
        this.fileController = new FileController(fileService);
    }

    @Test
    @SuppressWarnings("null")
    void testBrowseEmptyDirectory() {
        org.springframework.ui.Model model = mock(org.springframework.ui.Model.class);
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = fileController.browse("", model, request);

        assertEquals("index", result);
        verify(model).addAttribute(eq("files"), any());
        verify(model).addAttribute(eq("dirs"), any());
        verify(model).addAttribute("current_path", "");
    }

    @Test
    void testBrowseNonExistentDirectory() {
        org.springframework.ui.Model model = mock(org.springframework.ui.Model.class);
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = fileController.browse("nonexistent", model, request);

        assertEquals("redirect:/browse", result);
        verify(model).addAttribute("error", "Error: Invalid or inaccessible directory.");
    }

    @Test
    void testUploadValidFile() throws IOException {
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        MultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );

        String result = fileController.uploadFile(file, "", redirectAttributes);

        assertEquals("redirect:/browse?path=", result);
        assertTrue(Files.exists(tempDir.resolve("test.txt")));
    }

    @Test
    void testUploadEmptyFile() {
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        MultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.txt", 
            "text/plain", 
            new byte[0]
        );

        String result = fileController.uploadFile(emptyFile, "", redirectAttributes);

        assertEquals("redirect:/browse?path=", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("File is empty"));
    }

    @Test
    void testServeFile() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());

        var response = fileController.serveFile("test.txt");

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testServeNonExistentFile() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            fileController.serveFile("nonexistent.txt");
        });
    }

    @Test
    void testPreviewFile() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());

        var response = fileController.previewFile("test.txt");

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
