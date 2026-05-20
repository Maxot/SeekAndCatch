# SeekAndCatch — Development Guidelines for Junie

## Source of Truth
Before changing code, **always** consult:

- `/docs/PRD.md`       → product requirements
- `/docs/TECH_SPEC.md` → technical rules, formulas, and game logic

**Priority:** TECH_SPEC overrides PRD in case of mismatch.

## Junie Behavior Rules
1. Junie **must read** PRD + Tech Spec before executing a ticket.
2. Do **not** invent new mechanics.
3. Do **not** alter gameplay logic unless a ticket explicitly allows it.
4. Respect all invariants:
    - Wrong tap = immediate game over
    - Goal remains fixed during session
    - Restart never leaks previous state
    - Leaderboard failures do not crash the app
5. All generated code must include:
    - Modified/added file list
    - Tests if applicable
    - Description of edge cases handled

## Tickets
- Tickets live in `/tickets` as individual `.md` files.
- Each ticket represents **one atomic change**.
- Junie executes **one ticket at a time**.
- Tickets must reference relevant files and docs.

## Forbidden Actions
- Changing core rules without updating TECH_SPEC
- Adding hidden mechanics
- Merging changes without review
- Executing multiple tickets at once

## Tech Stack & Tools
- **Language:** Always use Kotlin.
- **UI Framework:** Use Jetpack Compose for all new UI.
- **Asynchrony:** Use Kotlin Coroutines and Flow; 
- **Build System:** Use Gradle Kotlin DSL (`.gradle.kts`) and Version Catalogs (`libs.versions.toml`).

## Architecture (MVI)
- Follow a strict MVI pattern: View -> Intent -> ViewModel -> Repository -> Data Source.
- **ViewModels:** Use `viewModelScope` for coroutines. Never pass Context or View references into ViewModels.
- **Dependency Injection:** Use Hilt. Always use constructor injection for classes.
- **State:** Use `MutableStateFlow` in ViewModels and expose as `StateFlow`.

## UI & Styling
- Use **Material 3** components and theming.
- **Accessibility:** Always provide `contentDescription` for images and ensure minimum touch targets of 48dp.
- **Previews:** Every Composable should have a `@Preview` function with a light and dark mode configuration.

## Data & Storage
- Use **Room** for local persistence.
- Define DAOs with `suspend` functions for one-shot operations and `Flow` for observables.

## Testing
- **Unit Tests:** Use JUnit 5 and MockK. Place them in `src/test`.
- **UI Tests:** Use Compose Testing library. Place them in `src/androidTest`.
