#!/usr/bin/env bash
# Delegates Gradle wrapper calls from the workspace root to the notes_frontend module.
# This is needed for CI environments that invoke ./gradlew from the repository root.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/notes_frontend"
exec bash ./gradlew "$@"
