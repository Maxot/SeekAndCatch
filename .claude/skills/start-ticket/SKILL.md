Start a ticket session. Usage: `/start-ticket <ticket-id>`

1. Run the start script:
   ```bash
   bash .claude/skills/start-ticket/start.sh <ticket-id>
   ```
2. Read the full ticket file at `tickets/<id>*.md` yourself.
3. Read every file listed in "Files to create/modify".
4. Read the relevant sections of `docs/TECH_SPEC.md` and `docs/PRD.md` that the ticket references.
5. Confirm you understand the task, then ask the user if they want you to begin implementation or if they want to discuss the approach first.

Do not start writing code before completing steps 2–4.
