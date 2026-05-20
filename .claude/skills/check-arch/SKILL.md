Run the architecture boundary check against the current codebase (or staged files only with `--staged`).

Execute the check script:

```bash
bash .claude/skills/check-arch/check.sh
```

If violations are found, show each one to the user and explain what boundary rule it violates, referencing the relevant rule number from `docs/TECH_SPEC.md` Section 21. Do not proceed with any commit or ticket completion until the script exits 0.

If the script exits 0, confirm "check-arch: all checks passed" and continue.
