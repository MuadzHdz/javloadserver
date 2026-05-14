package com.fluxload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @Test
    void testDefaultConfiguration() {
        String[] args = {};
        ServerConfig config = new ServerConfig(args);

        assertEquals(System.getProperty("user.dir"), config.getDirectory());
        assertEquals(8000, config.getPort());
        assertEquals("0.0.0.0", config.getBind());
        assertFalse(config.hasPassword());
        assertFalse(config.shouldOpenBrowser());
    }

    @Test
    void testPortConfiguration() {
        String[] args = {"-p", "9000"};
        ServerConfig config = new ServerConfig(args);

        assertEquals(9000, config.getPort());
    }

    @Test
    void testDirectoryConfiguration(@TempDir Path tempDir) {
        String[] args = {"-d", tempDir.toString()};
        ServerConfig config = new ServerConfig(args);

        assertEquals(tempDir.toString(), config.getDirectory());
    }

    @Test
    void testBindConfiguration() {
        String[] args = {"-b", "127.0.0.1"};
        ServerConfig config = new ServerConfig(args);

        assertEquals("127.0.0.1", config.getBind());
    }

    @Test
    void testPasswordConfiguration() {
        String[] args = {"--password", "testpass"};
        ServerConfig config = new ServerConfig(args);

        assertTrue(config.hasPassword());
        assertEquals("testpass", config.getPassword());
    }

    @Test
    void testOpenBrowserConfiguration() {
        String[] args = {"-o"};
        ServerConfig config = new ServerConfig(args);

        assertTrue(config.shouldOpenBrowser());
    }

    @Test
    void testLongFormArguments(@TempDir Path tempDir) {
        String[] args = {
            "--port", "8080",
            "--directory", tempDir.toString(),
            "--bind", "localhost",
            "--open"
        };
        ServerConfig config = new ServerConfig(args);

        assertEquals(8080, config.getPort());
        assertEquals(tempDir.toString(), config.getDirectory());
        assertEquals("localhost", config.getBind());
        assertTrue(config.shouldOpenBrowser());
    }

    // Note: --help and --version call System.exit() which cannot be tested
    // without a SecurityManager (deprecated/removed in Java 17+).
}