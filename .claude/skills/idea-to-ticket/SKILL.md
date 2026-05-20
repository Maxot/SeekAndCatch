Turn a raw idea into a draft ticket, with a doc impact analysis. Usage: `/idea-to-ticket "<idea>"`

1. Run the analysis script:
   ```bash
   bash .claude/skills/idea-to-ticket/run.sh "<idea>"
   ```
2. Read the script output carefully — it prints relevant sections from all four docs.
3. Add your own judgment on each layer:
   - **CONCEPT**: Does this violate any of the 7 core principles? Especially check principle 1 (wrong tap = instant death) and principle 6 (no monetisation).
   - **PRD**: Is this already in scope, explicitly out of scope (Section 13), or a new addition?
   - **TECH_SPEC**: Would this require a new module, use case, or Firestore collection? Would it violate any boundary rule?
   - **DESIGN_SYSTEM**: Does it need a new Composable? Does it respect the pixel-art aesthetic and Press Start 2P typography?
4. Show the user the generated draft ticket path.
5. Tell the user which documents need to be updated before implementation can begin.
6. Do not start implementing until the user confirms the draft ticket and any required doc updates.
