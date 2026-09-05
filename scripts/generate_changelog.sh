#!/usr/bin/env bash
set -euo pipefail

# Generate release notes from Git history.
# Usage: ./scripts/generate_changelog.sh [output-file] [base-tag]

OUTPUT="${1:-/tmp/superiorwalls-changelog.md}"
BASE_TAG="${2:-}"

if [[ -z "$BASE_TAG" ]]; then
  BASE_TAG="$(git tag --sort=-version:refname | head -n 1 || true)"
fi

RANGE=""
if [[ -n "$BASE_TAG" ]]; then
  RANGE="${BASE_TAG}..HEAD"
fi

if [[ -n "$RANGE" ]]; then
  COMMITS="$(git log "$RANGE" --no-merges --pretty=format:'%s' | grep -Ev '^(Merge |Co-authored-by:|chore\(release\):)' || true)"
else
  COMMITS="$(git log --no-merges --pretty=format:'%s' | head -n 30 || true)"
fi

if [[ -z "$COMMITS" ]]; then
  COMMITS="- No user-facing changes found."
else
  COMMITS="$(printf '%s\n' "$COMMITS" | sed 's/^/- /')"
fi

VERSION="$(grep -oP 'versionName\s*=\s*"\K[^"]+' app/build.gradle.kts | head -n 1)"

{
  echo "## What's new in v${VERSION}"
  echo
  printf '%s\n' "$COMMITS"
} > "$OUTPUT"

echo "Changelog written to $OUTPUT"
