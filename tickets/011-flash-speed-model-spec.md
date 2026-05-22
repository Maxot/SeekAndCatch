# Ticket 011 — Flash Speed Model: spec gap + density resolution

**Status:** Done
**Depends on:** (none)

---

## Context

PRD §5.2 describes Flash mode as: "Refresh rate and density depend on difficulty and coefficient."
TECH_SPEC §13 documents the Flow Speed Model in full but has no equivalent section for Flash.

`FlashGameEngine` has a fully implemented speed model, but it is entirely undocumented. Additionally, the code does not implement the PRD claim that **density** scales with coefficient — `visibleAtOnce` is set once at game init from `gridWidth` and never changes. The PRD overclaims, or the engine is incomplete.

A decision is needed before this ticket can be marked done: **does density scale with coefficient, or is it difficulty-only?**

---

## Task

### Step 1 — Owner decision required (before coding begins)

Answer: does `visibleAtOnce` (items shown per flash cycle) scale with coefficient?

- **Option A — Density is difficulty-only.** `visibleAtOnce = max(1, gridWidth − 1)` is correct as-is. PRD §5.2 is updated to remove "and coefficient" from the density clause.
- **Option B — Density scales with coefficient.** Introduce a formula (e.g. `visibleAtOnce = clamp(floor(gridWidth × coefFactor), 1, gridWidth²)`) and implement it in `FlashGameEngine`.

Recommendation: **Option A**. Principle 3 ("pressure must be earned") already applies via speed — the coefficient makes each flash shorter and more frequent. Adding density growth on top risks making the mid-to-high coefficient range unplayable. Keep density as a difficulty knob only.

### Step 2 — Update PRD §5.2

Replace the vague sentence with explicit behaviour. If Option A is chosen:

> - Items appear at random positions on a `gridWidth × gridWidth` grid.
> - Each cycle, `visibleAtOnce = max(1, gridWidth − 1)` items are shown simultaneously, guaranteed to include at least one goal-matching item.
> - Flash display duration and spawn period both decrease as coefficient rises (see TECH_SPEC Flash Speed Model).
> - Density (items per flash) depends on difficulty only; it does not scale with coefficient.

### Step 3 — Add TECH_SPEC Flash Speed Model section

Add a new section **§14 Flash Speed Model** (renumber §14 Firebase → §15, §15 Data Model → §16):

```
flashMillis     = (rowDuration × 2.0 × 1.2) / coefficient   ← coerced ≥ MIN_FLASH_MILLIS (300 ms)
spawnPeriodMillis = (rowDuration × 1.2 × 1.5) / coefficient  ← coerced ≥ MIN_SPAWN_PERIOD_MILLIS (300 ms)
visibleAtOnce   = max(1, gridWidth − 1)                      ← fixed at init; difficulty-only

Time factor: onTimeTick fires every game second; both durations are recalculated at each tick.
             (Time factor formula is defined in FlashGameEngine but currently has no effect because
              calculateFlashDuration / calculateSpawnDuration only divide by coefficient.
              Decide: keep time-decay parity with Flow, or leave Flash as coefficient-only.)
```

The time-decay sub-decision (whether `calculateDurationPercentage` should be wired in) should be captured explicitly — see Notes below.

### Step 4 — Remove dead code in FlashGameEngine

`calculateDurationPercentage(data: GameEngineData)` (lines 347–351) is defined but never called. Once the time-decay decision is made:

- If time decay is **not** wanted: delete the method.
- If time decay **is** wanted: wire it into `calculateFlashDuration` / `calculateSpawnDuration` (mirroring the Flow model) and update the TECH_SPEC formula accordingly.

---

## Acceptance Criteria

- [x] Owner has explicitly chosen Option A or Option B for density scaling.
- [x] Owner has explicitly chosen whether time-decay (`calculateDurationPercentage`) should apply to Flash.
- [x] `docs/PRD.md` §5.2 updated with unambiguous density + speed description.
- [x] `docs/TECH_SPEC.md` has a new §14 Flash Speed Model with all formulas, floor constants, and density rule.
- [x] `calculateDurationPercentage` in `FlashGameEngine` is either deleted (if unused) or wired in (if time-decay is desired) — not left as dead code.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [x] `FlashGameEngineTest` covers the chosen duration formulas (add missing cases if needed).

---

## Files to Create

(none)

## Files to Modify

- `docs/PRD.md` — §5.2: replace vague density/refresh sentence with explicit rules
- `docs/TECH_SPEC.md` — add §14 Flash Speed Model; renumber downstream sections
- `core/domain/src/main/java/com/maxot/seekandcatch/core/domain/engine/FlashGameEngine.kt` — remove or wire `calculateDurationPercentage`
- `core/domain/src/test/java/com/maxot/seekandcatch/core/domain/engine/FlashGameEngineTest.kt` — add/update formula coverage tests

---

## Notes

**Why `rowDuration` is reused as a base for Flash:** `GameParams.rowDuration` is described as "time for one row to scroll by, in ms" — it is a Flow concept. Flash reuses it as a timing baseline by applying multipliers (`×2×1.2` for flash, `×1.2×1.5` for spawn period). This is an implementation detail that TECH_SPEC §14 should call out explicitly so future difficulty tuning is not confused.

**Difficulty-to-`rowDuration` mapping** is not documented in TECH_SPEC for Flash (it is for Flow). If the mapping differs per mode, that should be captured.

**`calculateDurationPercentage` formula:** identical to Flow's §13 formula (`1 − coef²/100 − timeFactor`). If wired in, Flash would get an independent time-pressure ramp on top of coefficient pressure. This is a gameplay design question, not a technical one.

**No new Firestore collections, repositories, or modules** are required by this ticket.