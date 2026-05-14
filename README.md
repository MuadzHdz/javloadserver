<div align="center">

# fluxload (Python Version)

![Python](https://img.shields.io/badge/Python-3.8+-blue.svg?style=for-the-badge&logo=python)
![Flask](https://img.shields.io/badge/Flask-3.0+-lightgrey.svg?style=for-the-badge&logo=flask)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)

</div>

A modern file sharing server with two modes: a simple single-user server and an advanced multi-user enterprise server.

---

## Quick Start

```bash
git clone https://github.com/MuadzHdz/fluxload.git
cd fluxload
pip install -r requirements.txt
```

### Basic Mode (Simple file sharing)

```bash
python -m fluxload -d /path/to/share
```

### Advanced Mode (Multi-user, admin, search, etc.)

Run directly (no install needed):
```bash
python -m fluxload.advanced_main --dev-mode -d /path/to/share
```

Or install the package to use the `fluxload` command:
```bash
pip install -e .
fluxload --dev-mode -d /path/to/share
```

---

> **Note:** `--redis-url` and `--elasticsearch-url` require optional packages:
> ```bash
> pip install Flask-Session redis elasticsearch
> ```

## CLI Options

| Flag | Description |
|------|-------------|
| `-d` / `--directory` | Directory to serve (default: current dir) |
| `-p` / `--port` | Port to listen on (default: 8000) |
| `-b` / `--bind` | Bind address (default: 0.0.0.0) |
| `--password` | Enable password protection |
| `-o` / `--open` | Open browser automatically |

### Advanced-Only Options

| Flag | Description |
|------|-------------|
| `--dev-mode` | Enable development mode with auto-reload |
| `--workers` | Number of worker processes (default: 1) |
| `--max-upload-size` | Max upload size per file (default: 100MB) |
| `--storage-quota` | Default storage quota per user (default: 5GB) |
| `--disable-registration` | Disable user registration (default: enabled) |
| `--disable-file-sharing` | Disable file sharing (default: enabled) |
| `--database-url` | Database connection URL (default: SQLite) |
| `--redis-url` | Redis session storage (requires `pip install Flask-Session redis`) |
| `--elasticsearch-url` | Elasticsearch for search (requires `pip install elasticsearch`) |
| `--admin-email` | Administrator email displayed in admin dashboard |
| `--site-name` | Site name displayed in UI (default: FluxLoad Pro) |

---

## Access

Open `http://<your-ip>:8000` in any browser on the same network. A QR code is printed in the terminal for easy mobile access.

## License

MIT
