# ==============================================
# Configuration Module
# ==============================================

import os


class Config:
    """Application configuration."""

    SECRET_KEY = os.getenv("SECRET_KEY", "dev-secret-key")
    FLASK_ENV = os.getenv("FLASK_ENV", "development")
    DEBUG = os.getenv("DEBUG", "False").lower() == "true"
    DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///fluxload.db")
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    MAX_CONTENT_LENGTH = int(os.getenv("MAX_FILE_SIZE", 100 * 1024 * 1024))
    UPLOAD_FOLDER = os.getenv("UPLOAD_FOLDER", "uploads")
    ALLOWED_EXTENSIONS = {
        "txt",
        "pdf",
        "png",
        "jpg",
        "jpeg",
        "gif",
        "doc",
        "docx",
        "mp3",
        "mp4",
        "zip",
        "rar",
        "7z",
        "tar",
        "gz",
    }
    BCRYPT_LOG_ROUNDS = 12
    JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", SECRET_KEY)
    JWT_ACCESS_TOKEN_EXPIRES = 3600
    RATELIMIT_STORAGE_URL = os.getenv("RATE_LIMIT_STORAGE_URL", "memory://")
    RATELIMIT_STRATEGY = "fixed-window"
    SEARCH_INDEX_DIR = os.getenv("SEARCH_INDEX_DIR", "search_index")
    QR_CODE_SIZE = 10
    QR_CODE_BORDER = 2

    @classmethod
    def from_env(cls):
        return cls()


class DevelopmentConfig(Config):
    DEBUG = True
    SQLALCHEMY_ECHO = True


class ProductionConfig(Config):
    DEBUG = False
    BCRYPT_LOG_ROUNDS = 13

    @classmethod
    def from_env(cls):
        config = cls()
        if config.SECRET_KEY == "dev-secret-key":
            raise ValueError("SECRET_KEY must be set in production")
        return config


class TestingConfig(Config):
    TESTING = True
    DEBUG = True
    DATABASE_URL = "sqlite:///test.db"
    WTF_CSRF_ENABLED = False


config_by_name = {
    "development": DevelopmentConfig,
    "production": ProductionConfig,
    "testing": TestingConfig,
    "default": DevelopmentConfig,
}
