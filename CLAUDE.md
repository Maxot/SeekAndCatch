# CLAUDE.md — Seek and Catch

This file is read by Claude Code at the start of every session. Every section is project-specific — do not treat this as a template.

---

## Documents

Read these in order before making any changes:

| Document                | Purpose |
|-------------------------|---|
| `docs/CONCEPT.md`       | Product vision, core principles, out-of-scope boundaries, roadmap phases |
| `docs/PRD.md`           | Full product requirements: entities, screens, gameplay rules, UX invariants |
| `docs/TECH_SPEC.md`     | Authoritative technical architecture, game logic formulas, Firestore schema, naming conventions, boundary rules |
| `docs/DESIGN_SYSTEM.md` | Colour palette, typography, components, navigation, interaction patterns |
| `tickets/_template.md`  | Template for creating new tickets |
| `tickets/`              | Individual implementation tickets (one per session of work) |

## Document Hierarchy

```
CONCEPT.md  (product vision — highest authority)
    └── PRD.md  (product requirements — governs feature scope)
            ├── TECH_SPEC.md  (architecture and game logic — governs implementation)
            └── DESIGN_SYSTEM.md  (visual rules — governs UI implementation)
                        └── Code
```

**Rules:**
- Never modify a higher-level document to justify a lower-level decision.
- When making changes, identify the highest level affected and update downward.
- When asked to implement something, verify upward first: does it fit CONCEPT? Is it in PRD scope? Does it comply with TECH_SPEC?
- When updating documents, cascade explicitly — a PRD change may require TECH_SPEC and design system updates.
- When in doubt, ask before implementing.

---

## Skills

| Skill | Trigger | Command |
|---|---|---|
| `check-arch` | Before any commit; part of ticket Definition of Done | `.claude/skills/check-arch/check.sh` |
| `start-ticket` | Beginning of every implementation session | `.claude/skills/start-ticket/start.sh <ticket-id>` |
| `new-feature` | Starting a new feature module | `.claude/skills/new-feature/scaffold.sh <feature-name>` |
| `firestore-collection` | Adding a new Firestore collection integration | `.claude/skills/firestore-collection/scaffold.sh <collection-name> --ops <read\|write\|readwrite>` |
| `commit` | Committing completed ticket work | `.claude/skills/commit/commit.sh <type> <ticket-id\|-> "<description>"` |
| `concept-to-tickets` | Walking a large concept through the doc hierarchy | `.claude/skills/concept-to-tickets/run.sh "<concept>" --phase <1-5>` |
| `idea-to-ticket` | Turning a raw idea into a draft ticket with doc impact analysis | `.claude/skills/idea-to-ticket/run.sh "<idea>"` |

---

## How to Run

### Prerequisites
- Android Studio Jellyfish (2023.3.1) or newer
- JDK 17
- Android SDK 26+
- A `google-services.json` placed in `app/`
- Install git hooks (once per clone):
  ```bash
  git config core.hooksPath .claude/hooks
  ```

### Build
```bash
./gradlew assembleDebug
```

### Run unit tests
```bash
./gradlew test
```

### Run instrumentation tests (requires connected device/emulator)
```bash
./gradlew connectedAndroidTest
```

### Lint
```bash
./gradlew lint
```

### Full build (assemble + test + lint)
```bash
./gradlew build
```

---

## Architecture

### Layer Boundaries

| Layer | Location | Allowed to import | Forbidden |
|---|---|---|---|
| Screen (Composable) | `feature:*/ui/` | `core:designsystem`, `core:common` models, `core:model` | Repository interfaces/impls, Firestore classes |
| ViewModel | `feature:*/` | `core:domain` use cases, repository interfaces from `data`, `core:common` | `com.google.firebase.firestore.*`, Firebase Auth classes |
| UseCase / Engine | `core:domain/` | `core:common` models | `data` implementations, `feature:*`, Firestore |
| Repository Impl | `data/repository/` | `core:common` models, Firebase SDK, DataStore | `feature:*`, `core:domain` |
| DataSource | `data/firebase/datasource/` | Firebase SDK, `core:common` models | `feature:*`, `core:domain`, `data/repository/` |

**Enforced by:** `.claude/skills/check-arch/check.sh`

---

## Coding Conventions

### Naming

| Artefact | Convention | Example |
|---|---|---|
| ViewModel | `XxxViewModel` | `FlowGameViewModel` |
| UI State (data class) | `XxxUiState` | `FlowGameUiState` |
| UI Event (sealed class) | `XxxUiEvent` or `XxxEvent` | `FlowGameUiEvent`, `LeaderboardEvent` |
| Game State (sealed class) | `XxxGameState` | `FlowGameState` |
| Game Event (sealed class) | `XxxGameEvent` | `FlowGameEvent` |
| Repository interface | `XxxRepository` | `LeaderboardRepository` |
| Repository implementation | `XxxRepositoryImpl` | `LeaderboardRepositoryImpl` |
| Firestore DataSource | `XxxFirestoreDataSource` | `LeaderboardFirestoreDataSource` |
| Hilt Module | `XxxModule` | `DataModule`, `SettingsModule` |
| Screen Composable | `XxxScreen` | `FlowGameScreen`, `LeaderBoardScreen` |
| Navigation function | `navigateToXxx` | `navigateToFlowGame` |
| Navigation route constant | `XXX_ROUTE` | `GAME_MAIN_ROUTE` |
| Preview | `XxxPreview` (private) | `PixelButtonPreview` |
| Test fake | `FakeXxxRepository` | `FakeFiguresRepository` |

### File Structure (per feature module)
```
feature/<name>/
  src/main/java/com/maxot/seekandcatch/feature/<name>/
    navigation/        — XxxNavigation.kt (route constants, composable extension on NavGraphBuilder)
    ui/                — XxxScreen.kt, sub-composables
    ui/layout/         — layout sub-composables for complex screens
    <Feature>ViewModel.kt
    <Feature>UiState.kt
    <Feature>Event.kt
```

---

## What Never To Do

1. **No Firestore imports in ViewModels or UI layer.** `com.google.firebase.firestore.*` must only appear in `data/firebase/datasource/`.
2. **No Firebase Auth imports outside the auth data source.** Only `FirebaseAuthDataSource` may import `com.google.firebase.auth.*`.
3. **No business logic in Composables.** State derivation, scoring calculations, and condition checks belong in ViewModel or UseCase.
4. **No direct Firestore calls outside the repository/datasource layer.** All cloud access goes through a `DataSource` class.
5. **No anonymous Auth state checked outside `AuthRepositoryImpl` / `FirebaseAuthDataSource`.** Other code calls `authRepository.getUserId()`.
6. **No `Room` database.** This project uses DataStore Preferences for local settings and Firestore for cloud data. Do not add Room.
7. **No dynamic colour (Material You).** `dynamicDarkColorScheme` / `dynamicLightColorScheme` are disabled intentionally.
8. **No soft wrong-tap consequences.** A wrong tap is always instant game over. Never add leniency.
9. **No `collectAsState()` in production Composables.** Use `collectAsStateWithLifecycle()`.
10. **No goal changes mid-session.** Goals are generated once at game init and are immutable.

---

## How Tickets Work

Tickets live in `tickets/` as individual Markdown files named `NNN-short-title.md` (e.g. `001-leaderboard-offline.md`).

Each ticket represents one atomic Claude Code session of work.

**Before coding:**
```bash
.claude/skills/start-ticket/start.sh <ticket-id>
```
This verifies dependencies, prints the ticket context, and confirms files to create/modify.

**Definition of Done:**
A ticket is done when all of the following are true:

- [ ] All files listed in "Files to create/modify" exist with correct content.
- [ ] All acceptance criteria in the ticket checklist are verifiably met.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] Passes check-arch — run `.claude/skills/check-arch/check.sh` with exit 0.
- [ ] If the ticket includes UI: the screen has been visually verified on an emulator.
- [ ] All new screen files have at least one `@Preview`.
- [ ] Unit tests exist for all ViewModel logic added or changed by this ticket.
- [ ] If the ticket changed behaviour described in PRD: `docs/PRD.md` updated accordingly.
- [ ] If the ticket changed architecture or introduced new patterns: `docs/TECH_SPEC.md` updated accordingly.
- [ ] If the ticket changed visual identity or microcopy: `docs/DESIGN_SYSTEM.md` updated accordingly.
- [ ] The ticket's `Status` field is updated to `Done`.

**After coding:**
```bash
.claude/skills/commit/commit.sh feat <ticket-id> "brief description"
```

---

## Memory

Session-specific memory and project context is stored in `.claude/projects/memory/`. Claude Code reads this automatically at session start.
