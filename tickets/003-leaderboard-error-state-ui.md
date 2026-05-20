# Ticket 003 — Add Error UI to Leaderboard Failed State

**Status:** Done
**Depends on:** (none)

---

## Context

`LeaderboardUiState` is a sealed interface with three states: `Loading`, `Successful`, and `Failed`. The `Loading` state already shows a `CircularProgressIndicator`. The `Successful` state renders the list. But the `Failed` branch at `LeaderBoardScreen.kt:122` is an empty block — when Firestore fails, users see a completely blank screen with no message or recovery path.

Additionally, there is no unit test for `LeaderboardViewModel` verifying that a Firestore error produces the `Failed` state.

Reference: TECH_SPEC §19 Leaderboard; DESIGN_SYSTEM §10 Interaction Patterns.

---

## Task

1. **`LeaderBoardScreen.kt`** — add UI for the `Failed` branch:
   ```kotlin
   is LeaderboardUiState.Failed -> {
       Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
           Text(
               text = stringResource(id = R.string.feature_leaderboard_error_state),
               style = MaterialTheme.typography.bodyMedium,
               color = MaterialTheme.colorScheme.onBackground,
               textAlign = TextAlign.Center
           )
       }
   }
   ```

2. **`strings.xml`** (and `strings-uk.xml` if Ukrainian translations exist) — add:
   ```xml
   <string name="feature_leaderboard_error_state">Could not load scores.\nCheck your connection and try again.</string>
   ```

3. **`LeaderboardViewModel.kt`** — verify the Firestore error path actually emits `Failed`. The `.snapshots()` Flow emits a Flow error on network failure. The ViewModel must catch it:
   ```kotlin
   .catch { emit(LeaderboardUiState.Failed) }
   ```
   If this `catch` is already present, confirm it; if not, add it.

4. **Add `@Preview`** for the `Failed` state in `LeaderBoardScreen.kt`:
   ```kotlin
   @Preview
   private fun LeaderBoardScreenFailedPreview() {
       LeaderBoardScreenContent(leaderboardUiState = LeaderboardUiState.Failed)
   }
   ```

5. **`LeaderboardViewModelTest.kt`** — create a unit test verifying that when the repository throws, the UI state becomes `Failed`.

---

## Acceptance Criteria

- [ ] `LeaderBoardScreen` shows a non-blank error message when `LeaderboardUiState.Failed`.
- [ ] Error string is in `strings.xml`, not hardcoded.
- [ ] `LeaderboardViewModel` has a `catch` block that emits `Failed` on Firestore error.
- [ ] `@Preview` function exists for the `Failed` state.
- [ ] Unit test in `LeaderboardViewModelTest` covers the error → `Failed` transition.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] `check-arch` exits 0.

---

## Files to Modify

- `feature/leaderboard/src/main/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardScreen.kt` — add `Failed` branch UI and preview
- `feature/leaderboard/src/main/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardViewModel.kt` — verify/add `.catch { emit(Failed) }`
- `feature/leaderboard/src/main/res/values/strings.xml` — add error string
- `feature/leaderboard/src/main/res/values-uk/strings.xml` — add Ukrainian translation if file exists

## Files to Create

- `feature/leaderboard/src/test/java/com/maxot/seekandcatch/feature/leaderboard/LeaderboardViewModelTest.kt`

---

## Notes

Do not add a "retry" button — out of scope for MVP per PRD §13.
`LeaderboardUiState` itself does not need to change — the sealed class structure is correct.
