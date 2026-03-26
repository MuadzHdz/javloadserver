# UploadServer

A professional file sharing server built with Flask. Simple, modern, and efficient.

## Overview

UploadServer is a Flask-based file sharing application that provides a clean web interface for uploading, browsing, and managing files. It offers two operation modes:

- **Basic Mode**: Lightweight file server with password protection
- **Advanced Mode**: Enterprise features including user management, file versioning, search, and WebSocket support

## Features

- File upload and download with drag-and-drop interface
- Directory browsing with breadcrumb navigation
- File preview for images and text files
- Password protection for basic mode
- User authentication and management (advanced mode)
- Full-text search with Whoosh indexing
- Real-time updates via WebSocket
- Theme system with multiple color schemes
- QR code generation for mobile access
- RESTful API for programmatic access
- Docker support for easy deployment

## Requirements

- Python 3.8+
- Flask 2.3+
- See `requirements.txt` for full dependencies

## Installation

```bash
git clone https://github.com/MuadzHdz/uploadserver.git
cd uploadserver
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Quick Start

### Basic Mode (Simple File Server)

```bash
uploadserver -d /path/to/share -p 8000
```

### Advanced Mode (Enterprise Features)

```bash
uploadserver --dev-mode -d /path/to/share
```

## Command Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `-d, --directory` | Directory to serve | Current directory |
| `-p, --port` | Port to listen on | 8000 |
| `-b, --bind` | Bind address | 0.0.0.0 |
| `--password` | Enable password protection | None |
| `-o, --open` | Auto-open browser | False |
| `--dev-mode` | Enable development mode | False |

## Docker Deployment

### Option 1: Using Docker Compose (Recommended)

```bash
# Clone and navigate to project
git clone https://github.com/MuadzHdz/uploadserver.git
cd uploadserver

# Build and run
docker-compose up -d
```

The server will be available at `http://localhost:5000`.

### Option 2: Using Docker Directly

```bash
# Build the image
docker build -t uploadserver .

# Run the container
docker run -d \
  --name uploadserver \
  -p 5000:5000 \
  -v /path/to/your/uploads:/app/uploads \
  -e SECRET_KEY=your-secret-key \
  uploadserver
```

Access the app at `http://localhost:5000`

### Docker Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SECRET_KEY` | Flask secret key | dev-secret-key |
| `DATABASE_URL` | Database connection | sqlite:///data/uploadserver.db |
| `FLASK_ENV` | Environment | production |
| `MAX_FILE_SIZE` | Max upload size (bytes) | 104857600 (100MB) |

### Docker Volumes

- `/app/uploads` - File storage
- `/app/data` - Database location
- `/app/logs` - Application logs

## Configuration

### Environment Variables

```bash
export UPLOADSERVER_PORT=8000
export UPLOADSERVER_BIND=0.0.0.0
export UPLOADSERVER_PASSWORD=your-password
export UPLOADSERVER_DIRECTORY=/path/to/files
```

### Configuration File

Create `~/.uploadserver/config.yaml`:

```yaml
server:
  port: 8080
  bind: "0.0.0.0"
  directory: "/shared/files"
  password: "your-password"
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Redirect to browse |
| GET | `/browse/<path>` | Browse directory |
| GET | `/download/<path>` | Download file |
| POST | `/upload/<path>` | Upload file |
| POST | `/delete/<path>` | Delete file/directory |
| POST | `/rename/<path>` | Rename file/directory |
| POST | `/mkdir/<path>` | Create directory |
| GET | `/preview/<path>` | Preview file |
| GET | `/health` | Health check |
| GET | `/login` | Login page (advanced) |
| POST | `/login` | Authenticate (advanced) |

## Project Structure

```
uploadserver/
├── uploadserver/
│   ├── __init__.py          # Package initialization
│   ├── server.py            # Basic Flask server
│   ├── advanced_server.py   # Enterprise server
│   ├── advanced_main.py     # CLI entry point
│   ├── models.py            # Database models
│   ├── api_routes.py        # REST API
│   ├── search_engine.py    # Whoosh search
│   ├── utils.py             # Utilities
│   ├── templates/           # Jinja2 templates
│   └── static/              # CSS/JS assets
├── tests/                   # Test suite
├── Dockerfile              # Container definition
├── docker-compose.yml      # Docker orchestration
├── requirements.txt       # Python dependencies
└── setup.py                # Package configuration
```

## Testing

```bash
pytest tests/
```

## Security Features

- Path validation to prevent directory traversal
- Secure filename handling with Werkzeug
- Session management with secure cookies
- Password hashing for user authentication
- CORS configuration support

## License

MIT License - See LICENSE file for details

## Author

Mu'adz - adzhdz73@gmail.com
