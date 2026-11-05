#!/usr/bin/env bash
set -e

# ==============================================================================
# Script: verify-signature.sh
# Purpose: Educational helper to verify signatures of Keystores and APKs.
# ==============================================================================

TARGET="${1}"

if [ -z "$TARGET" ]; then
    echo "Usage: $0 <path-to-keystore.jks | path-to-app.apk>"
    exit 1
fi

if [ ! -f "$TARGET" ]; then
    echo "Error: File not found at '$TARGET'"
    exit 1
fi

# Locate apksigner if available
find_apksigner() {
    if command -v apksigner &> /dev/null; then
        echo "apksigner"
        return
    fi
    for dir in "$ANDROID_HOME/build-tools" "$HOME/Android/Sdk/build-tools" "$HOME/Library/Android/sdk/build-tools"; do
        if [ -d "$dir" ]; then
            local found
            found=$(find "$dir" -name apksigner -type f 2>/dev/null | sort -V | tail -n 1)
            if [ -n "$found" ] && [ -x "$found" ]; then
                echo "$found"
                return
            fi
        fi
    done
    echo ""
}

case "$TARGET" in
    *.keystore|*.jks|*.p12)
        echo "========================================================"
        echo " Inspecting Keystore Details with keytool"
        echo "========================================================"
        keytool -list -v -keystore "$TARGET"
        ;;
    *.apk)
        echo "========================================================"
        echo " Verifying APK Signature"
        echo "========================================================"
        APKSIGNER_BIN=$(find_apksigner)
        if [ -n "$APKSIGNER_BIN" ]; then
            "$APKSIGNER_BIN" verify --verbose --print-certs "$TARGET"
        else
            echo "apksigner not found in PATH or Android SDK."
            if command -v jarsigner &> /dev/null; then
                echo "Falling back to jarsigner verification (v1 only):"
                jarsigner -verify -verbose -certs "$TARGET"
            else
                echo "Neither apksigner nor jarsigner found. Please install Android Build-Tools."
                exit 1
            fi
        fi
        ;;
    *)
        echo "Unrecognized file extension. Expected .keystore, .jks, .p12, or .apk"
        exit 1
        ;;
esac
