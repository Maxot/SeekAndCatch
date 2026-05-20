#!/bin/bash
# Start a ticket: verify dependencies, print context and acceptance criteria.
# Usage: start.sh <ticket-id>
# Example: start.sh 001

set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
TICKETS_DIR="$REPO_ROOT/tickets"

if [[ $# -lt 1 ]]; then
  echo "Usage: start.sh <ticket-id>"
  echo "  Example: start.sh 001"
  exit 1
fi

TICKET_ID="$1"

# Find ticket file (prefix match)
TICKET_FILE=$(find "$TICKETS_DIR" -name "${TICKET_ID}*.md" | head -1)
if [[ -z "$TICKET_FILE" ]]; then
  echo "Error: no ticket file found matching '${TICKET_ID}' in $TICKETS_DIR"
  exit 1
fi

echo "═══════════════════════════════════════════════════════════"
echo " Starting ticket: $(basename "$TICKET_FILE")"
echo "═══════════════════════════════════════════════════════════"
echo ""

# ── Dependency check ──────────────────────────────────────────
DEPENDS_LINE=$(grep -i "^\\*\\*Depends on:\\*\\*" "$TICKET_FILE" || true)
if [[ -n "$DEPENDS_LINE" ]]; then
  DEPENDS="${DEPENDS_LINE#*\*\* }"
  DEPENDS="${DEPENDS/\*\*/}"
  DEPENDS="${DEPENDS//  / }"
  DEPENDS="$(echo "$DEPENDS" | xargs)"
  if [[ -n "$DEPENDS" && "$DEPENDS" != "(none)" && "$DEPENDS" != "none" ]]; then
    echo "Checking dependencies: $DEPENDS"
    IFS=',' read -ra DEP_IDS <<< "$DEPENDS"
    BLOCKED=0
    for dep in "${DEP_IDS[@]}"; do
      dep="$(echo "$dep" | xargs)"
      DEP_FILE=$(find "$TICKETS_DIR" -name "${dep}*.md" | head -1)
      if [[ -z "$DEP_FILE" ]]; then
        echo "  ⚠ Dependency ticket '${dep}' not found."
        BLOCKED=$((BLOCKED + 1))
      else
        STATUS=$(grep -i "^\\*\\*Status:\\*\\*" "$DEP_FILE" | sed 's/.*\*\* //' | sed 's/\*\*//' | xargs || true)
        if [[ "$STATUS" != "Done" ]]; then
          echo "  ✗ Ticket ${dep} is BLOCKING (Status: ${STATUS:-unknown})"
          BLOCKED=$((BLOCKED + 1))
        else
          echo "  ✓ Ticket ${dep} is Done"
        fi
      fi
    done
    if [[ $BLOCKED -gt 0 ]]; then
      echo ""
      echo "Cannot start: $BLOCKED blocking dependency/dependencies must be Done first."
      exit 1
    fi
    echo ""
  fi
fi

# ── Print ticket content sections ─────────────────────────────
print_section() {
  local heading="$1"
  local content
  content=$(awk "/^## ${heading}/,/^## [A-Z]/" "$TICKET_FILE" | grep -v "^## [A-Z]" | tail -n +2 || true)
  if [[ -n "$content" ]]; then
    echo "── ${heading} ──────────────────────────────────────────────"
    echo "$content"
    echo ""
  fi
}

print_section "Context"
print_section "Task"
print_section "Acceptance Criteria"
print_section "Files to Create"
print_section "Files to Modify"
print_section "Notes"

echo "═══════════════════════════════════════════════════════════"
echo " Reminder: run .claude/skills/check-arch/check.sh before"
echo " marking this ticket Done."
echo "═══════════════════════════════════════════════════════════"

exit 0
