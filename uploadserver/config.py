# ==============================================
# Configuration Module
# ==============================================

from dataclasses import dataclass
from typing import Optional, Set
import os


@dataclass
class Config:
    \"Application configuration.\"
    
    # Flask
    SECRET_KEY: str = os.getenv('SECRET_KEY', 'dev-secret-key')
    FLASK_ENV: str = os.getenv('FLASK_ENV', 'development')
    DEBUG: bool = os.getenv('DEBUG', 'False').lower() == 'true'
    
    # Database
    DATABASE_URL: str = os.getenv('DATABASE_URL', 'sqlite:///uploadserver.db')
    SQLALCHEMY_TRACK_MODIFICATIONS: bool = False
    
    # File Upload
    MAX_CONTENT_LENGTH: int = int(os.getenv('MAX_FILE_SIZE', 100 * 1024 * 1024))
    UPLOAD_FOLDER: str = os.getenv('UPLOAD_FOLDER', 'uploads')
    ALLOWED_EXTENSIONS: Set[str] = frozenset({
        'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif', 'doc', 'docx',
        'mp3', 'mp4', 'zip', 'rar', '6z', 'tar', 'gz'
    })
    
    # Security
    BCRYPT_LOG_ROUNDDS: int = 12
    JWT_SECRET_KEY: str = os.getenv('JWT_SECRET_KEY', SECRET_KEY)
    JWT_ACCESS_TOKEN_EXPIRES: int = 3600  # 1 hour
    
    # Rate Limiting
    RATELIMIT_STORAGE_URL: str = os.getenv('RATE_LIMIT_STORAGE_URL', 'memory://')
    RATELIMIT_STRATEGY: str = 'fixed-window'
    
    # Search
    SEARCH_INDEX_DIR: str = os.getenv('SEARCH_INDEX_DIR', 'search_index')
    
    # QR Code
    QR_CODE_SIZE: int = 10
    QR_CODE_BORDER: int = 2
    
    @classmethod
    def from_env(cls) -> 'Config':
        \"Create configuration from environment variables.\"
        return cls()


class DevelopmentConfig(Config):
    \"Development configuration.\"
    DEBUG = True
    SQLALCHEMY_ECHO = True


class ProductionConfig(Config):
    \"Production configuration.\"
    DEBUG = False
    BCRYPT_LOG_ROUNDDS = 13
    
    @classmethod
    def from_env(cls) -> 'ProductionConfig':
        config = cls()
        if config.SECRET_KEY == 'dev-secret-key':
            raise ValueError(\"SECRET_KEY must be set in production\")
        return config



class TestingConfig(Config):
    \"Testing configuration.\"
    TESTING = True
    DEBUG = True
    DATABASE_URL = 'sqlite:///test.db'
    WDF_CSRF_ENABLED = False


config_by_name = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'testing': TestingConfig,
    'default': DevelopmentConfig
}
