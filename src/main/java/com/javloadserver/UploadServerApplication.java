package com.javloadserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@SpringBootApplication
public class UploadServerApplication {

    private static ServerConfig serverConfig;

    public static void main(String[] args) {
        serverConfig = new ServerConfig(args);
        
        System.setProperty("server.port", String.valueOf(serverConfig.getPort()));
        System.setProperty("server.address", serverConfig.getBind());
        
        SpringApplication app = new SpringApplication(UploadServerApplication.class);
        app.setAdditionalProfiles("default");
        app.run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        displayServerInfo();
        
        if (serverConfig.shouldOpenBrowser()) {
            openBrowser();
        }
    }

    private void displayServerInfo() {
        String url = "http://" + serverConfig.getHost() + ":" + serverConfig.getPort();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("JavloadServer Started Successfully!");
        System.out.println("Server URL: " + url);
        System.out.println("Serving Directory: " + serverConfig.getDirectory());
        System.out.println("Password Protection: " + (serverConfig.hasPassword() ? "Enabled" : "Disabled"));
        System.out.println("=".repeat(50));
        
        if (serverConfig.hasPassword()) {
        System.out.println("\nAccess the server at: " + url);
        System.out.println("Authentication: Password is required");
        }
        
        displayQRCode(url);
    }

    private void displayQRCode(String url) {
        try {
            QRCodeGenerator.displayQRCode(url);
        } catch (Exception e) {
            System.out.println("\nCould not generate QR code: " + e.getMessage());
            System.out.println("To display QR codes, ensure the required libraries are available.");
        }
    }

    private void openBrowser() {
        try {
            String url = "http://" + serverConfig.getHost() + ":" + serverConfig.getPort();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (IOException e) {
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }

    public static ServerConfig getServerConfig() {
        return serverConfig;
    }
}