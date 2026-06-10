# Future Roadmap

This document outlines the planned features and architectural changes for upcoming releases of ResumeAI Pro.

## Q3 2026
- **Cloud Sync**: Introduce Firebase Authentication and Cloud Firestore to allow users to sync resumes across multiple devices.
- **Cover Letter Generator**: Add a new module that uses the existing AI service to generate tailored cover letters based on the user's resume and a target job description.
- **Enhanced PDF Exporter**: Implement a more robust PDF rendering engine with support for complex SVG graphics and custom fonts.

## Q4 2026
- **Interview Prep AI**: Introduce a mock interview feature where the AI asks personalized questions based on the resume and the target job description.
- **Web Dashboard**: Create a companion web application (using Kotlin Multiplatform or a separate web stack) to edit resumes on desktop.
- **Premium Subscription**: Integrate Google Play Billing for premium AI models and unlimited token usage.

## Technical Debt / Architecture Goals
- Complete test coverage: Increase unit test coverage for Domain UseCases to 80%+.
- Implement UI tests using Compose Test rules.
- Migrate from Retrofit to Ktor client (exploratory).
