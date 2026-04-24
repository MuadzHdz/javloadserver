import pytest
import sys
import tempfile
from typing import Generator

from flask import Flask
from flask_sqlalchemy import SQLAlchemy

from fluxload.config import TestingConfig


@pytest.fixture
def app() -> Generator[Flask]:
    """Create and configure a Flask app for testing."""
    app = Flask(__name__)
    app.config.from_object(TestingConfig.from_env())

    yield app


@pytest.fixture
def client(app):
    """Create a test client for the app."""
    return app.test_client()


@pytest.fixture
def db(app):
    """Initialize database for testing."""
    with app.app_context():
        db = SQLAlchemy(app)
        db.create_all()
        yield db
        db.drop_all()


@pytest.fixture
def sample_file():
    """Create a sample file for testing."""
    with tempfile.NamedTemporaryFile(mode="w", delete=False) as f:
        f.write(b"test content")
        f.flush()
        yield f
        f.close()
