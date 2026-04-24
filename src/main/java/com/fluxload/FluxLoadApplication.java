package com.fluxload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;

import java.net.URI;

@SpringBootApplication
public class FluxLoadApplication {

    private static ServerConfig serverConfig;

    public static void main(String[] args) {
        serverConfig = new ServerConfig(args);
        
        System.setProperty("server.port", String.valueOf(serverConfig.getPort()));
        System.setProperty("server.address", serverConfig.getBind());
        
        SpringApplication app = new SpringApplication(FluxLoadApplication.class);
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
        System.out.println("FluxLoad Started Successfully!");
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
            boolean opened = false;
            
            // Try Desktop API first
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                opened = true;
                System.out.println("Opened browser at: " + url);
            } else {
                // Fallback to system-specific commands
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                
                if (os.contains("linux")) {
                    // Try common Linux browsers
                    String[] browsers = {"xdg-open", "google-chrome", "firefox", "mozilla", "opera"};
                    for (String browser : browsers) {
                        try {
                            pb = new ProcessBuilder(browser, url);
                            pb.start();
                            opened = true;
                            System.out.println("Opened browser using: " + browser);
                            break;
                        } catch (Exception e) {
                            // Try next browser
                        }
                    }
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", url);
                    pb.start();
                    opened = true;
                    System.out.println("Opened browser using: open");
                } else if (os.contains("windows")) {
                    pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
                    pb.start();
                    opened = true;
                    System.out.println("Opened browser using: rundll32");
                }
                
                if (!opened) {
                    System.out.println("Could not automatically open browser. Please visit: " + url);
                    System.out.println("Tip: Make sure you have a default browser set up.");
                }
            }
        } catch (Exception e) {
            System.out.println("Could not open browser: " + e.getMessage());
            System.out.println("Please manually visit: http://" + serverConfig.getHost() + ":" + serverConfig.getPort());
        }
    }

    public static ServerConfig getServerConfig() {
        return serverConfig;
    }
}