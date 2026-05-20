Walk a large concept through the document hierarchy one phase at a time. Usage: `/concept-to-tickets "<concept>" --phase <1|2|3|4|5>`

Phases:
- **1** — CONCEPT.md: does this fit product principles?
- **2** — PRD.md: what user flows or entities are affected?
- **3** — TECH_SPEC.md: what architectural or Firestore changes are needed?
- **4** — DESIGN_SYSTEM.md: what new Composables or patterns are needed?
- **5** — Generate draft ticket(s)

1. Run the phase script:
   ```bash
   bash .claude/skills/concept-to-tickets/run.sh "<concept>" --phase <N>
   ```
2. Read the script output carefully.
3. Add your own analysis: flag any conflicts with existing principles, missing PRD coverage, or architectural implications not caught by the script.
4. Ask the user to confirm before proceeding to the next phase.
5. If a document needs updating, make the update before running the next phase.

Never advance to phase 5 (ticket generation) without the user confirming phases 1–4.
