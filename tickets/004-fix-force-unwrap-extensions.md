# Ticket 004 — Fix Force-Unwrap (!!) in moveAndScale Modifier

**Status:** Draft
**Depends on:** (none)

---

## Context

`feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/Extensions.kt` contains a `moveAndScale()` Modifier extension that uses `currentCoordinates!!` in three places (lines ~92, 100, 107–108). `currentCoordinates` is a `var` backed by `mutableStateOf`, so Kotlin cannot smart-cast it — the null checks in the `if` conditions do not suppress the `!!`, meaning a `NullPointerException` is possible if the state changes between the condition check and the dereference (e.g. during recomposition).

The fix is to snapshot `currentCoordinates` into a local `val` at the top of each `animateFloatAsState` target lambda so Kotlin can smart-cast it safely.

---

## Task

In `Extensions.kt`, inside `moveAndScale()`, replace the three `animateFloatAsState` `targetValue` lambdas:

**Before (translationX, ~line 88):**
```kotlin
targetValue = if (isAtTarget && targetCoordinates != null && currentCoordinates != null) {
    targetCoordinates.positionInRoot().x - currentCoordinates!!.positionInRoot().x
} else 0f,
```

**After:**
```kotlin
targetValue = run {
    val coords = currentCoordinates
    if (isAtTarget && targetCoordinates != null && coords != null)
        targetCoordinates.positionInRoot().x - coords.positionInRoot().x
    else 0f
},
```

Apply the same pattern to `translationY` and `scale`:

```kotlin
// translationY
targetValue = run {
    val coords = currentCoordinates
    if (isAtTarget && targetCoordinates != null && coords != null)
        targetCoordinates.positionInRoot().y - coords.positionInRoot().y
    else 0f
},

// scale
targetValue = run {
    val coords = currentCoordinates
    if (isAtTarget && targetCoordinates != null && coords != null && coords.size.toSize().width > 0)
        targetCoordinates.size.toSize().width / coords.size.toSize().width
    else 1f
},
```

No other changes needed.

---

## Acceptance Criteria

- [ ] No `!!` operator remains in `Extensions.kt`.
- [ ] `moveAndScale()` modifier behaves identically at runtime — animation targets are unchanged.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] No new lint warnings introduced.
- [ ] `check-arch` exits 0.
- [ ] No unit tests needed (pure null-safety refactor of a Compose modifier).

---

## Files to Modify

- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/Extensions.kt` — replace `!!` with local `val` + smart cast in all three `animateFloatAsState` blocks

---

## Notes

The `!!` on `currentCoordinates` is not actually guarded by the surrounding `if` because `currentCoordinates` is a `var` captured in a composable lambda — Kotlin does not allow smart casting of mutable vars. The `run { val coords = ... }` pattern is idiomatic Kotlin for this situation.
