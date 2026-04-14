# Seek And Catch

Welcome to SeekAndCatch, a mobile game where you test your speed and accuracy by clicking on the right items to score points!

## Table of Contents

1. [Introduction](#introduction)
2. [Gameplay](#gameplay)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Requirements](#requirements)
6. [Setup & Installation](#setup--installation)
7. [Scripts & Commands](#scripts--commands)
8. [Testing](#testing)
9. [Contributing](#contributing)
10. [License](#license)

## Introduction

Seek And Catch is a simple yet addictive mobile game where players click on items within a grid to score points. The game features different levels with increasing difficulty, requiring players to be quick and accurate in their selections.

## Gameplay

### Starting the Game
- Upon starting the game, the player is presented with a grid of items.
- A specific item is highlighted as the "goal" which the player needs to click on to score points.

### Clicking Mechanics
- Clicking on the correct item increases the player's coefficient and adds score points.
- Clicking on the wrong item decreases the coefficient.
- Missing items reduces the coefficient progress. If the progress reaches zero, the coefficient resets to its basic level.

### Level Progression
- The game also features increasing speed and coefficient progression, making it more challenging over time.

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture:** MVI (Intent -> ViewModel -> Repository -> Data Source)
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Asynchrony:** Kotlin Coroutines & Flow
- **Data Persistence:** 
    - [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences/Settings)
    - [Firebase Firestore](https://firebase.google.com/docs/firestore) (Leaderboard & Account)
- **Other Firebase Services:** Analytics, Realtime Database
- **Build System:** Gradle Kotlin DSL (`.gradle.kts`) with Version Catalogs

## Project Structure

The project follows a multi-module architecture:

- `app`: The main entry point of the application. Contains `MainActivity` and `SeekCatchApplication`.
- `core`: Shared logic and components.
    - `common`: Common utilities, DI modules, and managers (e.g., `VisualFeedbackManager`).
    - `designsystem`: UI components and theme.
    - `domain`: Core business logic and use cases.
    - `model`: Shared data models.
- `data`: Implementation of repositories and data sources (DataStore, Firebase).
- `feature`: Independent feature modules.
    - `gameplay`: Core game logic and UI (Flash/Flow games).
    - `leaderboard`: High scores and rankings.
    - `settings`: User preferences.
    - `account`: User profile and authentication.
    - `colorpicker`: Custom UI for color selection.
- `data-test`: Test doubles and fakes for repositories.
- `singleselectionlazyrow`: Custom library for single selection lists.

## Requirements

- Android Studio Jellyfish | 2023.3.1 or newer (Recommended)
- JDK 17
- Android SDK 26+ (Minimum SDK)
- Target SDK 35

## Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/SeekAndCatch.git
   cd SeekAndCatch
   ```

2. **Firebase Setup:**
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.maxot.seekandcatch`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable Firestore and Analytics in the Firebase Console.

3. **Open in Android Studio:**
   - Launch Android Studio and select **Open**.
   - Navigate to the project root and click **OK**.
   - Wait for Gradle sync to complete.

## Scripts & Commands

The project uses Gradle wrapper for builds and management.

- **Build project:**
  ```bash
  ./gradlew assembleDebug
  ```

- **Run unit tests:**
  ```bash
  ./gradlew test
  ```

- **Run instrumentation tests:**
  ```bash
  ./gradlew connectedAndroidTest
  ```

- **Lint check:**
  ```bash
  ./gradlew lint
  ```

## Testing

The project emphasizes test-driven development and quality:

- **Unit Tests:** Located in `src/test`. Powered by JUnit 4 and Mockito-Kotlin.
- **UI Tests:** Located in `src/androidTest`. Uses Compose UI Testing library.
- **Test Fakes:** The `data-test` module provides fake implementations for repositories to facilitate isolated testing.

## Contributing

Contributions to Seek And Catch are welcome! If you have any suggestions, bug fixes, or feature requests, feel free to submit a pull request.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details (TODO: Add LICENSE file).


