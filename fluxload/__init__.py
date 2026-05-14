"""FluxLoad: A simple, modern file server."""

__version__ = "1.2.9"

# Export create_app from advanced_server as default
from .advanced_server import create_app

__all__ = ["create_app", "__version__"]
