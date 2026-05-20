#!/bin/bash
# Walks a concept through the document hierarchy and generates draft ticket(s).
# Usage: run.sh "<concept>" --phase <1|2|3|4|5>
# Example: run.sh "add player statistics screen" --phase 1

set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
DOCS="$REPO_ROOT/docs"
TICKETS="$REPO_ROOT/tickets"

if [[ $# -lt 3 ]]; then
  echo "Usage: run.sh \"<concept>\" --phase <1|2|3|4|5>"
  echo ""
  echo "Phases:"
  echo "  1 — CONCEPT.md: does this fit product principles?"
  echo "  2 — PRD.md: what user flows or entities are affected?"
  echo "  3 — TECH_SPEC.md: what architectural or Firestore changes are needed?"
  echo "  4 — DESIGN_SYSTEM.md: what new Composables or patterns are needed?"
  echo "  5 — Generate draft ticket(s)"
  exit 1
fi

CONCEPT_TEXT="$1"
shift
PHASE=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --phase) PHASE="$2"; shift 2 ;;
    *) echo "Unknown argument: $1"; exit 1 ;;
  esac
done

if [[ -z "$PHASE" ]]; then
  echo "Error: --phase is required"
  exit 1
fi

divider() { echo ""; echo "────────────────────────────────────────────────────────────"; echo ""; }

case "$PHASE" in
  1)
    echo "═══ PHASE 1: CONCEPT.md ANALYSIS ═══════════════════════════"
    echo "Concept: \"$CONCEPT_TEXT\""
    divider
    echo "Checking against core principles in docs/CONCEPT.md..."
    echo ""
    grep -A 20 "^## Core Principles" "$DOCS/CONCEPT.md" | head -25
    divider
    echo "Out-of-scope items:"
    grep -A 10 "^## Roadmap" "$DOCS/CONCEPT.md" | head -15
    divider
    echo "Questions to answer before Phase 2:"
    echo "  1. Does this concept conflict with any numbered core principle?"
    echo "  2. Is this in Phase 1 (MVP), Phase 2, or Phase 3?"
    echo "  3. Does it require a CONCEPT.md update?"
    echo ""
    echo "Confirm or resolve, then run: run.sh \"$CONCEPT_TEXT\" --phase 2"
    ;;

  2)
    echo "═══ PHASE 2: PRD ANALYSIS ═══════════════════════════════════"
    echo "Concept: \"$CONCEPT_TEXT\""
    divider
    echo "Relevant PRD sections:"
    grep -n "^## " "$DOCS/PRD.md"
    divider
    echo "Current entities (Section 4):"
    grep -A 40 "^## 4\. Entities" "$DOCS/PRD.md" | head -45
    divider
    echo "Current screen inventory (Section 10):"
    grep -A 40 "^## 10\. Screens" "$DOCS/PRD.md" | head -45
    divider
    echo "Questions to answer before Phase 3:"
    echo "  1. What new user flows does this add or modify?"
    echo "  2. What new entities or fields are required?"
    echo "  3. What screens are added or modified?"
    echo "  4. Does the PRD out-of-scope list (Section 13) need updating?"
    echo ""
    echo "Update PRD.md if needed, then run: run.sh \"$CONCEPT_TEXT\" --phase 3"
    ;;

  3)
    echo "═══ PHASE 3: TECH_SPEC ANALYSIS ══════════════════════════════"
    echo "Concept: \"$CONCEPT_TEXT\""
    divider
    echo "Module structure (Section 3):"
    grep -A 30 "^## 3\. Project" "$DOCS/TECH_SPEC.md" | head -35
    divider
    echo "Firestore data model (Section 15):"
    grep -A 30 "^## 15\. Data Model" "$DOCS/TECH_SPEC.md" | head -35
    divider
    echo "Architecture boundary rules (Section 21):"
    grep -A 20 "^## 21\. Architecture" "$DOCS/TECH_SPEC.md" | head -25
    divider
    echo "Questions to answer before Phase 4:"
    echo "  1. Does this require a new Firestore collection? (use firestore-collection skill)"
    echo "  2. Does this require a new feature module? (use new-feature skill)"
    echo "  3. Does this require a new UseCase or Engine?"
    echo "  4. Does this change any game state machine or formula?"
    echo "  5. Does TECH_SPEC.md need updating?"
    echo ""
    echo "Update TECH_SPEC.md if needed, then run: run.sh \"$CONCEPT_TEXT\" --phase 4"
    ;;

  4)
    echo "═══ PHASE 4: DESIGN SYSTEM ANALYSIS ════════════════════════"
    echo "Concept: \"$CONCEPT_TEXT\""
    divider
    echo "Existing component catalog (Section 6):"
    grep -A 50 "^## 6\. Component Catalog" "$DOCS/DESIGN_SYSTEM.md" | head -55
    divider
    echo "Forbidden visual patterns (Section 11):"
    grep -A 15 "^## 11\. Forbidden" "$DOCS/DESIGN_SYSTEM.md" | head -20
    divider
    echo "Questions to answer before Phase 5:"
    echo "  1. What new Composables are needed?"
    echo "  2. Do any new colour tokens need to be named?"
    echo "  3. Does this use the pixel border aesthetic consistently?"
    echo "  4. Are all new text strings consistent with the tone of voice rules?"
    echo "  5. Does DESIGN_SYSTEM.md need updating?"
    echo ""
    echo "Update DESIGN_SYSTEM.md if needed, then run: run.sh \"$CONCEPT_TEXT\" --phase 5"
    ;;

  5)
    echo "═══ PHASE 5: GENERATING DRAFT TICKET(S) ════════════════════"
    echo "Concept: \"$CONCEPT_TEXT\""
    divider

    # Find next ticket ID
    LAST_ID=$(find "$TICKETS" -name "[0-9]*.md" | sort | tail -1 | xargs basename | sed 's/[^0-9].*//' 2>/dev/null || echo "000")
    NEXT_ID=$(printf "%03d" $((10#$LAST_ID + 1)))
    SLUG=$(echo "$CONCEPT_TEXT" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd 'a-z0-9-' | cut -c1-40)
    TICKET_FILE="$TICKETS/${NEXT_ID}-${SLUG}.md"

    cat > "$TICKET_FILE" << TMPL
# Ticket ${NEXT_ID} — $(echo "$CONCEPT_TEXT" | sed 's/\b./\u&/g')

**Status:** Draft
**Depends on:** (none)

---

## Context

<!-- Why this work is needed. Generated from concept: "$CONCEPT_TEXT" -->
<!-- Reference PRD section, prior ticket, or user request. -->

---

## Task

<!-- What exactly needs to be implemented. Specific class names, Composable names,
     repository method signatures, Firestore collection/document paths, file paths.
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

<!-- Design decisions, Firestore schema considerations, edge cases, constraints,
     references to spec sections. -->
TMPL

    echo "Draft ticket created: $TICKET_FILE"
    echo ""
    echo "Fill in the Task, Acceptance Criteria, and Files sections before starting work."
    ;;

  *)
    echo "Error: unknown phase '$PHASE'. Valid phases: 1 2 3 4 5"
    exit 1
    ;;
esac

exit 0
