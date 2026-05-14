import pytest
import sys
import tempfile
from typing import Generator

from fluxload import server


@pytest.fixture
def app() -> Generator:
    """Create and configure a Flask app for testing."""
    app = server.create_app()
    app.config.update({
        "TESTING": True,
        "SERVER_NAME": "localhost",
    })
    yield app


@pytest.fixture
def client(app):
    """Create a test client for the app."""
    return app.test_client()


@pytest.fixture
def sample_file():
    """Create a sample file for testing."""
    with tempfile.NamedTemporaryFile(mode="w", delete=False, suffix=".txt") as f:
        f.write("test content")
        f.flush()
        yield f
