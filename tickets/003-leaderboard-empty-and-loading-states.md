# Ticket 003 — Leaderboard Empty and Loading States

**Status:** Draft
**Depends on:** (none)

---

## Context

The PRD (Section 10.5) and TECH_SPEC §19 specify that leaderboard failures must never crash the app and must degrade gracefully. The existing `LeaderBoardScreen` uses a `LazyColumn` to display records, but there is no defined UI for:

1. **Loading state** — while Firestore is fetching records, there is no indicator (user sees a blank screen).
2. **Empty state** — if the leaderboard has no records matching the active filter, the list is empty with no user feedback.
3. **Offline/error state** — if Firestore fails entirely, the screen shows nothing without explanation.

The `LeaderboardUiState` and `LeaderboardViewModel` need to be verified and, if missing, extended with a `isLoading: Boolean` and an optional `errorMessage: String?` field, and the screen needs corresponding UI handling for these states.

Reference: PRD §10.5; TECH_SPEC §19 Leaderboard; DESIGN_SYSTEM §10 Interaction Patterns (Loading, Error, Empty).

---

## Task

1. **Audit `LeaderboardUiState.kt`** — verify whether it already has `isLoading` and error state fields. Add if missing:
   ```kotlin
   data class LeaderboardUiState(
       val isLoading: Boolean = true,
       val records: List<LeaderboardRecord> = emptyList(),
       val selectedMode: GameMode = GameMode.FLOW,
       val selectedDifficulty: GameDifficulty = GameDifficulty.NORMAL,
       val errorMessage: String? = null,
   )
   ```

2. **Audit `LeaderboardViewModel.kt`** — ensure it:
   - Sets `isLoading = true` before Firestore starts emitting.
   - Sets `isLoading = false` once the first emission is received.
   - Catches Firestore errors and sets `errorMessage` (does not crash).

3. **Update `LeaderBoardScreen.kt`** to handle all three states:
   - `isLoading = true` → show `CircularProgressIndicator` centred on screen.
   - `records.isEmpty() && !isLoading` → show a text message: "No records yet" (using `MaterialTheme.typography.bodyMedium`, `MaterialTheme.colorScheme.onBackground`).
   - `errorMessage != null` → show the error text (e.g. "Could not load scores") without crashing.
   - Normal: existing `LazyColumn` list rendering.

4. Add `@Preview` for empty state and loading state in `LeaderBoardScreen.kt`.

5. All text strings should be added to the feature's `strings.xml` resource file — do not hardcode.

---

## Acceptance Criteria

- [ ] `LeaderBoardScreen` shows a `CircularProgressIndicator` while loading.
- [ ] `LeaderBoardScreen` shows a "No records yet" message when the filtered list is empty and not loading.
- [ ] `LeaderBoardScreen` shows an error message (not a crash) when Firestore fails.
- [ ] `LeaderboardUiState` has `isLoading: Boolean` and `errorMessage: String?` fields.
- [ ] `LeaderboardViewModel` sets `isLoading = false` after the first Firestore emission.
- [ ] No Firestore imports in `LeaderBoardScreen.kt` or `LeaderboardViewModel.kt`.
- [ ] New `@Preview` functions exist for empty and loading states.
- [ ] String resources added to `strings.xml`; no hardcoded strings in Composables.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] `check-arch` exits 0.
- [ ] Unit tests for `LeaderboardViewModel` cover: initial loading state, successful load sets `isLoading = false`, error sets `errorMessage`.

---

## Files to Modify

- `feature/leaderboard/src/main/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardUiState.kt` — add `isLoading`, `errorMessage`
- `feature/leaderboard/src/main/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardViewModel.kt` — set loading/error states correctly
- `feature/leaderboard/src/main/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardScreen.kt` — add empty/loading/error state UI and new `@Preview` functions
- `feature/leaderboard/src/main/res/values/strings.xml` — add new strings

## Files to Create

- `feature/leaderboard/src/test/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardViewModelTest.kt` — unit tests for loading and error states

---

## Notes

Use `FakeLeaderboardRepository` from `data-test` if it exists, or create a minimal fake inline in the test file.

The Firestore real-time listener in `LeaderboardFirestoreDataSource.observeRecords()` does not currently emit an error state — it uses `.snapshots().map { ... }` which will emit a Flow error on Firestore failure. `LeaderboardViewModel` must catch this with `catch { e -> ... }` on the collected Flow.

Do not add a "retry" button — the PRD does not require it and it adds complexity out of scope for this ticket.
