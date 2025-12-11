#!/bin/bash
# Script to generate the release keystore for Quotey app
# Run this script once to generate the keystore file

KEYSTORE_FILE="quotey-release.jks"
KEYSTORE_PASSWORD="quotey2024"
KEY_ALIAS="quotey"
KEY_PASSWORD="quotey2024"

if [ -f "$KEYSTORE_FILE" ]; then
    echo "Keystore already exists at $KEYSTORE_FILE"
    exit 0
fi

keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias "$KEY_ALIAS" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=Quotey, OU=Development, O=Quotey, L=Internet, ST=Global, C=US"

echo "Keystore generated successfully at $KEYSTORE_FILE"
