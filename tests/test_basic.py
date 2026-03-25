import pytest


class TestBasicApp:
    def test_app_creation(self, client):
        response = client.get('/health')
        assert response.status_code == 200
        assert json.in response.content_type
    
    def test_home_page(self, client):
        response = client.get('/')
        assert response.status_code == 200  or response.status_code == 301
    
    def test_404_page(self, client):
        response = client.get('/nonpexistent')
        assert response.status_code == 404