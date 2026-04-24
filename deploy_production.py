"""
Production deployment script for UploadServer Pro
"""

import os
import sys
import subprocess
import platform
from pathlib import Path

def check_system_requirements():
    """Check if system meets requirements"""
    print("🔍 Checking system requirements...")
    
    # Python version
    python_version = sys.version_info
    if python_version < (3, 8):
        print(f"❌ Python {python_version.major}.{python_version.minor} is too old. Requires Python 3.8+")
        return False
    else:
        print(f"✅ Python {python_version.major}.{python_version.minor}")
    
    # System architecture
    system = platform.system()
    machine = platform.machine()
    print(f"✅ System: {system} ({machine})")
    
    # Memory check (basic)
    try:
        import psutil
        memory = psutil.virtual_memory()
        if memory.total < 4 * 1024**3:  # Less than 4GB
            print(f"⚠️  Warning: Low memory ({memory.total // (1024**3)}GB). 8GB+ recommended")
        else:
            print(f"✅ Memory: {memory.total // (1024**3)}GB")
    except ImportError:
        print("⚠️  psutil not available - cannot check memory")
    
    return True

def install_dependencies():
    """Install all production dependencies"""
    print("\n📦 Installing production dependencies...")
    
    core_deps = [
        "Flask>=2.3",
        "Flask-SocketIO>=5.3",
        "Flask-SQLAlchemy>=3.0",
        "Flask-Login>=0.6",
        "SQLAlchemy>=2.0",
        "Werkzeug>=2.3",
        "psycopg2-binary>=2.9",
        "redis>=4.5",
        "celery>=5.2",
        "elasticsearch>=8.9",
        "boto3>=1.28",
        "whoosh>=2.7",
        "passlib[bcrypt]>=1.7",
        "python-jose[cryptography]>=3.3",
        "python-multipart>=0.0.6",
        "gunicorn>=21.0",
        "pillow>=9.5",
        "watchdog>=3.0",
        "psutil>=5.9",
        "python-magic>=0.4"
    ]
    
    try:
        for dep in core_deps:
            print(f"   Installing {dep}...")
            subprocess.check_call([sys.executable, "-m", "pip", "install", dep], stdout=subprocess.DEVNULL)
        print("✅ All dependencies installed successfully")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ Error installing dependencies: {e}")
        return False

def setup_production_environment():
    """Setup production environment"""
    print("\n🏗 Setting up production environment...")
    
    # Create directories
    directories = [
        "logs",
        "uploads",
        "config",
        "search_index",
        "backups",
        "ssl",
        "plugins"
    ]
    
    for directory in directories:
        Path(directory).mkdir(exist_ok=True)
        print(f"   ✅ Created {directory}/ directory")
    
    # Create production config
    config_content = f"""
# Production Configuration for UploadServer Pro
import os
from pathlib import Path

# Database Configuration
DATABASE_URL = os.getenv('DATABASE_URL', 'postgresql://uploadserver:password@localhost:5432/uploadserver')

# Redis Configuration
REDIS_URL = os.getenv('REDIS_URL', 'redis://localhost:6379/0')

# Security
SECRET_KEY = os.getenv('SECRET_KEY', None)  # Must be set in production
JWT_SECRET_KEY = os.getenv('JWT_SECRET_KEY', None)  # Must be set in production

# Storage
UPLOAD_FOLDER = Path(os.getenv('UPLOAD_FOLDER', '/var/lib/uploadserver/uploads'))
MAX_CONTENT_LENGTH = 100 * 1024 * 1024  # 100MB
DEFAULT_QUOTA = 10 * 1024 * 1024 * 1024  # 10GB

# Monitoring
LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
LOG_FILE = Path(os.getenv('LOG_FILE', '/var/log/uploadserver/app.log'))

# Performance
WORKERS = int(os.getenv('WORKERS', '4'))
CACHE_TYPE = os.getenv('CACHE_TYPE', 'redis')

# External Services
ELASTICSEARCH_URL = os.getenv('ELASTICSEARCH_URL', None)
AWS_ACCESS_KEY_ID = os.getenv('AWS_ACCESS_KEY_ID', None)
AWS_SECRET_ACCESS_KEY = os.getenv('AWS_SECRET_ACCESS_KEY', None)
AWS_S3_BUCKET = os.getenv('AWS_S3_BUCKET', None)

# Features
ENABLE_REGISTRATION = os.getenv('ENABLE_REGISTRATION', 'true').lower() == 'true'
ENABLE_FILE_SHARING = os.getenv('ENABLE_FILE_SHARING', 'true').lower() == 'true'
ENABLE_2FA = os.getenv('ENABLE_2FA', 'false').lower() == 'true'

# SSL/HTTPS
FORCE_HTTPS = os.getenv('FORCE_HTTPS', 'false').lower() == 'true'
SSL_CERT_PATH = os.getenv('SSL_CERT_PATH', '/etc/ssl/certs/uploadserver.crt')
SSL_KEY_PATH = os.getenv('SSL_KEY_PATH', '/etc/ssl/private/uploadserver.key')
"""
    
    with open("config/production.py", "w") as f:
        f.write(config_content)
    
    print("   ✅ Created config/production.py")
    
    # Create systemd service file
    service_content = f"""[Unit]
Description=UploadServer Pro - Enterprise File Sharing Platform
After=network.target postgresql.service redis.service
Wants=postgresql.service redis.service

[Service]
Type=exec
User=uploadserver
Group=uploadserver
WorkingDirectory={Path.cwd()}
Environment=PYTHONPATH={Path.cwd()}
Environment=CONFIG_PATH=production
ExecStart={sys.executable} -m uploadserver.advanced_main
    --database-url $DATABASE_URL
    --redis-url $REDIS_URL
    --workers $WORKERS
    --site-name "{{{{SITE_NAME}}}"
    --admin-email "{{{{ADMIN_EMAIL}}}"
ExecReload=/bin/kill -HUP $MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=uploadserver

[Install]
WantedBy=multi-user.target

[Install]
WantedBy=graphical-session.target
"""
    
    with open("uploadserverpro.service", "w") as f:
        f.write(service_content)
    
    print("   ✅ Created uploadserverpro.service")
    
    # Create nginx configuration
    nginx_config = f"""
# Nginx configuration for UploadServer Pro
upstream uploadserver {{
    server 127.0.0.1:8000;
    keepalive 32;
}}

server {{
    listen 80;
    listen [::]:80;
    server_name {{{{DOMAIN}}}};
    
    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}}

server {{
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name {{{{DOMAIN}}}};
    
    # SSL Configuration
    ssl_certificate /etc/ssl/certs/uploadserver.crt;
    ssl_certificate_key /etc/ssl/private/uploadserver.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # Security Headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # Client Upload Size
    client_max_body_size 100M;
    
    # WebSocket Support
    location /socket.io {{
        proxy_pass http://uploadserver;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }}
    
    location / {{
        proxy_pass http://uploadserver;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffer settings
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        proxy_busy_buffers_size 8k;
    }}
    
    # Static files (optional)
    location /static {{
        alias /var/lib/uploadserver/static;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }}
    
    # Logs
    access_log /var/log/nginx/uploadserver_access.log;
    error_log /var/log/nginx/uploadserver_error.log;
}}
"""
    
    with open("nginx-uploadserverpro.conf", "w") as f:
        f.write(nginx_config)
    
    print("   ✅ Created nginx-uploadserverpro.conf")

def setup_ssl_certificates():
    """Generate self-signed SSL certificates for development"""
    print("\n🔐 Setting up SSL certificates...")
    
    import subprocess
    try:
        # Generate private key
        subprocess.run([
            "openssl", "genrsa", "-out", "ssl/uploadserver.key", "2048"
        ], check=True, stdout=subprocess.DEVNULL)
        
        # Generate certificate
        subprocess.run([
            "openssl", "req", "-new", "-x509", "-key", "ssl/uploadserver.key",
            "-out", "ssl/uploadserver.crt", "-days", "365",
            "-subj", "/C=US/ST=State/L=City/O=UploadServer/CN=localhost"
        ], check=True, stdout=subprocess.DEVNULL)
        
        print("   ✅ Generated self-signed SSL certificate")
        print("   ⚠️  For production, use certificates from a trusted CA")
        
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("   ⚠️  OpenSSL not found. Please install OpenSSL or provide certificates.")

def main():
    """Main deployment script"""
    print("🚀 UploadServer Pro - Production Deployment Setup")
    print("=" * 50)
    
    # Check system requirements
    if not check_system_requirements():
        sys.exit(1)
    
    # Install dependencies
    if not install_dependencies():
        sys.exit(1)
    
    # Setup production environment
    setup_production_environment()
    
    # Setup SSL certificates
    setup_ssl_certificates()
    

    
    # Create requirements.txt
    requirements = """Flask>=2.3
Flask-SocketIO>=5.3
Flask-SQLAlchemy>=3.0
Flask-Login>=0.6
SQLAlchemy>=2.0
Werkzeug>=2.3
psycopg2-binary>=2.9
redis>=4.5
celery>=5.2
elasticsearch>=8.9
boto3>=1.28
whoosh>=2.7
passlib[bcrypt]>=1.7
python-jose[cryptography]>=3.3
python-multipart>=0.0.6
gunicorn>=21.0
pillow>=9.5
watchdog>=3.0
psutil>=5.9
python-magic>=0.4
gevent>=23.9
PyPDF2>=3.0
python-docx>=0.8.11
"""
    
    with open("requirements.txt", "w") as f:
        f.write(requirements)
    
    print("   ✅ Created requirements.txt")
    
    print(f"\n✅ Production setup complete!")
    print(f"\n📋 Next steps:")
    print(f"1. Review and update config/production.py")
    print(f"2. Set environment variables (DATABASE_URL, SECRET_KEY, etc.)")
    print(f"3. Install systemd service:")
    print(f"   sudo cp uploadserverpro.service /etc/systemd/system/")
    print(f"   sudo systemctl daemon-reload")
    print(f"   sudo systemctl enable uploadserverpro")
    print(f"   sudo systemctl start uploadserverpro")
    print(f"4. Configure nginx:")
    print(f"   sudo cp nginx-uploadserverpro.conf /etc/nginx/sites-available/")
    print(f"   sudo ln -s /etc/nginx/sites-available/uploadserverpro.conf /etc/nginx/sites-enabled/")
    print(f"   sudo nginx -t && sudo systemctl reload nginx")

    print(f"\n📚 Documentation: https://docs.uploadserverpro.com")
    print(f"🐛 Issues: https://github.com/MuadzHdz/uploadserverpro/issues")

if __name__ == "__main__":
    main()