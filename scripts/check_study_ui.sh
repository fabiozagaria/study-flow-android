#!/usr/bin/env bash
# Keep status and screenshot collection in one shell: emulator-runner runs script lines separately.
set -uo pipefail
test_exit=0
./gradlew connectedDebugAndroidTest || test_exit=$?
adb pull /data/local/tmp/study-visual/ study-screenshots/ || true
exit "$test_exit"
