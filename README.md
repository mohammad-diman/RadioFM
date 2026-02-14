# RadioFM 📻

A modern Android Radio streaming application built with Jetpack Compose and Media3 (ExoPlayer). This app allows users to search and stream radio stations from all over the world using the Radio Garden API.

## ✨ Features

- **Global Radio Search**: Search for thousands of radio stations globally.
- **High-Quality Streaming**: Powered by Media3 ExoPlayer for stable and high-quality audio streaming.
- **Background Playback**: Continue listening to your favorite stations even when the app is in the background.
- **Modern UI**: Fully built with Jetpack Compose following Material 3 design guidelines.
- **Dynamic Player Bar**: Easy access to playback controls from any screen.
- **Image Loading**: Beautiful station icons powered by Coil.

## 🚀 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Audio Engine**: [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Dependency Management**: Gradle Kotlin DSL

## 🛠️ Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/mohammad-diman/RadioFM.git
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **File > Open**, then navigate to the cloned directory.

3. **Build the project**:
   Wait for Gradle to sync and then build the project.

4. **Run the app**:
   Connect an Android device or use an emulator (API 24 or higher) and click **Run**.

## 📖 How it Works

The app interacts with the Radio Garden API to fetch station data. It uses a `PlaybackService` to manage a `MediaSession`, ensuring that playback state is synchronized across the UI and system media controls.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Built by [Mohammad Diman](https://github.com/mohammad-diman)
