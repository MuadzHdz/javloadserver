package com.fluxload.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileService fileService;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        this.fileService = new FileService(tempDir.toString());
    }

    @Test
    void testListFilesInEmptyDirectory() {
        List<String> files = fileService.listFiles("");
        assertTrue(files.isEmpty());
    }

    @Test
    void testListDirectoriesInEmptyDirectory() {
        List<String> dirs = fileService.listDirectories("");
        assertTrue(dirs.isEmpty());
    }

    @Test
    void testUploadValidFile() throws IOException {
        // Create a test file
        MultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello, World!".getBytes()
        );

        String filename = fileService.uploadFile(file, "");
        
        assertEquals("test.txt", filename);
        assertTrue(Files.exists(tempDir.resolve("test.txt")));
    }

    @Test
    void testUploadEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.txt", 
            "text/plain", 
            new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(emptyFile, "");
        });
    }

    @Test
    void testUploadFileWithInvalidName() {
        MultipartFile file = new MockMultipartFile(
            "file", 
            "", 
            "text/plain", 
            "test content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(file, "");
        });
    }

    @Test
    void testUploadFileWithDangerousExtension() {
        MultipartFile file = new MockMultipartFile(
            "file", 
            "malicious.exe", 
            "application/octet-stream", 
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(file, "");
        });
    }

    @Test
    void testSanitizeFilename() throws IOException {
        // Create files with various special characters
        MultipartFile file1 = new MockMultipartFile(
            "file", 
            "test<>file.txt", 
            "text/plain", 
            "content".getBytes()
        );

        MultipartFile file2 = new MockMultipartFile(
            "file", 
            "normal file with spaces.pdf", 
            "application/pdf", 
            "pdf content".getBytes()
        );

        String filename1 = fileService.uploadFile(file1, "");
        String filename2 = fileService.uploadFile(file2, "");

        assertEquals("test__file.txt", filename1);
        assertEquals("normal file with spaces.pdf", filename2);
    }

    @Test
    void testDirectoryExists() throws IOException {
        Path newDir = tempDir.resolve("testdir");
        Files.createDirectory(newDir);

        assertTrue(fileService.directoryExists("testdir"));
        assertFalse(fileService.directoryExists("nonexistent"));
    }

    @Test
    void testGetParentPath() {
        assertEquals(null, fileService.getParentPath(""));
        assertEquals(null, fileService.getParentPath(null));
        assertEquals("parent", fileService.getParentPath("parent/child"));
        assertEquals("parent", fileService.getParentPath("parent/child/"));
    }

    @Test
    void testGetFilePath() throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());

        Path result = fileService.getFilePath("test.txt");
        assertEquals(testFile, result);
    }

    @Test
    void testGetNonExistentFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.getFilePath("nonexistent.txt");
        });
    }

    @Test
    void testUploadDuplicateFile() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
            "file", 
            "duplicate.txt", 
            "text/plain", 
            "content 1".getBytes()
        );

        MultipartFile file2 = new MockMultipartFile(
            "file", 
            "duplicate.txt", 
            "text/plain", 
            "content 2".getBytes()
        );

        String filename1 = fileService.uploadFile(file1, "");
        String filename2 = fileService.uploadFile(file2, "");

        assertEquals("duplicate.txt", filename1);
        assertEquals("duplicate_1.txt", filename2);
        
        assertTrue(Files.exists(tempDir.resolve("duplicate.txt")));
        assertTrue(Files.exists(tempDir.resolve("duplicate_1.txt")));
    }

    @Test
    void testDeleteFile() throws IOException {
        Path testFile = tempDir.resolve("delete_me.txt");
        Files.write(testFile, "to be deleted".getBytes());
        assertTrue(Files.exists(testFile));

        boolean result = fileService.deleteFile("delete_me.txt");
        assertTrue(result);
        assertFalse(Files.exists(testFile));
    }

    @Test
    void testRenameFile() throws IOException {
        Path testFile = tempDir.resolve("old_name.txt");
        Files.write(testFile, "rename test".getBytes());

        String newName = fileService.renameFile("old_name.txt", "new_name.txt");
        assertEquals("new_name.txt", newName);
        assertFalse(Files.exists(tempDir.resolve("old_name.txt")));
        assertTrue(Files.exists(tempDir.resolve("new_name.txt")));
    }

    @Test
    void testCreateDirectory() throws IOException {
        String dirName = fileService.createDirectory("", "new_directory");
        assertEquals("new_directory", dirName);
        assertTrue(Files.exists(tempDir.resolve("new_directory")));
        assertTrue(Files.isDirectory(tempDir.resolve("new_directory")));
    }

    @Test
    void testCreateDirectoryAlreadyExists() throws IOException {
        Files.createDirectory(tempDir.resolve("exists"));
        assertThrows(IOException.class, () -> {
            fileService.createDirectory("", "exists");
        });
    }

    @Test
    void testRenameNonExistentFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.renameFile("nonexistent.txt", "new.txt");
        });
    }

    @Test
    void testDeleteNonExistentFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.deleteFile("nonexistent.txt");
        });
    }
}