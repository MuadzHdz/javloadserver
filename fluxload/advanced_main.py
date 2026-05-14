"""
Main entry point for FluxLoad Pro Enterprise
"""

import sys
import os
import argparse
import signal
import threading
import time
from pathlib import Path
from datetime import datetime, timezone
from dataclasses import dataclass, field

from fluxload.advanced_server import create_app
from fluxload.models import db, SystemSettings
from fluxload.search_engine import SEARCH_ENGINE
from fluxload import __version__


@dataclass
class BackgroundTaskManager:
    """Manages background tasks with proper cleanup support"""

    app: any = field(default=None)
    observer: any = field(default=None)
    cleanup_event: threading.Event = field(default_factory=threading.Event)
    threads: list = field(default_factory=list)

    def start(self):
        if self.app is None:
            return

        self.cleanup_event.clear()

        cleanup_thread = threading.Thread(target=self._cleanup_task, daemon=True)
        cleanup_thread.start()
        self.threads.append(cleanup_thread)

        backup_thread = threading.Thread(target=self._backup_task, daemon=True)
        backup_thread.start()
        self.threads.append(backup_thread)

        try:
            from watchdog.observers import Observer
            from watchdog.events import FileSystemEventHandler

            class FileChangeHandler(FileSystemEventHandler):
                def on_modified(self, event):
                    if not event.is_directory:
                        print(f"File modified: {event.src_path}")

                def on_created(self, event):
                    if not event.is_directory:
                        print(f"File created: {event.src_path}")

                def on_deleted(self, event):
                    if not event.is_directory:
                        print(f"File deleted: {event.src_path}")

            self.observer = Observer()
            self.observer.schedule(
                FileChangeHandler(), self.app.config["UPLOAD_FOLDER"], recursive=True
            )
            self.observer.start()
        except ImportError:
            print("Watchdog not installed - file monitoring disabled")
        except Exception as e:
            print(f"Failed to start file monitor: {e}")

    def _cleanup_task(self):
        """Clean up expired user sessions"""
        from fluxload.models import UserSession

        while not self.cleanup_event.is_set():
            try:
                with self.app.app_context():
                    expired_sessions = UserSession.query.filter(
                        UserSession.expires_at < datetime.now(timezone.utc)
                    ).all()

                    for session in expired_sessions:
                        db.session.delete(session)

                    if expired_sessions:
                        db.session.commit()
                        print(f"Cleaned up {len(expired_sessions)} expired sessions")
            except Exception as e:
                print(f"Error in session cleanup: {e}")

            self.cleanup_event.wait(3600)

    def _backup_task(self):
        """Periodic database backup"""
        import shutil

        while not self.cleanup_event.is_set():
            try:
                if (
                    hasattr(self.app, "config")
                    and "SQLALCHEMY_DATABASE_URI" in self.app.config
                ):
                    db_path = self.app.config["SQLALCHEMY_DATABASE_URI"].replace(
                        "sqlite:///", ""
                    )
                    if os.path.exists(db_path):
                        backup_path = f"{db_path}.backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
                        shutil.copy2(db_path, backup_path)
                        print(f"Database backed up to {backup_path}")
            except Exception as e:
                print(f"Error in database backup: {e}")

            self.cleanup_event.wait(86400)

    def stop(self):
        """Stop all background tasks gracefully"""
        self.cleanup_event.set()

        if self.observer:
            self.observer.stop()
            self.observer.join(timeout=5)
            self.observer = None

        print("Background tasks stopped")


def setup_background_tasks(app):
    """Setup background tasks for file monitoring and maintenance"""
    manager = BackgroundTaskManager(app=app)
    manager.start()
    return manager


def main():
    """Main function for FluxLoad Pro"""
    parser = argparse.ArgumentParser(
        description="FluxLoad Pro - Enterprise-grade collaborative file sharing platform",
        formatter_class=argparse.RawTextHelpFormatter,
    )

    # Basic options
    parser.add_argument(
        "-d",
        "--directory",
        default=os.getcwd(),
        help="The directory to serve files from and save uploads to.\n[default: current directory]",
    )

    parser.add_argument(
        "-p",
        "--port",
        default=8000,
        type=int,
        help="The port to listen on.\n[default: 8000]",
    )

    parser.add_argument(
        "-b",
        "--bind",
        default="0.0.0.0",
        help="The address to bind to.\n[default: 0.0.0.0 (all interfaces)]",
    )

    parser.add_argument(
        "--password",
        nargs="?",
        const="prompt",
        default=None,
        help="Enable admin password protection.\nIf no password is provided, you will be prompted to enter one.",
    )

    parser.add_argument(
        "-o",
        "--open",
        action="store_true",
        help="Open the server URL in a web browser automatically.",
    )

    parser.add_argument(
        "--debug", action="store_true", help="Enable debug mode with detailed logging."
    )

    # Advanced options
    parser.add_argument(
        "--dev-mode",
        action="store_true",
        help="Enable development mode with auto-reload.",
    )

    parser.add_argument(
        "--workers",
        type=int,
        default=1,
        help="Number of worker processes to use.\n[default: 1]",
    )

    parser.add_argument(
        "--max-upload-size",
        type=str,
        default="100MB",
        help="Maximum upload size per file.\n[default: 100MB]",
    )

    parser.add_argument(
        "--storage-quota",
        type=str,
        default="5GB",
        help="Default user storage quota.\n[default: 5GB]",
    )

    parser.add_argument(
        "--disable-registration",
        action="store_true",
        help="Disable user registration.\n[default: enabled]",
    )

    parser.add_argument(
        "--disable-file-sharing",
        action="store_true",
        help="Disable file sharing features.\n[default: enabled]",
    )

    parser.add_argument(
        "--database-url",
        help="Database connection URL.\n[default: sqlite:///<directory>/fluxload.db]",
    )

    parser.add_argument(
        "--redis-url",
        help="Redis connection URL for session storage.\nIf not provided, uses Flask's default cookie sessions.",
    )

    parser.add_argument(
        "--elasticsearch-url",
        help="Elasticsearch URL for search indexing.\n[default: built-in Whoosh]",
    )

    parser.add_argument(
        "--admin-email", help="Administrator email for system notifications."
    )

    parser.add_argument(
        "--site-name",
        default="FluxLoad Pro",
        help="Site name displayed in UI.\n[default: FluxLoad Pro]",
    )

    parser.add_argument(
        "--version",
        action="version",
        version=f"fluxload {__version__}",
        help="Show the version number and exit.",
    )

    args = parser.parse_args()

    enable_registration = not args.disable_registration
    enable_file_sharing = not args.disable_file_sharing

    if not os.path.isdir(args.directory):
        print(f"Error: Directory '{args.directory}' does not exist.")
        sys.exit(1)

    # Apply --password to the global BEFORE create_app so login route picks it up
    if args.password:
        from getpass import getpass

        if args.password == "prompt":
            try:
                PASSWORD_GLOBAL = getpass("Enter admin password: ")
            except (EOFError, KeyboardInterrupt):
                print("\nPassword entry cancelled. Shutting down.")
                sys.exit(0)
        else:
            PASSWORD_GLOBAL = args.password
        from fluxload import advanced_server as _as
        _as.PASSWORD = PASSWORD_GLOBAL

    # Create app with proper config
    app = create_app(
        directory=args.directory,
        database_url=args.database_url,
    )

    # Configure app with command line args
    app.config["MAX_CONTENT_LENGTH"] = parse_size(args.max_upload_size)
    app.config["DEFAULT_QUOTA"] = parse_size(args.storage_quota)
    app.config["ENABLE_REGISTRATION"] = enable_registration
    app.config["ENABLE_FILE_SHARING"] = enable_file_sharing

    # Override system settings with command line args
    with app.app_context():
        update_system_settings(
            {
                "site_name": args.site_name,
                "admin_email": args.admin_email,
                "max_file_size": args.max_upload_size,
                "default_quota": args.storage_quota,
                "enable_registration": enable_registration,
                "enable_file_sharing": enable_file_sharing,
                "redis_url": args.redis_url,
                "elasticsearch_url": args.elasticsearch_url,
            }
        )

        # Reload settings into app.config so templates pick up CLI overrides
        for s in SystemSettings.query.all():
            app.config[s.key] = s.value

        # Initialize search index
        print("🔍 Initializing search index...")
        try:
            SEARCH_ENGINE.index_directory(args.directory)
            print(f"✅ Search index initialized for: {args.directory}")
        except Exception as e:
            print(f"⚠️  Warning: Could not initialize search index: {e}")

    # Setup admin user in DB if password is set
    if args.password:
        from fluxload.models import User

        with app.app_context():
            admin_user = User.query.filter_by(username="admin").first()
            if not admin_user:
                admin_user = User(
                    username="admin",
                    email="admin@fluxload.local",
                    full_name="System Administrator",
                    role="admin",
                    storage_quota=parse_size(args.storage_quota),
                )
                admin_user.set_password(PASSWORD_GLOBAL)
                db.session.add(admin_user)
                db.session.commit()
                print("✅ Admin user created successfully")
            else:
                admin_user.set_password(PASSWORD_GLOBAL)
                db.session.commit()
                print("✅ Admin password updated")

    # Get local IP and display server info
    host = args.bind if args.bind != "0.0.0.0" else get_local_ip()
    url = f"http://{host}:{args.port}"

    print(f"""
🚀 FluxLoad Pro v{__version__} Starting...

🌐 Server Information:
   URL: {url}
   Directory: {args.directory}
   Port: {args.port}
   Host: {host}
   Workers: {args.workers}
   Debug: {args.debug}
   Dev Mode: {args.dev_mode}

🔧 Features Enabled:
   User Registration: {"✅" if enable_registration else "❌"}
   File Sharing: {"✅" if enable_file_sharing else "❌"}
   Search Engine: ✅
   Real-time Collaboration: ✅
   Multi-user Support: ✅
   File Versioning: ✅
   Admin Dashboard: ✅
   API Endpoints: ✅

💾 Storage Configuration:
   Max Upload Size: {args.max_upload_size}
   Default User Quota: {args.storage_quota}
   Database: {"SQLite (built-in)" if not args.database_url else args.database_url}
   Search Index: {"Whoosh (built-in)" if not args.elasticsearch_url else "Elasticsearch"}

📁 Directory Structure:
   Upload Directory: {args.directory}
   Database: {args.database_url or f"sqlite:///{args.directory}/fluxload.db"}
   Search Index: {args.directory}/search_index
   Logs: {args.directory}/logs
""")

    try:
        import qrcode

        print(f"\n📱 Scan QR code to connect from mobile:")
        qr = qrcode.QRCode()
        qr.add_data(url)
        qr.make(fit=True)
        qr.print_ascii(tty=True)
    except ImportError:
        print("\n📱 Install 'qrcode[pil]' for QR code support: pip install qrcode[pil]")
    except Exception as e:
        print(f"\n⚠️  Could not generate QR code: {e}")

    # Configure Redis session storage if URL provided
    if args.redis_url:
        try:
            import redis as redis_module
            from flask_session import Session

            app.config["SESSION_TYPE"] = "redis"
            app.config["SESSION_REDIS"] = redis_module.from_url(args.redis_url)
            app.config["SESSION_PERMANENT"] = False
            app.config["SESSION_USE_SIGNER"] = True
            Session(app)
            print(f"✅ Redis session storage configured at {args.redis_url}")
        except ImportError:
            print(
                "⚠️  Flask-Session or redis not installed. Run: pip install Flask-Session redis"
            )
        except Exception as e:
            print(f"⚠️  Failed to configure Redis session storage: {e}")

    # Configure Elasticsearch search if URL provided
    if args.elasticsearch_url:
        try:
            from elasticsearch import Elasticsearch

            es_client = Elasticsearch(args.elasticsearch_url)
            if es_client.ping():
                print(f"✅ Elasticsearch connected at {args.elasticsearch_url}")
                app.config["ELASTICSEARCH_CLIENT"] = es_client
            else:
                print(f"⚠️  Could not connect to Elasticsearch at {args.elasticsearch_url}")
        except ImportError:
            print("⚠️  elasticsearch-py not installed. Run: pip install elasticsearch")
        except Exception as e:
            print(f"⚠️  Failed to connect Elasticsearch: {e}")

    # Setup background tasks
    file_monitor = setup_background_tasks(app)

    # Graceful shutdown handler
    def signal_handler(signum, frame):
        print(f"\n🛑 Shutting down gracefully...")
        if file_monitor:
            file_monitor.stop()
        db.session.remove()
        sys.exit(0)

    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    # Open browser if requested
    if args.open:
        import webbrowser
        import threading

        threading.Timer(1, lambda: webbrowser.open(url)).start()

    # Start server
    print(f"\n🎯 Server starting at {url}")
    print("Press Ctrl+C to stop the server")

    try:
        if args.dev_mode:
            app.run(
                host=args.bind,
                port=args.port,
                debug=True,
                threaded=True,
            )
        else:
            # Production server
            if args.workers > 1:
                print("⚠️  Multi-worker mode requires gunicorn; falling back to threaded mode.")
                print(
                    f"   For production: gunicorn -w {args.workers} -b {args.bind}:{args.port} fluxload.advanced_server:app"
                )

            app.run(host=args.bind, port=args.port, debug=args.debug, threaded=True)

    except KeyboardInterrupt:
        print("\n👋 Server stopped by user")
    except Exception as e:
        print(f"\n❌ Server error: {e}")
        sys.exit(1)


def get_local_ip():
    """Get local IP address."""
    try:
        import socket

        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))
            return s.getsockname()[0]
    except Exception:
        return "127.0.0.1"


def parse_size(size_str):
    """Parse size string like '100MB' to bytes."""
    import re

    size_str = size_str.upper().strip()

    # Define conversion factors
    units = {"B": 1, "KB": 1024, "MB": 1024**2, "GB": 1024**3, "TB": 1024**4}

    # Extract number and unit
    match = re.match(r"^(\d+(?:\.\d+)?)\s*([A-Z]+)$", size_str)
    if not match:
        return 100 * 1024 * 1024  # Default to 100MB

    number = float(match.group(1))
    unit = match.group(2)

    return int(number * units.get(unit, 1))


def update_system_settings(settings):
    """Update system settings in database."""
    for key, value in settings.items():
        setting = SystemSettings.query.get(key)
        if setting:
            setting.value = value
            setting.updated_at = datetime.now(timezone.utc)
        else:
            setting = SystemSettings(key=key, value=value)
            db.session.add(setting)
    db.session.commit()


if __name__ == "__main__":
    main()
