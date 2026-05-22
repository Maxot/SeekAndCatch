# Ticket 012 — Flash: fair-time scaling per cycle

**Status:** Draft
**Depends on:** 011 (Flash Speed Model spec must be finalised first)

---

## Context

Currently `flashMillis` is a fixed value per coefficient level — the player always gets the same window regardless of how many correct items appear in a cycle. This penalises cycles where more correct items are visible (not enough time to tap them all) and is overly generous on sparse cycles (plenty of time for one tap).

The goal is to make flash duration **proportional to the number of correct items shown that cycle**, trimmed by coefficient. More to click → more time. The coefficient still governs overall speed pressure. `spawnPeriodMillis` is unchanged.

---

## Task

### Step 1 — Add `flashTimePerItemMillis` to `GameParams`

In `core/common/.../model/GameParams.kt`, add:

```kotlin
val flashTimePerItemMillis: Int = 500
```

### Step 2 — Set per-difficulty values in `GameDifficulty`

In `core/common/.../model/GameDifficulty.kt`, set:

| Difficulty | `flashTimePerItemMillis` | Rationale |
|---|---|---|
| EASY | 700 | gridWidth=3, visibleAtOnce=2 → max 1 400 ms at coef ×1 |
| NORMAL | 500 | gridWidth=4, visibleAtOnce=3 → max 1 500 ms at coef ×1 |
| HARD | 350 | gridWidth=5, visibleAtOnce=4 → max 1 400 ms at coef ×1 |

### Step 3 — Rework `FlashGameEngine` flash-cycle logic

**Correct-item count is now deliberate, not incidental.**

Replace the current "guarantee ≥1 correct, fill rest randomly" strategy with:

```
correctCount      = random(1, visibleAtOnce)        // inclusive on both ends
flashMillis       = (correctCount × flashTimePerItemMillis) / coefficient
                    coerced ≥ MIN_FLASH_MILLIS (300 ms)
```

Cycle construction:
1. Draw `correctCount` indices from active suitable items (with force-generation if needed).
2. Fill remaining `(visibleAtOnce − correctCount)` slots with active non-suitable items.
3. Set `flashMillis` from the formula above before starting the display delay.
4. `spawnPeriodMillis` formula is **unchanged**: `(rowDuration × 1.2 × 1.5) / coefficient`.

`calculateFlashDuration` signature changes to accept `correctCount: Int` in addition to `base`/`data`, or can be rewritten to take `correctCount` and `params` directly — whichever is cleaner.

### Step 4 — Update TECH_SPEC §14 Flash Speed Model

Replace the current `flashMillis` formula line with:

```
correctCount      = random(1, visibleAtOnce)                            ← drawn per cycle
flashMillis       = (correctCount × flashTimePerItemMillis) / coefficient ← coerced ≥ 300 ms
spawnPeriodMillis = (rowDuration × 1.2 × 1.5) / coefficient            ← unchanged; coerced ≥ 300 ms
visibleAtOnce     = max(1, gridWidth − 1)                               ← fixed at init; difficulty-only
flashTimePerItemMillis — per-difficulty constant (Easy=700, Normal=500, Hard=350)
```

Add a note: "`flashMillis` is recalculated at the start of each flash cycle, not on `onTimeTick`. `spawnPeriodMillis` and durations for `onTimeTick` update remain coefficient-only."

### Step 5 — Update PRD §5.2 and §7

**§5.2 Flash Mode** — replace the flash-duration sentence with:

> Each cycle, between 1 and `visibleAtOnce` correct items are shown. Flash display duration is proportional to the number of correct items: `flashMillis = correctCount × timePerItem / coefficient`. Spawn period is coefficient-driven only.

**§7 Difficulty Rules** — add `flashTimePerItemMillis` column to the difficulty table.

### Step 6 — Update tests in `FlashGameEngineTest`

- Update `initialDurations_matchFlashSpeedModelFormula` — `flashMillis` is now variable; assert it is within `[MIN_FLASH_MILLIS, visibleAtOnce × flashTimePerItemMillis]` at init.
- Add `flashDuration_scalesWithCorrectItemCount`: stub the RNG (or seed it) so `correctCount` is deterministic; assert `flashMillis == (correctCount × flashTimePerItemMillis / coefficient).coerceAtLeast(300)`.
- Add `flashDuration_clampedToMinimumAtHighCoefficient`: drive coefficient high; assert all cycles observe `flashMillis >= 300`.

---

## Acceptance Criteria

- [ ] `GameParams` has `flashTimePerItemMillis` with default 500; `GameDifficulty` sets 700/500/350 per level.
- [ ] `FlashGameEngine` draws `correctCount = random(1, visibleAtOnce)` each cycle and sets `flashMillis` from the fair-time formula.
- [ ] Total items shown per cycle is still `visibleAtOnce = max(1, gridWidth − 1)` — `(visibleAtOnce − correctCount)` slots are filled with decoys.
- [ ] `spawnPeriodMillis` formula is unchanged.
- [ ] `flashMillis` is always `≥ MIN_FLASH_MILLIS` (300 ms).
- [ ] `docs/TECH_SPEC.md` §14 reflects the new formula.
- [ ] `docs/PRD.md` §5.2 and §7 updated.
- [ ] `FlashGameEngineTest` covers formula correctness and the MIN floor.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] Passes `check-arch` with exit 0.

---

## Files to Create

(none)

## Files to Modify

- `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/GameParams.kt` — add `flashTimePerItemMillis`
- `core/common/src/main/java/com/maxot/seekandcatch/core/common/model/GameDifficulty.kt` — set per-difficulty values
- `core/domain/src/main/java/com/maxot/seekandcatch/core/domain/engine/FlashGameEngine.kt` — implement fair-time formula; fix cycle construction
- `core/domain/src/test/java/com/maxot/seekandcatch/core/domain/engine/FlashGameEngineTest.kt` — update + add formula tests
- `docs/PRD.md` — §5.2 and §7 Difficulty Rules
- `docs/TECH_SPEC.md` — §14 Flash Speed Model

---

## Notes

**Why `flashMillis` is recalculated per cycle, not on `onTimeTick`:** The cycle duration is a function of `correctCount`, which is drawn fresh each cycle. Recalculating on every game-second tick would overwrite a mid-cycle duration with a stale `correctCount`. Instead, `flashMillis` is set at cycle start and consumed by the display `delay`.

**`onTimeTick` / `updateDurations`:** After this ticket, `updateDurations()` (called from `onTimeTick`) only needs to keep `spawnPeriodMillis` current. `flashMillis` in `GameEngineData` is still updated there for UI display purposes but the live delay inside `startFlashLoop` reads the value set at cycle start, not the one updated mid-cycle.

**Floor dominance at high coefficient:** At coefficient ×5 with `flashTimePerItemMillis=350` (Hard) and `correctCount=1`, raw duration is 70 ms — well below the 300 ms floor. This is intentional: the floor defines the maximum playable speed cap. The formula controls pacing in the low-to-mid coefficient range.

**`GameParams.flashTimePerItemMillis` is Flash-only.** `FlowGameEngine` ignores it. The field lives in `GameParams` to keep difficulty configuration in one place (consistent with all other per-difficulty knobs).
