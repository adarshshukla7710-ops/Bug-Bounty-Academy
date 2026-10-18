#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "      BugBounty Academy - Automated APK Builder           "
echo "=========================================================="

WORKSPACE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$WORKSPACE_DIR"

# 1. Check for Java / JDK
if ! command -v javac &> /dev/null; then
    echo "[-] JDK 17+ not detected in PATH."
    if [ -d "/Library/Java/JavaVirtualMachines" ] && [ "$(ls -A /Library/Java/JavaVirtualMachines 2>/dev/null)" ]; then
        echo "[+] Found existing JVM in /Library/Java/JavaVirtualMachines."
        export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
    elif command -v brew &> /dev/null; then
        echo "[*] Attempting to locate or install openjdk via Homebrew..."
        if [ ! -d "/usr/local/opt/openjdk" ] && [ ! -d "/opt/homebrew/opt/openjdk" ]; then
            echo "[*] Installing openjdk via brew..."
            brew install openjdk
        fi
        if [ -d "/opt/homebrew/opt/openjdk" ]; then
            export JAVA_HOME="/opt/homebrew/opt/openjdk"
            export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
        elif [ -d "/usr/local/opt/openjdk" ]; then
            export JAVA_HOME="/usr/local/opt/openjdk"
            export PATH="/usr/local/opt/openjdk/bin:$PATH"
        fi
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    echo "[+] Using JAVA_HOME: $JAVA_HOME"
fi

# Verify Java
if command -v java &> /dev/null; then
    echo "[+] Java version:"
    java -version
else
    echo "[-] ERROR: Java runtime is still missing. Please install JDK 17+ or run: brew install openjdk"
    exit 1
fi

# 2. Ensure gradlew is executable
chmod +x ./gradlew

# 3. Assemble Debug APK
echo "[*] Running Gradle build: ./gradlew assembleDebug --no-daemon ..."
./gradlew assembleDebug --no-daemon

# 4. Check for generated APK
OUTPUT_APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$OUTPUT_APK" ]; then
    echo "[+] Build Successful!"
    cp "$OUTPUT_APK" "BugBountyAcademy-v1.0.0-debug.apk"
    echo "=========================================================="
    echo "SUCCESS: Debug APK generated at:"
    echo "  $WORKSPACE_DIR/BugBountyAcademy-v1.0.0-debug.apk"
    echo "=========================================================="
else
    echo "[-] APK file was not found at $OUTPUT_APK"
    exit 1
fi
