# Ticket 007 — Add Unit Tests for Feature ViewModels

**Status:** Done
**Depends on:** 003 (LeaderboardViewModel tests are in ticket 003), 005 (AccountViewModel tests are in ticket 005)

---

## Context

Seven feature ViewModels have zero unit tests. The game engines (`FlowGameEngine`, `FlashGameEngine`) have good coverage, but the ViewModels that wire them to the UI do not. This means ViewModel state transitions, scoring logic paths, and repository call sequences are only tested manually.

This ticket covers the four remaining ViewModels not addressed by tickets 003 and 005:
- `GameSelectionViewModel`
- `GameResultViewModel`
- `FlowGameViewModel`
- `FlashGameViewModel`
- `SettingsViewModel`

Reference: CLAUDE.md Definition of Done — "Unit tests exist for all ViewModel logic added or changed by this ticket."

---

## Task

Create a test file for each ViewModel listed below. Use `kotlinx-coroutines-test` with `StandardTestDispatcher` and `Turbine` (if available in the project) or manual `StateFlow` collection. Use fake/mock repositories — check `data-test` module for existing fakes, otherwise create minimal fakes inline.

### `GameSelectionViewModelTest`
- Initial state exposes available game modes.
- Selecting a mode updates selected state.

### `GameResultViewModelTest`
- Score is correctly derived from the game result passed to the ViewModel.
- `isHighScore` is `true` when current score > best in leaderboard for same mode+difficulty.
- `isHighScore` is `false` when score is not a new best.
- Leaderboard `addRecord()` is called when `isHighScore` is `true`.
- Leaderboard `addRecord()` is NOT called when `isHighScore` is `false`.
- Repository error during score submission does not crash the ViewModel.

### `FlowGameViewModelTest`
- Game starts in the correct initial state.
- A correct tap increments the score.
- A wrong tap transitions to game-over state.
- Pause/resume transitions game state correctly.

### `FlashGameViewModelTest`
- Same structure as `FlowGameViewModelTest` adapted for Flash mode rules.
- A missed item (timeout) transitions to game-over state.

### `SettingsViewModelTest`
- Toggling sound saves to repository.
- Toggling music saves to repository.
- Toggling vibration saves to repository.
- Initial state reads from repository.

---

## Acceptance Criteria

- [ ] Test files created for all 5 ViewModels listed above.
- [ ] Each test class has at least 3 test cases covering the happy path and one error/edge case.
- [ ] Tests use `@OptIn(ExperimentalCoroutinesApi::class)` and `StandardTestDispatcher` (or `UnconfinedTestDispatcher`).
- [ ] All tests pass (`./gradlew test`).
- [ ] No production code changes needed.
- [ ] `check-arch` exits 0.

---

## Files to Create

- `feature/gameplay/src/test/java/.../feature/gameplay/GameSelectionViewModelTest.kt`
- `feature/gameplay/src/test/java/.../feature/gameplay/GameResultViewModelTest.kt`
- `feature/gameplay/src/test/java/.../feature/gameplay/FlowGameViewModelTest.kt`
- `feature/gameplay/src/test/java/.../feature/gameplay/FlashGameViewModelTest.kt`
- `feature/settings/src/test/java/.../feature/settings/SettingsViewModelTest.kt`

---

## Notes

Read the existing `FiguresRepositoryImplTest` and engine tests in `core/domain/test` to understand the test patterns already established in this project before writing new tests.

`GameResultViewModel` injects `LeaderboardRepository` — create a `FakeLeaderboardRepository` in the test source set if one doesn't already exist. Do not put fakes in production source sets.
