# Ticket 002 — Move Figure and Goal Models from data:model to core:common

**Status:** Done
**Depends on:** (none)

---

## Context

`Figure` and `Goal` are core game entities used across multiple modules: `core:domain`, `feature:gameplay`, `feature:account`, and `data`. Currently they live in `com.maxot.seekandcatch.data.model`, which means feature UI files import from the `data` module — a direct violation of the architecture boundary rules defined in TECH_SPEC §21.

Affected files with the violation:
- `feature/account/src/.../ui/AccountScreen.kt` — imports `com.maxot.seekandcatch.data.model.Figure`
- `feature/gameplay/src/.../gameselection/GameSelectionScreen.kt` — imports `com.maxot.seekandcatch.data.model.Figure`
- Multiple files in `core:domain` and `feature:gameplay` that import these models

The fix is to move `Figure.kt` and `Goal.kt` from `data/src/main/.../data/model/` to `core/common/src/main/.../core/common/model/`, which is where shared game models already live (`GameDifficulty`, `GameMode`, `LeaderboardRecord`, `User`).

Reference: TECH_SPEC §21 Architecture Boundary Rules; TECH_SPEC §20 Naming Conventions.

---

## Task

1. Move `data/src/main/java/com/maxot/seekandcatch/data/model/Figure.kt` to `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/Figure.kt`.
   - Update package declaration from `com.maxot.seekandcatch.data.model` to `com.maxot.seekandcatch.core.common.model`.
   - `Figure` uses `RoundedTriangleShape` from `core:designsystem` — this import is fine since `core:common` can depend on `core:designsystem`. Verify `core:common/build.gradle.kts` has `core:designsystem` as a dependency; add it if not.

2. Move `data/src/main/java/com/maxot/seekandcatch/data/model/Goal.kt` to `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/Goal.kt`.
   - Update package declaration accordingly.

3. Update all import statements across the codebase that referenced `com.maxot.seekandcatch.data.model.Figure` or `com.maxot.seekandcatch.data.model.Goal` to use the new package.
   - Use `grep -r "com.maxot.seekandcatch.data.model" --include="*.kt"` to find all affected files.

4. If `data/src/main/.../data/model/` directory becomes empty, delete it.

5. Update `data/build.gradle.kts` to ensure `core:common` is a dependency (needed for the models it still uses).

6. Update `docs/TECH_SPEC.md` Section 3 (Project Structure) to note that `Figure` and `Goal` live in `core:common`.

---

## Acceptance Criteria

- [ ] `Figure` and `Goal` are in package `com.maxot.seekandcatch.core.common.model`.
- [ ] No files import `com.maxot.seekandcatch.data.model.Figure` or `com.maxot.seekandcatch.data.model.Goal`.
- [ ] `check-arch` exits 0 (no feature UI importing from `data` for these models).
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] Existing tests for `FigureRepositoryImplTest` still pass (update imports if needed).
- [ ] `docs/TECH_SPEC.md` Section 3 updated.
- [ ] No unit tests needed beyond ensuring existing tests pass.

---

## Files to Create

- `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/Figure.kt` — moved from `data:model`
- `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/Goal.kt` — moved from `data:model`

## Files to Modify

- `data/src/main/java/com/maxot/seekandcatch/data/model/Figure.kt` — delete (move)
- `data/src/main/java/com/maxot/seekandcatch/data/model/Goal.kt` — delete (move)
- All `*.kt` files that import `com.maxot.seekandcatch.data.model.*` — update imports
- `core/common/build.gradle.kts` — add `core:designsystem` dependency if missing
- `docs/TECH_SPEC.md` — update Section 3 project structure

---

## Notes

Run `grep -r "com.maxot.seekandcatch.data.model" . --include="*.kt" | grep -v build` first to get the complete list of affected files before starting.

`Figure.kt` has a dependency on `RoundedTriangleShape` from `core:designsystem`. Since `core:common` is a shared utilities module, having a Compose UI class (`Shape`) in it is slightly unusual, but it's already the home for `GameDifficulty` which references `GameParams` (pure Kotlin). The `getShapeForFigure()` extension function could stay in `core:designsystem` if preferred, but keeping it with `Figure` for now is acceptable.

`Goal.kt` type — check its content before moving to understand all dependencies.
