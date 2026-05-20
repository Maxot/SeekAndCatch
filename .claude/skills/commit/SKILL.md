Commit staged changes with architecture check and conventional commit format. Usage: `/commit <type> <ticket-id|-> "<description>"`

Valid types: `feat` | `fix` | `refactor` | `docs` | `test` | `chore`

`feat` and `fix` commits must reference a ticket: `feat: ticket 3 — description`
All other types may omit the ticket: `chore: update gitignore`

1. Run the commit script (it runs check-arch internally on staged files):
   ```bash
   bash .claude/skills/commit/commit.sh <type> <ticket-id|-> "<description>"
   ```
2. If check-arch fails, show the violations and fix them before retrying.
3. If the commit succeeds, confirm the commit message to the user.

Do not skip check-arch. Do not use `--no-verify`.
