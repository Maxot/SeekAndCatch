#!/bin/bash
# Commit with architecture check and conventional commit format.
# Usage: commit.sh <type> <ticket-id|-> "<description>"
# Example: commit.sh feat 003 "add leaderboard offline fallback"
#          commit.sh chore - "update gitignore"

set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
CHECK_ARCH="$REPO_ROOT/.claude/skills/check-arch/check.sh"

VALID_TYPES="feat fix refactor docs test chore"

if [[ $# -lt 3 ]]; then
  echo "Usage: commit.sh <type> <ticket-id|-> \"<description>\""
  echo "  type: feat | fix | refactor | docs | test | chore"
  echo "  ticket-id: numeric ticket ID (e.g. 003) or - for no ticket"
  echo "  description: short description of the change"
  exit 1
fi

TYPE="$1"
TICKET="$2"
DESCRIPTION="$3"

# Validate type
if ! echo "$VALID_TYPES" | grep -qw "$TYPE"; then
  echo "Error: invalid type '$TYPE'. Valid types: $VALID_TYPES"
  exit 1
fi

# Validate description
if [[ -z "$DESCRIPTION" ]]; then
  echo "Error: description cannot be empty"
  exit 1
fi

# Run architecture check on staged files
echo "Running check-arch on staged files..."
if ! bash "$CHECK_ARCH" --staged; then
  echo ""
  echo "Commit aborted: fix architecture violations first."
  exit 1
fi
echo ""

# Build commit message
if [[ "$TICKET" == "-" ]]; then
  SUBJECT="${TYPE}: ${DESCRIPTION}"
else
  # Zero-pad ticket number if needed
  TICKET_NUM=$(echo "$TICKET" | sed 's/^0*//')
  SUBJECT="${TYPE}: ticket ${TICKET_NUM} — ${DESCRIPTION}"
fi

# Commit
git -C "$REPO_ROOT" commit -m "$(cat <<EOF
${SUBJECT}

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"

echo ""
echo "Committed: $SUBJECT"
exit 0
