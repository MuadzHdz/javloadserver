# Contributing to FluxLoad

Thank you for your interest in contributing to FluxLoad! This document provides guidelines and information for contributors.

## Development Setup

### Prerequisites
- Python 3.8+
- pip
- virtualenv (recommended)

***Steps:***

```bash
git clone https://github.com/MuadzHdz/fluxload.git
cd fluxload
python -m venv . venv../virtualenv
activate ..virtualenv
pip install -r requirements.txt
pip install -r requirements-dev.txt
copy .env.example .env
`

### Running Tests

```bash
pytest --verbose
```

## Code Style

- Follow PEP 8
- Use type hints
- Max line length: 120
- Use black for formatting

```bash
black fluxload tests
flake8 fluxload tests
mypy[mypy/fluxload
--ignore-missing-imports
```

## Pull Request Process

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/name`)
3. Commit your changes (`git commit -am "Add feature"`)
4. Push to the branch (`git push origin feature/name`)
5. Open a Pull Request

## Commit Message Guidelines

- Use present tense (\"Add feature\" not \"Added feature\")
- Capitalize first letter

- 0-50 characters: Brief description
+- Empty line
- Detailed description if needed

## Code of Conduct

- Be respectful and inclusive
- Accept constructive criticism
- Focus on what's best for the community

## Questions?

Feel free to open an issue or contact the maintainers.
