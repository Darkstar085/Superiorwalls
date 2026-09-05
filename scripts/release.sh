#!/usr/bin/env bash
set -euo pipefail

# Prepare and publish a Superiorwalls release.
# Usage: ./scripts/release.sh 0.6.0

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 0.6.0"
  exit 1
fi

VERSION="$1"
TAG="v${VERSION}"
BUILD_FILE="app/build.gradle.kts"

if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]]; then
  echo "Invalid version: $VERSION"
  echo "Use a version such as 0.6.0 or 0.6.0-beta1."
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash your changes first."
  exit 1
fi

if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Tag already exists: $TAG"
  exit 1
fi

CURRENT_VERSION="$(grep -oP 'versionName\s*=\s*"\K[^"]+' "$BUILD_FILE" | head -n 1)"
CURRENT_CODE="$(grep -oP 'versionCode\s*=\s*\K[0-9]+' "$BUILD_FILE" | head -n 1)"
NEXT_CODE=$((CURRENT_CODE + 1))

if [[ "$CURRENT_VERSION" == "$VERSION" ]]; then
  echo "Version is already $VERSION. Nothing to release."
  exit 1
fi

sed -i -E "s/versionCode = [0-9]+/versionCode = ${NEXT_CODE}/" "$BUILD_FILE"
sed -i -E "s/versionName = \"[^\"]+\"/versionName = \"${VERSION}\"/" "$BUILD_FILE"

CHANGELOG_FILE="$(mktemp)"
trap 'rm -f "$CHANGELOG_FILE"' EXIT
./scripts/generate_changelog.sh "$CHANGELOG_FILE"

echo "Running release build and tests..."
./gradlew testDebugUnitTest assembleRelease

CHANGE_ID="I$(printf '%s' "${VERSION}-$(date +%s%N)" | sha256sum | cut -c1-40)"

printf 'chore: prepare release %s\n\n- Update the app version to %s.\n- Increase the Android version code for the new release.\n- Verify the release build and tests before tagging.\n\nChange-Id: %s\nSigned-off-by: S I P U N <sipunkumar85@gmail.com>\n' \
  "$VERSION" "$VERSION" "$CHANGE_ID" > /tmp/release-commit-message.txt

git add "$BUILD_FILE"
git commit -s -F /tmp/release-commit-message.txt

git tag -a "$TAG" -m "Release $TAG"

git push origin HEAD:main
git push origin "$TAG"

echo
printf 'Release %s is tagged and pushed.\n' "$TAG"
echo "GitHub Actions will build the APK and publish the GitHub release."
