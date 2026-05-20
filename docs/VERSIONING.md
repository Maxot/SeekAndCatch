# doc/ — Versioning Quick Reference

## Version numbers

`major.minor` — bump **major** for significant rewrites or structural changes; bump **minor** for additions, corrections, or expansions.

| Change | Example |
|---|---|
| Full rewrite, new section structure | v1.0 → v2.0 |
| New section or substantial addition | v1.0 → v1.1 |
| Corrections, wording, small updates | v1.2 → v1.3 |

## Status values

| Status | Meaning |
|---|---|
| `Draft` | Work in progress, not yet authoritative |
| `Active` | Current authoritative version |
| `Superseded` | Replaced by a newer major version; kept for reference |

## Commit message format

```
docs(FILENAME): brief description

- what changed
- rationale if not obvious

Bumps FILENAME from vX.Y to vX.Z
```

Example:

```
docs(CONCEPT): add Bridge mechanic and economic flywheel sections

- Added "Bridge from personal to philosophical" with full flow description
- Added economic flywheel and expertise-as-unit sections

Bumps CONCEPT from v1.0 to v2.0
```

## Tagging major versions

Tag after committing a major bump:

```bash
git tag -a docs/CONCEPT-v2.0 -m "CONCEPT: full product concept v2 with flywheel and expertise model"
git tag -a docs/TECH_SPEC-v2.0 -m "TECH_SPEC: major restructure for feature module architecture"
```

Use the `docs/` prefix to keep doc tags visually separate from code tags.

## Useful git commands

```bash
# Full history of one doc
git log --oneline docs/CONCEPT.md

# Diff between two commits
git diff abc1234 def5678 -- docs/CONCEPT.md

# Diff between two tags
git diff docs/CONCEPT-v1.0 docs/CONCEPT-v2.0 -- docs/CONCEPT.md

# Read a doc at a specific tag
git show docs/CONCEPT-v1.0:docs/CONCEPT.md

# Restore a doc to a previous state (then commit the revert)
git checkout docs/CONCEPT-v1.0 -- docs/CONCEPT.md
```