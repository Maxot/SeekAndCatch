# Ticket 001 — Fix collectAsState() → collectAsStateWithLifecycle() in All Screens

**Status:** Draft
**Depends on:** (none)

---

## Context

Three production Composables use `collectAsState()` instead of `collectAsStateWithLifecycle()`. The project rule (CLAUDE.md §9) prohibits `collectAsState()` because it keeps the Flow active even when the app is below STARTED lifecycle state, wasting resources and keeping game audio/logic running in the background.

All other screens already use `collectAsStateWithLifecycle()` correctly. These are the remaining violations:

1. `app/src/main/java/com/maxot/seekandcatch/MainActivity.kt:40` — `viewModel.uiState.collectAsState()`
2. `feature/settings/.../ui/SettingsDialog.kt:52–58` — 5 calls across `soundState`, `musicState`, `vibrationState`, `darkTheme`, `colorblindMode`
3. `feature/gameplay/.../gameselection/GameSelectionScreen.kt:61` — `viewModel.uiState.collectAsState()`

Reference: CLAUDE.md "What Never To Do" rule 9; TECH_SPEC §18.

---

## Task

For each of the three files:

1. Remove `import androidx.compose.runtime.collectAsState`
2. Add `import androidx.lifecycle.compose.collectAsStateWithLifecycle`
3. Replace every `.collectAsState()` call with `.collectAsStateWithLifecycle()`

**`MainActivity.kt` change:**
```kotlin
// Before
val uiState = viewModel.uiState.collectAsState()
// After
val uiState = viewModel.uiState.collectAsStateWithLifecycle()
```

**`SettingsDialog.kt` changes (lines 52–58):**
```kotlin
// Before
val isSoundEnabled by viewModel.soundState.collectAsState(true)
val isMusicEnabled by viewModel.musicState.collectAsState(true)
val isVibrationEnabled by viewModel.vibrationState.collectAsState(true)
val darkTheme by viewModel.darkTheme.collectAsState()
val isColorblindModeEnabled by viewModel.colorblindMode.collectAsState(false)
// After — same but collectAsStateWithLifecycle()
```

**`GameSelectionScreen.kt` change:**
```kotlin
// Before
val uiState by viewModel.uiState.collectAsState()
// After
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

Verify `lifecycle-runtime-compose` is listed as a dependency in each affected module's `build.gradle.kts`. It is already present in the gameplay module; confirm for `app` and `settings`.

---

## Acceptance Criteria

- [ ] No `import androidx.compose.runtime.collectAsState` remains in any production Composable.
- [ ] All three files use `collectAsStateWithLifecycle()`.
- [ ] `lifecycle-runtime-compose` dependency present in `app/build.gradle.kts` and `feature/settings/build.gradle.kts`.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] `check-arch` exits 0.
- [ ] No unit tests needed (pure import/call swap, no logic change).

---

## Files to Modify

- `app/src/main/java/com/maxot/seekandcatch/MainActivity.kt` — swap import and usage
- `feature/settings/src/main/java/com/maxot/seekandcatch/feature/settings/ui/SettingsDialog.kt` — swap import and 5 usages
- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/gameselection/GameSelectionScreen.kt` — swap import and usage
- `app/build.gradle.kts` — add `lifecycle-runtime-compose` if missing
- `feature/settings/build.gradle.kts` — add `lifecycle-runtime-compose` if missing
