# Upload Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)[![Java versions](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)

A simple, modern, and secure file sharing server with a user-friendly web interface and QR code access. Built with Java Spring Boot for enterprise-grade performance and reliability.

This project was born out of a desire for a tool that is as easy to run as Python's `http.server`, but offers modern features like file uploads, password protection, and easy mobile access, all wrapped in a clean, dark-themed UI.

---

## 🚀 Key Features

- **Simple File Sharing**: Serve files and directories instantly from any folder
- **File Upload**: Allow users to upload files directly to server through a web form
- **Password Protection**: Secure your server with a password using Spring Security
- **QR Code Access**: Instantly get a QR code in your terminal to access server from mobile device
- **Customizable Themes**: Change visual theme of web interface using an interactive slider carousel (15+ themes)
- **Modern UI**: A clean, dark-themed, and responsive web interface that looks great on both desktop and mobile
- **Enterprise Grade**: Built with Spring Boot for production-ready performance and security
- **Cross-Platform**: Runs anywhere Java 17+ is installed

---

## 🎬 Demo

When you start the server, you get a clean and informative output right in your terminal:

```sh
$ java -jar uploadserver.jar --password

==================================================
🚀 UploadServer Started Successfully!
📍 Server URL: http://192.168.1.100:8000
📂 Serving Directory: /home/user/documents
🔐 Password Protection: Enabled
==================================================

📱 Scan the QR code to connect:
█████████████████████████████████████
█████████████████████████████████████
█████████████████████████████████████
█████████████████████████████████████
█████████████████████████████████████
```

---

## 📦 Installation

### Prerequisites

- **Java 17 or higher** (OpenJDK, Oracle Java, or compatible)
- **Maven 3.6+** (for building from source)

### Option 1: Download Pre-built JAR

1. Download the latest release from [Releases](https://github.com/MuadzHdz/uploadserver/releases)
2. Run the JAR file:
   ```bash
   java -jar uploadserver-1.2.0.jar
   ```

### Option 2: Build from Source

1. **Clone Repository:** 
    ```bash
    git clone https://github.com/MuadzHdz/uploadserver.git
    ```

    ```bash
    cd uploadserver
    ```
    
2. **Build Project:** 
    ```bash
    mvn clean package -DskipTests
    ```
    
3. **Run the Application:**
    ```bash
    java -jar target/uploadserver-1.2.0.jar
    ```

---

## 🎯 Usage

The most basic way to start the server is to just run the command. It will serve the current directory on port 8000.

```bash
java -jar uploadserver.jar
```

### Command-Line Options

| Argument | Short | Description | Default |
|----------|--------|-------------|----------|
| `--directory <path>` | `-d` | The directory to serve files from and save uploads to | Current Directory |
| `--port <number>` | `-p` | The port to listen on | 8000 |
| `--bind <address>` | `-b` | The network address to bind to. Use `0.0.0.0` for all interfaces | `0.0.0.0` |
| `--password [PASSWORD]` | | Protect the server with a password. If no value is provided, you will be prompted to enter one securely | None |
| `--open` | `-o` | Open the server URL in a web browser automatically | N/A |
| `--version` | | Show version number and exit | N/A |
| `--help` | `-h` | Show this help message and exit | N/A |

### Usage Examples

- **Serve current folder and open it in a browser:**
    ```bash
    java -jar uploadserver.jar -o
    ```
    
- **Serve a specific folder on a different port:**
    ```bash
    java -jar uploadserver.jar -d /home/user/shared -p 8080
    ```
    
- **Protect the server with a password (you will be prompted to enter one):**
    ```bash
    java -jar uploadserver.jar --password
    ```
    
- **Set a password directly from the command line (less secure, avoid in scripts):**
    ```bash
    java -jar uploadserver.jar --password mysecretpassword
    ```

---

## 🎨 Available Themes

The application includes 15+ beautiful themes:

- **Dark Themes**: Tokyo Night, Rose Pine, Catppuccin Mocha/Macchiato/Frappe, Nord, Gruvbox Dark, Dracula, Monokai Pro, Solarized Dark, One Dark Pro, Ayu Dark
- **Light Themes**: Catppuccin Latte, Gruvbox Light, Solarized Light

Switch between themes using the carousel at the bottom of the web interface.

---

## 🔧 Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Security**: Spring Security 6.2.0
- **Template Engine**: Thymeleaf
- **File Processing**: Java NIO
- **QR Code Generation**: ZXing
- **Build Tool**: Maven
- **Frontend**: HTML5, CSS3, JavaScript (ES6+)

---

## 🔒 Security Features

- **Password Protection**: Optional authentication using Spring Security
- **Path Validation**: Prevents directory traversal attacks
- **File Type Validation**: Sanitizes filenames to prevent malicious uploads
- **Session Management**: Secure session handling with automatic cleanup
- **CSRF Protection**: Built-in Cross-Site Request Forgery protection

---

## 📁 Project Structure

```
src/main/java/com/uploadserver/
├── UploadServerApplication.java    # Main application class
├── ServerConfig.java              # Command-line argument handling
├── QRCodeGenerator.java           # QR code generation
├── config/
│   └── SecurityConfig.java       # Spring Security configuration
├── controller/
│   └── FileController.java       # Web controllers
└── service/
    └── FileService.java          # File operations

src/main/resources/
├── templates/                   # Thymeleaf templates
│   ├── index.html              # Main file browser
│   └── login.html             # Login page
├── static/
│   ├── css/
│   │   └── style.css          # Stylesheets with themes
│   └── js/
│       └── script.js          # Frontend interactions
└── application.properties        # Spring Boot configuration
```

---

## 🤝 Contributing

We welcome contributions! If you have ideas for new features, bug reports, or improvements, please feel free to:

1. **Open an Issue**: Describe the bug or suggest a new feature
2. **Fork the Repository**: Make your changes in a feature branch
3. **Create a Pull Request**: Ensure your code adheres to existing style, includes relevant tests, and is well-documented

### Development Guidelines

- Follow Java coding conventions
- Add unit tests for new features
- Update documentation as needed
- Ensure the application builds and runs successfully

Please ensure your changes align with the project's goals of simplicity, security, and user-friendliness.

---

## 📜 Migration from Python

This project was originally written in Python Flask and has been completely rewritten in Java Spring Boot for better performance, security, and maintainability. The Java version maintains full feature parity with the Python version while adding:

- Better performance and memory efficiency
- Enterprise-grade security with Spring Security
- Easier deployment (single JAR file)
- Better scalability and threading
- Type safety and better IDE support

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

- **Mu'adz**
  - GitHub: [@MuadzHdz](https://github.com/MuadzHdz)
  - Email: `adzhdz73@gmail.com`

---

## ⭐ Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) for the amazing framework
- [ZXing](https://github.com/zxing/zxing) for QR code generation
- [Thymeleaf](https://www.thymeleaf.org/) for the template engine
- The Python Flask community for the original inspiration