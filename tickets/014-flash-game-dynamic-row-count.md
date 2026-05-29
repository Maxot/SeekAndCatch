# Ticket 014 — Flash Game: Dynamic Row Count Based on Available Screen Height

**Status:** Done
**Depends on:** (none)

---

## Context

The Flash game grid is currently a square: `gridWidth × gridWidth` cells (e.g. 4×4 = 16 cells on Normal). On taller phones this leaves significant dead space below the grid. The fix is to compute the row count from available screen height so the grid always fills the space, with the constraint that the last row must be fully visible (no clipping).

Column count (`gridWidth`) is unchanged — it remains difficulty-driven (Easy=3, Normal=4, Hard=5).

---

## Task

### 1. Add `gridRowCount` to `GameParams`

In `core/common/src/main/java/.../model/GameParams.kt`, add:

```kotlin
val gridRowCount: Int? = null   // null → falls back to gridWidth (square)
```

### 2. Update `FlashGameEngine` to use `gridRowCount`

In `core/domain/src/main/java/.../engine/FlashGameEngine.kt`, line 35:

```kotlin
// before
val gridCount = gridWidth * gridWidth
// after
val rowCount = (params.gridRowCount ?: gridWidth).coerceAtLeast(1)
val gridCount = gridWidth * rowCount
```

The `rowWidth` field of `FlashGameData` (and `FlashGameState`) already carries the column count; add a `rowCount` field if `FlashGameState` needs to surface row count to the UI (check `FlashGameState.kt`).

### 3. ViewModel: accept and forward row count

In `FlashGameViewModel`, add:

```kotlin
fun setGridRowCount(rowCount: Int)
```

Store it (e.g. as a private `MutableStateFlow<Int>`) and inject it into `GameParams` when building params for `startInitJob`. The ViewModel must not start the engine until the row count has been set at least once.

### 4. UI: measure available height and calculate row count

In `FlashGameScreen.kt`, wrap the grid-area content in `BoxWithConstraints`. Inside:

```
gridPaddingPx      = 16.dp.toPx()
cellSizePx         = (constraints.maxWidth - 2 * gridPaddingPx) / gridWidth
rowCount           = ((constraints.maxHeight - 2 * gridPaddingPx) / cellSizePx)
                       .toInt().coerceAtLeast(1)
```

Call `viewModel.setGridRowCount(rowCount)` via `LaunchedEffect` keyed on `rowCount`. This must run before `onStart()` is called (it always will, since the ready-state screen is shown first).

Pass the computed `gridSize = gridWidth * rowCount` through `uiState` as before — no change to `FlashGameFieldLayout` signature.

### 5. Account for padding in the row count formula

The grid container keeps its `padding(16.dp)`. The row count formula subtracts `2 × 16.dp` from both axes before dividing, so the computed rows fit exactly within the padded area.

---

## Acceptance Criteria

- [ ] On a standard phone (e.g. Pixel 6a), the Flash game grid fills the screen below the `GameInfoPanel` with no empty vertical gap and no clipped bottom row.
- [ ] `gridRowCount` is derived purely from screen dimensions; no hardcoded row count remains in production code.
- [ ] `GameParams.gridRowCount = null` still produces a square grid (backward-compatible default; used by unit tests).
- [ ] `FlashGameEngine` unit tests pass unchanged (`gridRowCount` defaults to `null` → same square behaviour).
- [ ] New ViewModel unit test: `setGridRowCount(6)` with `gridWidth=4` produces `gridSize=24` in the engine params.
- [ ] No business logic added to Composables (row count arithmetic from screen dimensions is acceptable; game-rule logic is not).
- [ ] No Firestore imports in ViewModel or UI layer.
- [ ] All new/changed screen files have at least one `@Preview`.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] Passes `check-arch` with exit 0.
- [ ] Visually verified on emulator.

---

## Files to Create

(none)

## Files to Modify

- `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/GameParams.kt` — add `gridRowCount: Int? = null`
- `core/domain/src/main/java/com/maxot/seekandcatch/core/domain/engine/FlashGameEngine.kt` — use `gridRowCount` for row dimension
- `core/domain/src/main/java/com/maxot/seekandcatch/core/domain/flash/FlashGameState.kt` — add `rowCount` field if needed to surface it to UI
- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/ui/flashgame/FlashGameViewModel.kt` — add `setGridRowCount()`, inject into params
- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/ui/flashgame/model/FlashGameUiState.kt` — confirm `gridSize` is derived correctly (no change likely)
- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/ui/flashgame/FlashGameScreen.kt` — `BoxWithConstraints` measurement, call `setGridRowCount`
- `docs/PRD.md` — Section 5.2: change "gridWidth × gridWidth" to "gridWidth × rowCount (rowCount = floor(availableHeight / cellSize))"
- `docs/TECH_SPEC.md` — Section 9: add row count derivation formula

---

## Notes

**Doc updates required before implementation:**
- `docs/PRD.md` Section 5.2: grid description currently says `gridWidth × gridWidth`; update to dynamic rows.
- `docs/TECH_SPEC.md` Section 9 (Grid & Item Rules): add row count formula and note that Flash grid is no longer square.

**CONCEPT check:** No core principle violated. Visual/layout polish only; does not affect scoring, failure rules, or monetisation.

**Architecture note:** The row count is computed from Compose layout constraints (`BoxWithConstraints`). This is screen-measurement input, not game logic, so placing the arithmetic in the Composable is acceptable per the "no business logic in Composables" rule. The ViewModel merely stores the result and forwards it to the engine.
