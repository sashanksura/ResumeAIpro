# ResumeAI Pro

![ResumeAI Pro Boot]()

ResumeAI Pro is a modern, AI-powered Android application designed to help users create, analyze, and optimize their resumes using state-of-the-art Large Language Models (LLMs). Featuring a sleek, futuristic UI, this app leverages NVIDIA NIM API integration to bring advanced AI capabilities directly to your device.

## ✨ Features

- **AI-Powered Builder**: Automatically generate summaries, improve bullet points, and rewrite experiences based on your career goals.
- **ATS Optimization**: Get a detailed ATS (Applicant Tracking System) score and actionable advice on improving your resume's parsability.
- **Job Matching**: Paste a job description and let AI tailor your resume to perfectly match the role requirements.
- **Skill Suggestions**: Get AI-recommended skills based on your past experiences and target job.
- **Multiple Export Formats**: Export your polished resume to professional PDF or DOCX formats with multiple aesthetic templates.
- **Modern UI/UX**: Enjoy a cutting-edge interface featuring glassmorphism, holographic cards, and dynamic particle backgrounds.
- **Dark/Light Themes**: Fully customizable appearance with a stunning default dark theme.

## 🛠 Tech Stack

- **Architecture**: MVVM + Clean Architecture
- **UI Toolkit**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit & OkHttp
- **AI Integration**: NVIDIA NIM API (DeepSeek V4 Pro)
- **Async & Concurrency**: Kotlin Coroutines & Flow
- **Export Engines**: Custom PDF & DOCX generation

## 📸 Screenshots

*(Placeholders - Add actual screenshots here before final release)*

<p align="center">
  <img src="https://via.placeholder.com/200x400.png?text=Home+Screen" width="200" />
  <img src="https://via.placeholder.com/200x400.png?text=AI+Optimization" width="200" />
  <img src="https://via.placeholder.com/200x400.png?text=Templates" width="200" />
</p>

## 🚀 Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/resumepro.git
   ```

2. **Open in Android Studio:**
   Open the cloned directory in Android Studio (Ladybug or newer recommended).

3. **Configure API Key:**
   Create a `local.properties` file in the root directory if it doesn't exist, and add your NVIDIA API Key:
   ```properties
   NVIDIA_API_KEY=your_api_key_here
   ```

4. **Build and Run:**
   Sync the Gradle project and run the app on an emulator or physical device (API 26+).

## 🚧 Current Limitations

- Some UI features (like sharing resumes) are marked as Work In Progress (WIP).
- Cloud sync and user accounts are not yet implemented (all data is stored locally).

## 🗺 Roadmap

See [FUTURE_ROADMAP.md](FUTURE_ROADMAP.md) for details on upcoming features.

## 🤝 Contributing

Contributions are welcome! If you'd like to help improve ResumeAI Pro, please fork the repository, make your changes, and submit a pull request. 

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
