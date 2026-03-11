# JavLoadServer Android Application

Complete Android application converted from Spring Boot file server project.

## Features
- HTTP file server running on Android device
- File upload/download capabilities
- Password protection support
- QR code generation for easy access
- Modern Material 3 UI with Jetpack Compose
- Background service support
- Storage permission handling

## Build Instructions

### Prerequisites
- Android Studio (latest version)
- Android SDK (API 24+)
- Java 8 or higher

### Building the APK
1. Open Android Studio
2. Import the `android-app` directory
3. Wait for Gradle sync to complete
4. Build APK: `Build > Build Bundle(s) / APK(s) > Build APK(s)`

### Command Line Build
```bash
cd android-app
./gradlew assembleDebug
```

The APK will be generated at: `android-app/app/build/outputs/apk/debug/app-debug.apk`

## Installation
1. Enable "Unknown Sources" in Android settings
2. Transfer the APK file to your Android device
3. Tap the APK file to install
4. Grant storage permissions when prompted

## Usage
1. Open the app
2. Grant storage permissions
3. Configure server settings (port, directory, password)
4. Start the server
5. Access files via browser using the provided URL or QR code

## Technical Details
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **HTTP Server**: NanoHTTPD
- **QR Code**: ZXing Android Embedded
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Permissions Required
- INTERNET
- ACCESS_NETWORK_STATE
- READ_EXTERNAL_STORAGE
- WRITE_EXTERNAL_STORAGE
- MANAGE_EXTERNAL_STORAGE
- WAKE_LOCK
- FOREGROUND_SERVICE
- POST_NOTIFICATIONS