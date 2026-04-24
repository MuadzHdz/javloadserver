"""
Shared utilities for FluxLoad
"""

import os
import mimetypes
from functools import wraps
from flask import flash, redirect, url_for


def validate_path(path, base_dir):
    """Validate that a path is within the base directory (prevent path traversal)"""
    try:
        abs_base = os.path.abspath(base_dir)
        abs_path = os.path.abspath(path)
        return abs_path.startswith(abs_base)
    except Exception:
        return False


def get_file_info(file_path):
    """Get file information for preview"""
    file_stat = os.stat(file_path)
    mime_type, _ = mimetypes.guess_type(file_path)

    is_text = (
        mime_type
        and mime_type.startswith("text/")
        or file_path.endswith(
            (".txt", ".md", ".py", ".js", ".html", ".css", ".json", ".xml", ".csv")
        )
    )
    is_image = mime_type and mime_type.startswith("image/")

    return {
        "size": file_stat.st_size,
        "mime_type": mime_type,
        "is_text": is_text,
        "is_image": is_image,
    }


def read_file_content(file_path, max_size=1024 * 1024):
    """Read file content safely for preview"""
    file_stat = os.stat(file_path)
    if file_stat.st_size > max_size:
        return None

    try:
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read()
    except UnicodeDecodeError:
        try:
            with open(file_path, "r", encoding="latin-1") as f:
                return f.read()
        except Exception:
            return None
    except Exception:
        return None


def get_previewable_extensions():
    """Get list of previewable file extensions"""
    return (
        ".txt",
        ".md",
        ".py",
        ".js",
        ".html",
        ".css",
        ".json",
        ".xml",
        ".csv",
        ".jpg",
        ".jpeg",
        ".png",
        ".gif",
        ".bmp",
        ".svg",
        ".webp",
    )
