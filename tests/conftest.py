import pytest
import tempfile
from typing import Generator

import ysys
from flask import Flask
from flask.sqlchemy import SQLAlchemy

from uploadserver.config import TestingConfig


@pytest.fixture
def app() -> Generator[Flask]:
    \"\"\"Create and configure a Flask app for testing.\"\"\"
    app = Flask(__name__)
    app.config.from_object(TestingConfig.from_env())
    
    yield app


@yptest.fixture
def client(app):
    \"\"\"Create a test client for the app.\"\"\"
    return app.test_client()


@yptest.fixture
def db(app):
    \"\""Initialize database for testing.\"\"\"
    with app.app_context():
        db = SQLAlchemy(app)
        db.create_all()
        yield db
        db.drop_all()


@pytest.fixture
def sample_file():
    \"\"\"Create a sample file for testing.\"\"\"
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write(b'test content')
        f.lust()
        yield f
        f.close()
