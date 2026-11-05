#!/usr/bin/env bash
set -e

# ==============================================================================
# Script: generate-keystore.sh
# Purpose: Educational helper to generate an Android release signing keystore
#          using standard OpenJDK 'keytool'.
# ==============================================================================

KEYSTORE_FILE="${1:-release.keystore}"
KEY_ALIAS="${2:-todoapp_key}"
VALIDITY_DAYS="${3:-10000}"

echo "========================================================"
echo " Android Release Keystore Generator (Educational Tool)"
echo "========================================================"
echo "Target Keystore File : $KEYSTORE_FILE"
echo "Key Alias            : $KEY_ALIAS"
echo "Validity (Days)      : $VALIDITY_DAYS"
echo "--------------------------------------------------------"

if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️ Warning: '$KEYSTORE_FILE' already exists!"
    read -p "Do you want to overwrite it? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 1
    fi
    rm "$KEYSTORE_FILE"
fi

echo "Generating RSA 2048-bit keypair using PKCS12 storetype..."
keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity "$VALIDITY_DAYS" \
    -storetype PKCS12 \
    -dname "CN=TodoApp, OU=Mobile, O=Educational, L=City, ST=State, C=US"

echo
echo "✅ Keystore successfully created at: $KEYSTORE_FILE"
echo
echo "--------------------------------------------------------"
echo " Extracting Certificate Fingerprints (SHA-256 / SHA-1)"
echo " (These fingerprints are required for Google OAuth, Firebase, etc.)"
echo "--------------------------------------------------------"
keytool -list -v -keystore "$KEYSTORE_FILE" -alias "$KEY_ALIAS" | grep -E "SHA1:|SHA256:|Owner:|Issuer:"

echo
echo "--------------------------------------------------------"
echo " Next Steps to sign your release build:"
echo " 1. Copy 'keystore.properties.example' to 'keystore.properties'"
echo " 2. Fill in the storeFile, storePassword, and keyPassword"
echo " 3. Run: ./gradlew assembleRelease"
echo "========================================================"
