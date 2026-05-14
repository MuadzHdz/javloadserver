import pytest
import json


class TestBasicApp:
    def test_app_creation(self, client):
        response = client.get("/health")
        assert response.status_code == 200
        assert "json" in response.content_type

    def test_home_page(self, client):
        response = client.get("/")
        assert response.status_code in (200, 301, 302)

    def test_404_page(self, client):
        response = client.get("/nonexistent")
        assert response.status_code == 404
