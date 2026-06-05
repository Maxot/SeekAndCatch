# Ticket 016 — Small-screen layout fixes (360×640dp)

**Status:** Done
**Depends on:** (none)

---

## Context

Verified on a 360×640dp emulated screen (720×1280 at 320dpi override on Pixel 9 Pro). Three screens had content cut off with no way to reach it. All game-mode and result screens were confirmed safe.

Partial work was completed during the verification session and exists as uncommitted changes in the working tree.

---

## Task

### Already done (working tree, not yet committed)

| File | Change |
|---|---|
| `feature/account/.../ui/AccountScreen.kt` | Added `verticalScroll(rememberScrollState())` to content Column; changed `Arrangement.Center` → `Arrangement.Top` |
| `feature/gameplay/.../ui/layout/GoalsLayout.kt` | `DetailedGoalsLayout`: replaced `FlowRow` with a `Row` + `BoxWithConstraints` so figures resize to always fit on one line. Figure size = `(availableWidth / count).coerceAtMost(50.dp)`. Label uses `softWrap = false`. |
| `feature/gameplay/.../gameselection/GameSelectionScreen.kt` | Proportional layout — removed scroll + spacer; Column uses `Arrangement.Top`; three weighted sections: `ModeSelectionLayout` in `Box(weight(6f))`, Start Game button in `Box(weight(2f))`, `GameDifficultSelectorLayout` in `Box(weight(2f))`; cards use `fillMaxHeight()` to fill allocated slot. `StartGameLayout` no longer used here. |

### Still to do

None — all screens now fit correctly without scrolling on 360×640dp.

### Screens confirmed safe (no changes needed)

| Screen | Reason |
|---|---|
| `FlowGameScreen` / `FlashGameScreen` active play | `GameInfoPanel` + `weight(1f)` game field; fills height correctly |
| `ReadyToGameLayout` / `ReadyToFlashGameLayout` | Column wraps intrinsic content (~250dp); no `fillMaxSize` applied |
| `GameResultScreen` | Content ~300dp max; fits in ~480dp available content area |
| `LeaderboardScreen` | `LazyColumn` is inherently scrollable |
| `SplashScreen` | Centred logo on `fillMaxSize` background |

---

## Acceptance Criteria

- [ ] On a 360×640dp device, the Delete Account button on `AccountScreen` is reachable by scrolling.
- [ ] On a 360×640dp device, `DetailedGoalsLayout` shows all goal figures on a single line; figure size shrinks when count is high.
- [ ] On a 360×640dp device, `GameSelectionScreen` shows the game card (6/10), Start Game button (2/10), and difficulty selector (2/10) all visible simultaneously without scrolling.
- [ ] On a 360×640dp device, tapping Start Game navigates to the game.
- [ ] All three fixes above are visually verified on the emulator.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] Passes `check-arch` with exit 0.
- [ ] No new unit tests required (no ViewModel logic changed).

---

## Files to Modify

- `feature/account/src/main/java/com/maxot/seekandcatch/feature/account/ui/AccountScreen.kt` — verticalScroll + Arrangement.Top *(done)*
- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/ui/layout/GoalsLayout.kt` — BoxWithConstraints figure sizing in DetailedGoalsLayout *(done)*
- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/gameselection/GameSelectionScreen.kt` — proportional weighted layout, no scroll *(done)*

---

## Notes

- Minimum supported SDK is 26 (Android 8.0). Common small phones at that API level ship at 360×640dp.
- `BoxWithConstraints` is stable in Compose BOM 2025.05.01; no new dependency needed.
- Figure size formula: `(maxWidth / count - 20.dp).coerceIn(16.dp, 50.dp)` — `ColoredFigureLayout` applies `.padding(10.dp)` before `.size()`, so each figure's real footprint is `size + 20dp`. Subtracting 20dp gives the correct drawable area. On a 360dp screen (GameInfoPanel padding ~28dp each side, label ~60dp), BoxWithConstraints gets ≈ 272dp. With 4 figures: size = 272/4 − 20 = 48dp → fits exactly. With 3 figures: capped at 50dp. Verified with 4 figures on 360×640dp.
- `StartGameLayout.kt` is no longer used by `GameSelectionScreen` but is kept in place — it still has its own `@Preview`.
- `weight()` on children requires the parent Column to have a bounded height (`fillMaxSize()` provides that).
- The `fillMaxHeight()` on game preview cards fills whatever the `Box(weight(6f))` slot allocates; the card content (scrollable game field with large spacer) always overflows the card height, so no blank space appears inside the card.
