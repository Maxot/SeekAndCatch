# Ticket 001 — Fix collectAsState() → collectAsStateWithLifecycle() in GameSelectionScreen

**Status:** Draft
**Depends on:** (none)

---

## Context

`GameSelectionScreen.kt` uses `collectAsState()` to observe `viewModel.uiState`, while the established convention in this project (and the `CLAUDE.md` coding rules) requires `collectAsStateWithLifecycle()` for all production Composables. Using `collectAsState()` keeps the Flow active even when the lifecycle is below STARTED, wasting resources and potentially causing issues on background/foreground transitions.

Every other screen in the project (`LeaderBoardScreen`, `GameResultScreen`, `AccountScreen`) already uses `collectAsStateWithLifecycle()` correctly. This is the only remaining violation.

Reference: TECH_SPEC §18 Naming/Conventions; CLAUDE.md "Coding Conventions" — Flow Collection.

---

## Task

In `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/gameselection/GameSelectionScreen.kt`:

1. Replace the `collectAsState()` import with `collectAsStateWithLifecycle()`:
   - Remove: `import androidx.compose.runtime.collectAsState`
   - Add: `import androidx.lifecycle.compose.collectAsStateWithLifecycle`
2. Replace the usage on line ~62:
   - Before: `val uiState by viewModel.uiState.collectAsState()`
   - After: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`

No other changes needed.

---

## Acceptance Criteria

- [ ] `GameSelectionScreen.kt` uses `collectAsStateWithLifecycle()` instead of `collectAsState()`.
- [ ] No `import androidx.compose.runtime.collectAsState` remains in `GameSelectionScreen.kt`.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] `check-arch` exits 0.
- [ ] Existing `GameSelectionScreenPreview` still compiles and renders.
- [ ] No unit tests needed for this change (no logic modified).

---

## Files to Modify

- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/gameselection/GameSelectionScreen.kt` — swap import and usage

---

## Notes

This is a pure convention fix. No logic changes, no behavioural changes. The `collectAsStateWithLifecycle()` function is already available — `lifecycle-runtime-compose` is declared as a dependency in the gameplay feature's `build.gradle.kts`.
