<!-- Version: v1.2 · Last updated: 2026-05-19 · Status: Active / Source of Truth -->

# Technical Specification — Seek and Catch

**Derived from:** Codebase + PRD
**Note:** Architecture boundary rules are enforced by `.claude/skills/check-arch/check.sh`.
Commit message traceability (ticket reference on feat/fix commits) is enforced by `.claude/hooks/commit-msg`.
New feature modules are scaffolded by `.claude/skills/new-feature/scaffold.sh`.

---

## 1. Scope & Goals

This document defines authoritative technical architecture, gameplay rules, state transitions, scoring, health logic, leaderboard behaviour, and UX guarantees for SeekAndCatch. Anything not specified here must be considered undefined, must not be changed implicitly, and requires a spec update before implementation.

---

## 2. Tech Stack

| Dependency | Version |
|---|---|
| Kotlin | 1.9.22 |
| Android Gradle Plugin | 8.13.0 |
| Compose BOM | 2025.05.01 |
| Navigation Compose | 2.7.7 |
| Hilt | 2.51 |
| KSP | 1.9.22-1.0.18 |
| Firebase BOM | 33.0.0 |
| Kotlinx Coroutines | 1.8.0 |
| AndroidX DataStore | 1.1.1 |
| AndroidX Lifecycle | 2.7.0 |
| AndroidX Activity Compose | 1.9.0 |
| AndroidX Splashscreen | 1.0.1 |
| AndroidX Graphics Shapes | 1.0.0-beta01 |
| Hilt Navigation Compose | 1.2.0 |
| JUnit 4 | 4.13.2 |
| Mockito Kotlin | 5.1.0 |
| Mockito Core | 5.6.0 |

| SDK | Value |
|---|---|
| minSdk | 26 |
| compileSdk | 36 |
| targetSdk | 35 |
| JVM target | 21 |

---

## 3. Project / Module Structure

```
SeekAndCatch/
├── app/                        — Single-activity entry point; NavHost wiring; Application class
├── core/
│   ├── common/                 — Shared DI qualifiers, coroutine scopes, cross-module models
│   │                             (GameDifficulty, GameMode, GameParams, LeaderboardRecord, User,
│   │                              Figure, Goal)
│   ├── designsystem/           — SeekAndCatchTheme, color/type/shape tokens, reusable Composables
│   ├── domain/                 — Game use cases and engine (FlowGameUseCase, FlashGameUseCase,
│   │                             AuthUseCase, BaseGameEngine, FlowGameEngine, FlashGameEngine)
│   ├── media/                  — AudioManager, MusicManager, SoundManager, SettingsProvider
│   └── model/                  — UserConfig, DarkThemeConfig
├── data/                       — Repository implementations, DataStore, Firebase data sources
│   ├── repository/             — *RepositoryImpl classes, repository interfaces
│   ├── firebase/datasource/    — LeaderboardFirestoreDataSource, UserFirestoreDataSource,
│   │                             FirebaseAuthDataSource
│   ├── datastore/              — SettingsDataStore, AccountDataStore
│   └── di/                     — DataModule (Hilt bindings)
├── data-test/                  — Fake repository implementations for unit testing
├── feature/
│   ├── gameplay/               — GameSelectionScreen, FlowGameScreen, FlashGameScreen,
│   │                             GameResultScreen, all ViewModels, game layouts
│   ├── leaderboard/            — LeaderboardScreen, LeaderboardViewModel
│   ├── settings/               — SettingsDialog, SettingsViewModel, VibrationManager, SCLocaleManager
│   ├── account/                — AccountScreen, AccountViewModel
│   └── colorpicker/            — ColorPicker composable (custom UI)
└── singleselectionlazyrow/     — Custom library: single-selection horizontal lazy list
```

---

## 4. Module Dependency Graph

```
app
├── core:common, core:designsystem, core:domain, core:model, core:media
├── data
└── feature:gameplay, feature:settings, feature:leaderboard, feature:account

feature:gameplay
├── core:common, core:designsystem, core:domain, core:media
├── data (repository interfaces via Hilt)
└── feature:settings (VibrationManager)

feature:leaderboard, feature:account, feature:settings
└── core:common, core:designsystem, data

core:domain
└── core:common (models only)

data
└── core:common (models only)

data-test
└── data (repository interfaces)
```

**Rules:**
- `feature:*` modules must not depend on each other directly (except `feature:gameplay` → `feature:settings` for `VibrationManager`).
- `core:domain` must not depend on `data` directly — it receives data via DI.
- `data` must not import anything from `feature:*`.
- `core:designsystem` must not import from `data` or `feature:*`.

---

## 5. Architecture Pattern

```
UI (Composable Screen)
    │  collectAsStateWithLifecycle()
    ▼
ViewModel (@HiltViewModel)
    │  StateFlow<XxxUiState>
    │  onEvent(XxxEvent) / direct public fun
    ▼
UseCase / Engine (core:domain — injected by Hilt)
    │  StateFlow<GameState> upstream
    ▼
Repository Interface (data — injected by Hilt)
    │  suspend fun / Flow<T>
    ▼
DataSource / DataStore / Firestore
```

- Single-activity (`MainActivity`).
- Single `NavHost` (`SeekCatchNavHost`) with `startDestination = GAME_MAIN_ROUTE`.
- Navigation patterns: push for game screens, bottom-nav for top-level destinations (Leaderboard, Account).
- UDF: UI sends events down, ViewModel emits `StateFlow<UiState>` up.
- Game logic lives entirely in `core:domain` engines/use cases; ViewModels observe game state and translate it to UI state.

---

## 6. Game State Machine

### 6.1 States (both Flow and Flash)
- `Idle` — initial, before game is configured
- `Created` — game configured, waiting for countdown to complete
- `Countdown` — 3-second countdown; goals generated; no input
- `Started` — input enabled; timers active
- `Paused` — timers frozen; animations stopped; input disabled
- `Resumed` — active game state with full data snapshot
- `Finished` — no further mutations; result snapshot available

### 6.2 State Transition Rules
- `Countdown`: goals generated once; no input accepted; countdown sound plays.
- `Started` / `Resumed`: input enabled; timers active.
- `Paused`: timers frozen; animations stopped; input disabled.
- `Finished`: no further mutations allowed; result snapshot created.
- Restart always produces a new `Created` state — no state from the previous session leaks.

---

## 7. Game Modes

### 7.1 Supported Modes
- `FLASH`
- `FLOW` (with randomised scrolling direction)

---

## 8. Goal System

- Goal type: Color-based or Shape-based.
- Goal conflicts are forbidden by the generator.
- One active goal at a time; generated once at game init; never changed during session.
- At least one item matching the goal is guaranteed in every grid state.

---

## 9. Grid & Item Rules

### Grid Size by Difficulty
| Difficulty | Items per Row (`gridWidth`) |
|---|---|
| Easy | 3 |
| Normal | 4 |
| Hard | 5 |

**Row count (Flash only):** `rowCount` is not fixed; it is computed in `FlashGameScreen` using `BoxWithConstraints` before the game starts and forwarded to `FlashGameViewModel` via `setGridRowCount(rowCount)`:

```
cellSizePx  = (constraints.maxWidth − 2 × gridPaddingPx) / gridWidth
rowCount    = floor((constraints.maxHeight − 2 × gridPaddingPx) / cellSizePx)
              coerced to ≥ 1
```

`gridPadding` is the fixed `16.dp` padding applied to the grid container. The total cell count passed to `FlashGameEngine` is `gridWidth × rowCount`. `GameParams.gridRowCount` carries this value; `null` falls back to `gridWidth` (square grid, used by unit tests).

### Distribution
- Random placement.
- At least one valid goal item guaranteed.
- No conflicting goals in generated set.

---

## 10. Health & Failure Logic

### Initial Health
| Difficulty | HP |
|---|---|
| Easy | 5 |
| Normal | 3 |
| Hard | 1 |

Max HP = 5 for all difficulties.

### Failure Rules
| Event | Result |
|---|---|
| Wrong tap | Immediate Game Over |
| Missed correct item | Coefficient drop |
| Missed item + coefficient == x1 | −1 HP |
| HP reaches 0 | Game Over |

No other instant-death paths are allowed.

### Health Recovery
Triggered after passing N items without a miss:
| Difficulty | Items Required |
|---|---|
| Easy | 25 |
| Normal | 50 |
| Hard | 100 |

---

## 11. Coefficient System

- Starts at x1.
- Multiplies score and influences speed.
- **Increase:** `coefficient += coefficientStep` per correct tap.
  - Easy: step = 0.25, Normal: step = 0.20, Hard: step = 0.10
- **Decrease:** `newCoefficient = max(1.0, current / 2)` on missed correct item.
- No coefficient drop sound.
- No upper limit.

---

## 12. Scoring

`score += scorePoint × coefficient`

| Difficulty | scorePoint |
|---|---|
| Easy | 10 |
| Normal | 15 |
| Hard | 20 |

---

## 13. Flow Speed Model

```
actualDurationPercentage = 1.0 - coefPercentage - timePercentage
coefPercentage = coefficient² / 100
timePercentage = ((seconds / 30) × 5) / 100
min(actualDurationPercentage) = 0.35  ← hard floor
```

---

## 14. Flash Speed Model

```
visibleAtOnce          = random(visibleAtOnceMin, visibleAtOnceMax)            ← drawn per cycle from difficulty range
minCorrect             = ceil(visibleAtOnce / 2)                               ← at least half the visible slots are correct
correctCount           = random(minCorrect, visibleAtOnce)                     ← drawn per cycle; inclusive both ends
flashMillis            = (correctCount × flashTimePerItemMillis) / sqrt(floor(coefficient)) ← coerced ≥ MIN_FLASH_MILLIS (300 ms)
spawnPeriodMillis      = (rowDuration × 1.2 × 1.5) / coefficient              ← unchanged; coerced ≥ MIN_SPAWN_PERIOD_MILLIS (300 ms)
flashTimePerItemMillis — per-difficulty constant (Easy=700, Normal=500, Hard=350)
visibleAtOnceMin/Max   — per-difficulty range: Easy (3–4), Normal (5–6), Hard (7–8)
```

**Recalculation:** `flashMillis` is recalculated at the start of each flash cycle, not on `onTimeTick`. `spawnPeriodMillis` is kept current by `updateDurations()` (called from `onTimeTick`) and updates immediately on every correct tap and coefficient decrease.

**Density:** Total items shown per cycle is always `visibleAtOnce`. Of those, `correctCount` are goal-matching; `(visibleAtOnce − correctCount)` are decoys. Coefficient does not affect density — pressure is applied through shorter, faster flashes only.

**Time decay:** Flash uses no time-decay multiplier. `calculateDurationPercentage` (the time-decay formula used in Flow §13) was evaluated and explicitly removed; Flash is coefficient-only.

**Floor dominance at high coefficient:** At coefficient ×5 with `flashTimePerItemMillis=350` (Hard) and `correctCount=1`, raw duration is 70 ms — well below the 300 ms floor. The floor defines the maximum playable speed cap; the formula controls pacing in the low-to-mid coefficient range.

---

## 15. Firebase Integration Pattern

### Authentication
- Class: `FirebaseAuthDataSource` (`data/firebase/datasource/auth/`)
- Uses `Firebase.auth.signInAnonymously().await()` for one-shot anonymous sign-in.
- Called once on app start via `AuthUseCase` → `AuthRepositoryImpl`.
- `getUserId()` returns empty string on failure — callers must handle empty string gracefully.

### Firestore — Leaderboard
- Collection: `leaderboard`
- Documents keyed by `userId`.
- Real-time listener: `leaderboardCollection.snapshots().map { it.toObjects<LeaderboardRecord>() }` — returns `Flow<List<LeaderboardRecord>>`.
- Write: `userDocument.set(recordMap)` — fire-and-forget with `addOnFailureListener` logging only.
- No `callbackFlow` or `channelFlow` needed — the Firestore KTX `.snapshots()` extension returns a Kotlin Flow directly.

### Firestore — Users
- Collection: `users`
- Accessed via `UserFirestoreDataSource` for display name resolution.
- Real-time with `.snapshots()`.

### Error Handling
- Firestore write failures: `Log.w(TAG, ...)` only — never throw, never crash.
- Auth failure: returns `null` / empty string — repository handles defensively.
- Rank fetch failure: display "—" in Result Screen — never crash.
- Flow collection in ViewModels: errors propagate via StateFlow update with error state; not caught silently.

---

## 16. Data Model — Firestore

### Collection: `leaderboard`
Document path: `leaderboard/{userId}`

| Field | Type | Written by |
|---|---|---|
| `userId` | String | Client (Firebase UID) |
| `score` | Int | Client |
| `gameMode` | String | Client (GameMode.name) |
| `difficulty` | String | Client (GameDifficulty.name) |
| `userName` | String? | Client (optional, from user profile) |

Note: `userName` is resolved at read time by joining with the `users` collection in `LeaderboardRepositoryImpl` — it may differ from the value stored in the leaderboard document.

### Collection: `users`
Document path: `users/{userId}`

Fields derived from `UserFirestoreDataSource` and `UserRepositoryImpl` — exact schema to be confirmed with author.
<!-- inferred from code — verify with author -->

---

## 17. Audio & Haptics

### Sounds
- Countdown start (3s)
- Correct tap
- Wrong tap
- Game over
- New best score

### Music
- Background loop per mode.
- Muted on pause.

### Vibration
- Correct tap: light.
- Wrong tap / Game Over / Health or Coefficient loss: strong.
- Patterns configurable via `VibrationManager` in `feature:settings`.

### Visual Feedback
- **Shake:** triggered on health or coefficient loss.
- **Red Flash:** overlay on Game Info Panel on health or coefficient loss.
- **Fragment particles:** on correct item tap.
- All managed via `VisualFeedbackManager` in `core:common`.

---

## 18. Pause UX Guarantees
- Timers stopped.
- Animations frozen.
- Input disabled.
- Resume restores exact game state.

---

## 19. Result Screen
- Displays: Final Score, Best Score (local + remote), Leaderboard Rank.
- Restart: same mode + difficulty, full state reset, countdown replayed, no cached state.
- "To Main": navigate back to Game Selection.

---

## 20. Leaderboard System

### Auth
- Auto anonymous login on first launch.
- Optional Google account linking.
- User ID remains stable.

### Submission Rules
- Submit only if score > previous best.
- Offline → skip or queue silently.

### Rank Fetch
- Rank fetched after submission.
- Offline → display "—".
- Failure must never crash UI.

---

## 21. Naming Conventions

| Artefact | Convention | Example |
|---|---|---|
| ViewModel | `XxxViewModel` | `FlowGameViewModel` |
| UI State | `XxxUiState` (data class) | `FlowGameUiState` |
| UI Event (sealed) | `XxxUiEvent` or `XxxEvent` | `FlowGameUiEvent`, `LeaderboardEvent` |
| Game State (sealed) | `XxxGameState` | `FlowGameState` |
| Game Event (sealed) | `XxxGameEvent` | `FlowGameEvent` |
| Repository interface | `XxxRepository` | `LeaderboardRepository` |
| Repository impl | `XxxRepositoryImpl` | `LeaderboardRepositoryImpl` |
| Firestore DataSource | `XxxFirestoreDataSource` | `LeaderboardFirestoreDataSource` |
| Hilt Module | `XxxModule` | `DataModule`, `SettingsModule` |
| Screen Composable | `XxxScreen` | `FlowGameScreen`, `LeaderBoardScreen` |
| Navigation fun | `navigateToXxx` | `navigateToFlowGame` |
| Navigation route | `XXX_ROUTE` constant | `GAME_MAIN_ROUTE` |
| Preview | `XxxPreview` (private) | `PixelButtonPreview` |
| Fake for testing | `FakeXxxRepository` | `FakeFiguresRepository` |

---

## 22. Architecture Boundary Rules

| Layer | May depend on | Must NOT import |
|---|---|---|
| `feature:*` UI (Composable) | `core:designsystem`, `core:common` models | Repository interfaces/impls, Firestore classes |
| `feature:*` ViewModel | `core:domain` use cases, `data` repository interfaces, `core:common` | Firestore classes (`com.google.firebase.firestore.*`) |
| `core:domain` | `core:common` models | `data` repository impls, `feature:*`, Firestore |
| `data` repository impls | `core:common` models, Firebase SDK, DataStore | `feature:*`, `core:domain` |
| `core:designsystem` | Compose, Material3 | `data`, `feature:*`, `core:domain` |

**Enforced by:** `.claude/skills/check-arch/check.sh`

---

## 23. Testing Approach

- **Unit tests:** `src/test/java/` in each module.
- **Instrumentation tests:** `src/androidTest/java/` (Compose UI Testing + Espresso).
- **Frameworks:** JUnit 4, Mockito-Kotlin, Kotlinx Coroutines Test.
- **Fakes:** `data-test` module provides `FakeFiguresRepository`, `FakeGoalsRepository`, etc. for ViewModel tests.
- **Coverage targets:** All ViewModel logic; all game engine state transitions; repository implementations spot-checked.

---

## 24. Invariants (Must Never Break)

- Wrong tap = instant game over.
- Goals do not change mid-game.
- Result screen always reachable.
- Restart never leaks state.
- Leaderboard failure never crashes app.
