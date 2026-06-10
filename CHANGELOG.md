# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - Initial Release Preparation

### Added
- Complete modern UI using Jetpack Compose (Holographic, Neon, Glassmorphic components).
- AI resume analysis and enhancement via NVIDIA NIM API.
- Local storage using Room database.
- PDF and DOCX document generation features.
- Dynamic theme support (Dark/Light).
- ATS score calculation and dashboard.
- Job matching functionality.

### Security
- Excluded `local.properties` from version control to protect API keys.
- Reviewed codebase for hardcoded credentials (none found).

### Changed
- Standardized Android `.gitignore` to prevent accidental inclusion of build artifacts.
- Fixed `themes.xml` lint warning for SplashScreen API level 31 requirement.

### Removed
- Dead code, unused imports, and temporary stub files (`StepsStubs.kt`, `process_logo.py`).
