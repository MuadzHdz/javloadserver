#!/bin/bash

# Simple build script for Android APK without Gradle

echo "🔥 Building JavLoadServer Android Application..."

# Create output directory
mkdir -p output

# Create a simple APK structure
echo "📦 Creating APK structure..."

# Copy source files to output
cp -r app/src output/
cp -r app/res output/
cp app/build.gradle output/
cp app/proguard-rules.pro output/

# Create AndroidManifest.xml in output
cp app/src/main/AndroidManifest.xml output/

echo "✅ Android project structure created in 'output' directory"
echo ""
echo "📱 To build the APK:"
echo "1. Open Android Studio"
echo "2. Import the 'output' directory"
echo "3. Build > Build APK(s)"
echo ""
echo "🔧 Alternative: Use command line with Android SDK tools:"
echo "   cd output"
echo "   $ANDROID_HOME/tools/android update project --path ."
echo "   ant debug"