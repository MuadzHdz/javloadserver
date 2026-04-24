package com.fluxload;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ServerConfig {
    private String directory;
    private int port;
    private String bind;
    private String password;
    private boolean openBrowser;
    private String host;

    public ServerConfig(String[] args) {
        parseArguments(args);
        validateConfiguration();
        resolveHost();
    }

    private void parseArguments(String[] args) {
        directory = System.getProperty("user.dir");
        port = 8000;
        bind = "0.0.0.0";
        password = null;
        openBrowser = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-d":
                case "--directory":
                    if (i + 1 < args.length) {
                        directory = args[++i];
                    }
                    break;
                case "-p":
                case "--port":
                    if (i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid port number");
                            System.exit(1);
                        }
                    }
                    break;
                case "-b":
                case "--bind":
                    if (i + 1 < args.length) {
                        bind = args[++i];
                    }
                    break;
                case "--password":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        password = args[++i];
                    } else {
                        password = promptForPassword();
                    }
                    break;
                case "-o":
                case "--open":
                    openBrowser = true;
                    break;
                case "--version":
                    System.out.println("fluxload 1.1.0");
                    System.exit(0);
                    break;
                case "-h":
                case "--help":
                    displayHelp();
                    System.exit(0);
                    break;
            }
        }
    }

    @SuppressWarnings("resource")
    private String promptForPassword() {
        java.io.Console console = System.console();
        if (console != null) {
            return new String(console.readPassword("Enter password: "));
        } else {
            // Fallback to scanner if console is not available (e.g., in some IDEs)
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter password: ");
            try {
                return scanner.nextLine();
            } catch (Exception e) {
                System.err.println("Error reading password");
                System.exit(1);
                return "";
            }
        }
    }

    private void validateConfiguration() {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Error: Directory '" + directory + "' does not exist.");
            System.exit(1);
        }

        if (port < 1 || port > 65535) {
            System.err.println("Error: Port must be between 1 and 65535");
            System.exit(1);
        }
    }

    private void resolveHost() {
        if (bind.equals("0.0.0.0")) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                host = "127.0.0.1";
            }
        } else {
            host = bind;
        }
    }

    private void displayHelp() {
        System.out.println("FluxLoad - A simple, modern file server with upload, password protection, and QR code access");
        System.out.println();
        System.out.println("Usage: java -jar fluxload-1.1.0.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -d, --directory DIR    The directory to serve files from and save uploads to");
        System.out.println("                          [default: current directory]");
        System.out.println("  -p, --port PORT        The port to listen on [default: 8000]");
        System.out.println("  -b, --bind ADDRESS     The address to bind to [default: 0.0.0.0 (all interfaces)]");
        System.out.println("  --password [PASSWORD]  Protect the server with a password");
        System.out.println("                          If no password is provided, you will be prompted to enter one");
        System.out.println("  -o, --open             Open the server URL in a web browser automatically");
        System.out.println("  --version              Show the version number and exit");
        System.out.println("  -h, --help             Show this help message and exit");
    }

    public String getDirectory() {
        return directory;
    }

    public int getPort() {
        return port;
    }

    public String getBind() {
        return bind;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }

    public boolean shouldOpenBrowser() {
        return openBrowser;
    }

    public String getHost() {
        return host;
    }
}