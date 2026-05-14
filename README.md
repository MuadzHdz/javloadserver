<div align="center">

# FluxLoad (Java Version)

![Java](https://img.shields.io/badge/Java-17+-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg?style=for-the-badge&logo=spring-boot)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

</div>

A modern file sharing server built with Spring Boot 3.2.0.

---

## Quick Start

```bash
git clone https://github.com/muadzhdz/fluxload.git
cd fluxload
git checkout java
mvn clean package -DskipTests
```

### Run

Download the latest JAR from the [Releases page](https://github.com/muadzhdz/fluxload/releases).

```bash
java -jar fluxload-1.1.0.jar -d /path/to/share
```

---

## CLI Options

| Flag | Description |
|------|-------------|
| `-d` / `--directory` | Directory to serve (default: current dir) |
| `-p` / `--port` | Port to listen on (default: 8000) |
| `-b` / `--bind` | Bind address (default: 0.0.0.0) |
| `--password` | Enable password protection |
| `-o` / `--open` | Open browser automatically |
| `--version` | Show version |
| `-h` / `--help` | Show help |

---

## Build

```bash
mvn clean package           # Build JAR (with tests)
mvn clean package -DskipTests  # Build JAR (skip tests)
```

## Access

Open `http://<your-ip>:8000` in any browser on the same network. A QR code is printed in the terminal for easy mobile access.

## License

MIT
