#!/bin/bash

# Build script for JavLoadServer Android Application

echo "🔥 Building JavLoadServer Android Application..."

# Check if required tools are installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed"
    exit 1
fi

if ! command -v wget &> /dev/null && ! command -v curl &> /dev/null; then
    echo "❌ wget or curl is required for downloading Gradle"
    exit 1
fi

# Download Gradle if not present
GRADLE_VERSION="8.4"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

if [ ! -d "gradle" ]; then
    echo "📦 Downloading Gradle..."
    if command -v wget &> /dev/null; then
        wget -O gradle.zip "$GRADLE_URL"
    else
        curl -L -o gradle.zip "$GRADLE_URL"
    fi
    
    unzip gradle.zip
    mv gradle-${GRADLE_VERSION} gradle
    rm gradle.zip
fi

# Set GRADLE_HOME
export GRADLE_HOME="./gradle"
export PATH="$GRADLE_HOME/bin:$PATH"

# Build the APK
echo "🔨 Building APK..."
if [ -f "gradlew" ]; then
    chmod +x gradlew
    ./gradlew assembleDebug
else
    $GRADLE_HOME/bin/gradle assembleDebug
fi

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo "📲 Install: adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Build failed!"
    exit 1
fi