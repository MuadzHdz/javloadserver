# ==============================================
# UPLOADSERVER - Multi-stage Dockerfile
# ==============================================

# Stage 1: Builder
FROM python:3.11-slim as builder

WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# Stage 2: Production
FROM python:3.11-slim

# Create non-root user
RUN groupadd -r uploadserver && useradd -r -g uploadserver uploadserver

WORKDIR /app

# Install runtime dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq5 \
    libmagic1 \
    && rm -rf /var/lib/apt/lists/

# Copy Python packages from builder
COPY --from=builder /root/.local /home/uploadserver/.local

# Copy application
COPY --chown=uploadserver:uploadserver . .

# Create necessary directories
RUN mkdir -p uploads search_index logs && chown -R uploadserver:uploadserver /app

# Switch to non-root user
USER uploadserver

# Environment
ENV PATH=/home/uploadserver/.local/bin:$PATH
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV FLASK_APP=uploadserver

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD ["python", "-c", "import requests; requests.get('http://localhost:5000/health')"] || exit 1

# Expose port
EXPOSE 5000

# Run with gunicorn
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "4", "--worker-class", "gevent", "--access-logfile", "-", "--error-logfile", "-", "uploadserver:create_app()"]