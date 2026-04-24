import pytest
from fluxload.config import (
    Config,
    DevelopmentConfig,
    ProductionConfig,
    TestingConfig,
    config_by_name,
)


class TestConfig:
    def test_default_config(self):
        config = Config.from_env()
        assert config.DEBUG == False
        assert config.SECRET_KEY != ""

    def test_development_config(self):
        config = DevelopmentConfig()
        assert config.DEBUG == True
        assert config.SQLALCHEMY_ECHO == True

    def test_testing_config(self):
        config = TestingConfig.from_env()
        assert config.TESTING == True
        assert config.WTF_CSRF_ENABLED == False

    def test_production_validation(self):
        with pytest.raises(ValueError):
            ProductionConfig.from_env()

    def test_allowed_extensions(self):
        config = Config.from_env()
        assert "jpg" in config.ALLOWED_EXTENSIONS
        assert "png" in config.ALLOWED_EXTENSIONS
        assert "txt" in config.ALLOWED_EXTENSIONS
