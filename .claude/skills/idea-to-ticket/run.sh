#!/bin/bash
# Checks a raw idea against all doc layers and generates a draft ticket.
# Usage: run.sh "<idea>"
# Example: run.sh "show session duration on the result screen"

set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
DOCS="$REPO_ROOT/docs"
TICKETS="$REPO_ROOT/tickets"

if [[ $# -lt 1 ]]; then
  echo "Usage: run.sh \"<idea>\""
  echo "  Example: run.sh \"show session duration on the result screen\""
  exit 1
fi

IDEA="$1"
ISSUES=0

divider() { echo ""; echo "────────────────────────────────────────────────────────────"; echo ""; }

echo "═══ IDEA-TO-TICKET ANALYSIS ═══════════════════════════════"
echo "Idea: \"$IDEA\""
echo ""

# ── 1. CONCEPT.md check ──────────────────────────────────────
echo "▶ 1. Checking CONCEPT.md..."
divider

OUT_OF_SCOPE=$(grep -A 20 "^## Key Risks" "$DOCS/CONCEPT.md" | head -20 || true)
echo "Core principles:"
grep -A 20 "^## Core Principles" "$DOCS/CONCEPT.md" | grep "^[0-9]" | head -10
echo ""
echo "Out-of-scope / Roadmap phases:"
grep -A 15 "^## Roadmap" "$DOCS/CONCEPT.md" | head -18
echo ""
echo "⚑ Manually verify: does this idea conflict with any core principle?"
echo "  If it changes economic model, target audience, or aesthetic — update CONCEPT.md first."
divider

# ── 2. PRD check ─────────────────────────────────────────────
echo "▶ 2. Checking PRD.md..."
divider
echo "Current screen inventory:"
grep "^### 10\." "$DOCS/PRD.md" | head -15
echo ""
echo "Out of scope (Section 13):"
grep -A 15 "^## 13\. Out of Scope" "$DOCS/PRD.md" | grep "^-" | head -15
echo ""
echo "⚑ Manually verify:"
echo "  - Is this idea within MVP scope, or is it explicitly out of scope?"
echo "  - Does it add or modify a screen listed in Section 10?"
echo "  - Does it require a new entity or Firestore field?"
divider

# ── 3. TECH_SPEC check ───────────────────────────────────────
echo "▶ 3. Checking TECH_SPEC.md..."
divider
echo "Existing repository interfaces in data/:"
find "$REPO_ROOT/data/src/main" -name "*Repository.kt" ! -name "*Impl.kt" -exec basename {} .kt \; | sort 2>/dev/null || true
echo ""
echo "Existing feature modules:"
ls "$REPO_ROOT/feature/" 2>/dev/null || true
echo ""
echo "⚑ Manually verify:"
echo "  - Does this need a new repository / datasource / Firestore collection?"
echo "  - Does this change a game formula or state machine?"
echo "  - Does this add a new module dependency?"
divider

# ── 4. DESIGN_SYSTEM check ───────────────────────────────────
echo "▶ 4. Checking DESIGN_SYSTEM.md..."
divider
echo "Existing components:"
grep "^### " "$DOCS/DESIGN_SYSTEM.md" | head -15
echo ""
echo "⚑ Manually verify:"
echo "  - Does this require a new Composable in core:designsystem?"
echo "  - Does any copy violate the tone-of-voice rules?"
echo "  - Does any colour need to be named as a new token?"
divider

# ── 5. Generate draft ticket ──────────────────────────────────
echo "▶ 5. Generating draft ticket..."
divider

LAST_ID=$(find "$TICKETS" -name "[0-9]*.md" | sort | tail -1 | xargs basename 2>/dev/null | sed 's/[^0-9].*//' || echo "000")
NEXT_ID=$(printf "%03d" $((10#$LAST_ID + 1)))
SLUG=$(echo "$IDEA" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd 'a-z0-9-' | cut -c1-40)
TICKET_FILE="$TICKETS/${NEXT_ID}-${SLUG}.md"

TITLE="$(echo "$IDEA" | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) tolower(substr($i,2)); print}')"

cat > "$TICKET_FILE" << TMPL
# Ticket ${NEXT_ID} — ${TITLE}

**Status:** Draft
**Depends on:** (none)

---

## Context

<!-- Generated from idea: "$IDEA" -->
<!-- Why this work is needed. Reference PRD section or user request. -->

---

## Task

<!-- What exactly needs to be implemented. Specific class names, Composable names,
     repository method signatures, Firestore paths, file paths.
     Reference PRD and TECH_SPEC where relevant. -->

---

## Acceptance Criteria

- [ ] [Specific, testable criterion]
- [ ] Project builds without errors (\`./gradlew build\`)
- [ ] No Firestore imports in ViewModel or UI layer
- [ ] No business logic in Composables
- [ ] All new screen files have at least one \`@Preview\`
- [ ] Unit tests exist for all ViewModel logic added or changed by this ticket

---

## Files to Create

<!-- - \`path/to/NewFile.kt\` — description -->

## Files to Modify

<!-- - \`path/to/ExistingFile.kt\` — describe what changes -->

---

## Notes

<!-- Doc updates required before implementation: -->
<!-- - PRD.md: [describe changes needed] -->
<!-- - TECH_SPEC.md: [describe changes needed] -->
<!-- - DESIGN_SYSTEM.md: [describe changes needed] -->
TMPL

echo "Draft ticket: $TICKET_FILE"
echo ""
echo "Before starting implementation:"
echo "  1. Complete the Task and Acceptance Criteria sections"
echo "  2. Update any docs flagged above"
echo "  3. Run: .claude/skills/start-ticket/start.sh ${NEXT_ID}"

exit 0
