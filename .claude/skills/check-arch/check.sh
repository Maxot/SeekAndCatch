#!/bin/bash
# Enforces architecture boundary rules from docs/TECH_SPEC.md
# Usage: check.sh [--staged]
# Compatible with macOS bash 3.x (no mapfile)

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
VIOLATIONS=0
STAGED_ONLY=false

if [[ "${1:-}" == "--staged" ]]; then
  STAGED_ONLY=true
fi

# Collect files to scan into a temp file (macOS bash 3 compatible)
TMPFILE=$(mktemp /tmp/check-arch-XXXXXX.txt)
trap 'rm -f "$TMPFILE"' EXIT

if $STAGED_ONLY; then
  git -C "$REPO_ROOT" diff --cached --name-only --diff-filter=ACM \
    | grep '\.kt$' \
    | sed "s|^|$REPO_ROOT/|" > "$TMPFILE"
else
  find "$REPO_ROOT" -name "*.kt" \
    -not -path "*/build/*" \
    -not -path "*/.gradle/*" \
    -not -path "*/data-test/*" > "$TMPFILE"
fi

if [[ ! -s "$TMPFILE" ]]; then
  echo "check-arch: no Kotlin files to check."
  exit 0
fi

error() {
  echo "check-arch VIOLATION: $1"
  VIOLATIONS=$((VIOLATIONS + 1))
}

# ─────────────────────────────────────────────────────────────────────────────
# Rule 1: No Firestore imports in ViewModel or UI (feature) files
# Firestore must only appear in data/firebase/datasource/
# ─────────────────────────────────────────────────────────────────────────────
while IFS= read -r f; do
  # ViewModel files: anywhere in feature: modules
  if echo "$f" | grep -q '/feature/' && echo "$f" | grep -qE 'ViewModel\.kt$'; then
    if grep -q 'com\.google\.firebase\.firestore' "$f"; then
      error "Firestore import in ViewModel: $f"
    fi
  fi

  # UI files: Screen or Layout composables in feature: modules
  if echo "$f" | grep -q '/feature/' && echo "$f" | grep -qE '(Screen|Layout|Dialog)\.kt$'; then
    if grep -q 'com\.google\.firebase\.firestore' "$f"; then
      error "Firestore import in UI composable: $f"
    fi
    if grep -q 'com\.google\.firebase\.auth' "$f"; then
      error "Firebase Auth import in UI composable: $f"
    fi
  fi

  # app module UI and ViewModel files
  if echo "$f" | grep -q '/app/src/' && echo "$f" | grep -qE '\.(kt)$'; then
    if grep -q 'com\.google\.firebase\.firestore' "$f"; then
      error "Firestore import in app module file: $f"
    fi
  fi

  # core:domain must not import Firestore or Auth
  if echo "$f" | grep -q '/core/domain/'; then
    if grep -qE 'com\.google\.firebase\.(firestore|auth)' "$f"; then
      error "Firebase import in core:domain: $f"
    fi
  fi

  # core:designsystem must not import from data or feature
  if echo "$f" | grep -q '/core/designsystem/'; then
    if grep -qE 'com\.maxot\.seekandcatch\.(data|feature)\.' "$f"; then
      error "data/feature import in core:designsystem: $f"
    fi
  fi
done < "$TMPFILE"

# ─────────────────────────────────────────────────────────────────────────────
# Rule 2: Firebase Auth must only appear in FirebaseAuthDataSource
# ─────────────────────────────────────────────────────────────────────────────
while IFS= read -r f; do
  if grep -q 'com\.google\.firebase\.auth' "$f" 2>/dev/null; then
    if ! echo "$f" | grep -q 'FirebaseAuthDataSource\.kt'; then
      error "Firebase Auth import outside FirebaseAuthDataSource: $f"
    fi
  fi
done < "$TMPFILE"

# ─────────────────────────────────────────────────────────────────────────────
# Rule 3: Screen composables must not import repository interfaces/impls
# ─────────────────────────────────────────────────────────────────────────────
while IFS= read -r f; do
  if echo "$f" | grep -q '/feature/' && echo "$f" | grep -qE 'Screen\.kt$'; then
    if grep -qE 'import com\.maxot\.seekandcatch\.data\.repository\.' "$f"; then
      error "Repository import in Screen composable: $f"
    fi
  fi
done < "$TMPFILE"

# ─────────────────────────────────────────────────────────────────────────────
# Rule 4: Naming conventions
# Files ending in ViewModel.kt must declare a class ending in ViewModel
# Files ending in Repository.kt must declare an interface or class ending in Repository
# ─────────────────────────────────────────────────────────────────────────────
while IFS= read -r f; do
  filename=$(basename "$f")

  if [[ "$filename" == *ViewModel.kt ]]; then
    base="${filename%.kt}"
    if ! grep -qE "^(class|abstract class|open class) ${base}" "$f"; then
      error "File named ${filename} does not declare class/open class ${base}: $f"
    fi
  fi

  if [[ "$filename" == *Repository.kt ]] && ! echo "$filename" | grep -q "Impl"; then
    base="${filename%.kt}"
    if ! grep -qE "^(interface|class|abstract class) ${base}" "$f"; then
      error "File named ${filename} does not declare interface/class ${base}: $f"
    fi
  fi
done < "$TMPFILE"

# ─────────────────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────────────────
if [[ $VIOLATIONS -gt 0 ]]; then
  echo ""
  echo "check-arch: $VIOLATIONS violation(s) found. Fix before committing."
  exit 1
else
  echo "check-arch: all checks passed."
  exit 0
fi
