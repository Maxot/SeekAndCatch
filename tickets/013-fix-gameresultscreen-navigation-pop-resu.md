# Ticket 013 — Fix game navigation: pop game and result screens off back stack

**Status:** Done
**Depends on:** (none)

---

## Context

Two back-stack problems exist in the game flow, both leaving dead ViewModels alive:

**Problem 1 — game screen stays when navigating to result.**
`navigateToGameResult(score)` is called with no `NavOptions`, so the back stack becomes:
```
[GameSelection] → [FlowGame/FlashGame] → [GameResult]
```
The finished game screen is still alive underneath the result screen, holding
`FlowGameViewModel`/`FlashGameViewModel` in memory unnecessarily. Pressing back from
the result screen would also incorrectly return to a frozen game mid-session.

**Problem 2 — result screen stays when navigating to main.**
`toMainScreen` calls `navigateToGameSelection(null)` with no `NavOptions`, pushing a
new GameSelection entry without removing the result screen or the (already wrong) game
screen. The back stack ends up as:
```
[GameSelection] → [FlowGame/FlashGame] → [GameResult] → [GameSelection]  ← wrong
```
`GameResultViewModel` stays alive. Because it observes `settingsRepository.observeGameMode()`
and `observeDifficulty()`, any settings change on the new game selection screen
re-triggers `autoSubmitScore`, creating Firestore leaderboard documents with the wrong
mode/difficulty for the just-finished game's score.

The "Restart" path already handles problem 2 correctly via
`setPopUpTo(GAME_SELECTION_ROUTE, inclusive = false)`. This ticket fixes both problems
to produce the correct back stacks at every transition.

---

## Task

All changes are in **`feature/gameplay/.../navigation/GameNavigation.kt`**.

### Fix 1 — pop the game screen when navigating to result

`navigateToGameResult` is called from `FlowGameScreen` and `FlashGameScreen` via the
`toGameResultScreen` lambda wired in `GameNavigation.kt`. Add `NavOptions` that pop
the current game screen before pushing the result screen:

```kotlin
// FLOW_GAME_ROUTE composable
FlowGameScreen(
    toGameResultScreen = { score ->
        val navOptions = NavOptions.Builder()
            .setPopUpTo(FLOW_GAME_ROUTE, inclusive = true)
            .build()
        navigateToGameResult(score, navOptions)
    }
)

// FLASH_GAME_ROUTE composable
FlashGameScreen(
    toGameResultScreen = { score ->
        val navOptions = NavOptions.Builder()
            .setPopUpTo(FLASH_GAME_ROUTE, inclusive = true)
            .build()
        navigateToGameResult(score, navOptions)
    }
)
```

Also update `navigateToGameResult` signature to accept `NavOptions?`:
```kotlin
fun NavController.navigateToGameResult(score: Int, navOptions: NavOptions? = null) =
    navigate("game_result_route/$score", navOptions)
```
(It already has this signature — confirm it passes `navOptions` through.)

Desired back stack after game ends:
```
[GameSelection] → [GameResult]
```

### Fix 2 — pop the result screen when navigating to main

Change the `toMainScreen` lambda inside the `GAME_RESULT_ROUTE` composable from:

```kotlin
toMainScreen = { navigateToGameSelection(null) }
```

to:

```kotlin
toMainScreen = {
    val navOptions = NavOptions.Builder()
        .setPopUpTo(GAME_SELECTION_ROUTE, inclusive = true)
        .setLaunchSingleTop(true)
        .build()
    navigateToGameSelection(navOptions)
}
```

`setPopUpTo(GAME_SELECTION_ROUTE, inclusive = true)` removes the existing GameSelection
entry plus everything above it. `setLaunchSingleTop(true)` prevents a duplicate if
GameSelection is somehow already at the top.

Desired back stack after "to main screen":
```
[GameSelection]   ← all game/result ViewModels destroyed
```

The defensive `scoreSubmitted` flag and the `observeGameMode().first()` snapshot in
`GameResultViewModel` can stay as belt-and-suspenders but are no longer load-bearing.

---

## Acceptance Criteria

- [ ] After a game ends, back stack is `[GameSelection] → [GameResult]` — the game
      screen is gone (verify with Layout Inspector or `NavController.backQueue` log).
- [ ] `FlowGameViewModel` / `FlashGameViewModel` `onCleared()` is called as soon as
      the result screen appears.
- [ ] Pressing back from the result screen goes to `GameSelection`, not back into a
      frozen game screen.
- [ ] Tapping "to main screen" leaves exactly `[GameSelection]` in the back stack.
- [ ] `GameResultViewModel` `onCleared()` is called when "to main screen" is tapped.
- [ ] Changing mode/difficulty after returning to game selection does NOT trigger
      any Firestore writes from the previous game's result.
- [ ] The Android back button on game selection navigates to the app's top-level
      destination (not back into any game or result screen).
- [ ] "Restart" navigation behaviour is unchanged.
- [ ] Project builds without errors (`./gradlew build`)
- [ ] No Firestore imports in ViewModel or UI layer
- [ ] No business logic in Composables

---

## Files to Modify

- `feature/gameplay/src/main/java/com/maxot/seekandcatch/feature/gameplay/navigation/GameNavigation.kt`
  — add `NavOptions` with `setPopUpTo` to the `FLOW_GAME_ROUTE` and `FLASH_GAME_ROUTE`
    composables' `toGameResultScreen` lambdas; add `NavOptions` to the `toMainScreen`
    lambda in the `GAME_RESULT_ROUTE` composable

---

## Notes

No doc updates required — this is an internal navigation correctness fix with no
user-visible behaviour change beyond fixing the bug.
