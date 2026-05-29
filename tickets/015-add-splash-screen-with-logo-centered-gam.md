# Ticket 015 — Splash Screen with Logo, Game Name, and Loading Bar

**Status:** Done
**Depends on:** (none)

---

## Context

The app currently shows the default Android 12+ system splash (white/icon only, no branding). `MainActivity` already calls `installSplashScreen()`, so the system splash fires, but control immediately passes to the game selection screen with no transition. This ticket adds a custom in-app `SplashScreen` that shows the app logo centred, the game name in Press Start 2P, and an animated pixel-art loading bar that fills while the app initialises (reads settings from DataStore). Once both the minimum display time (1.5 s) has elapsed **and** `MainActivityUiState` transitions to `Success`, the splash auto-navigates to Game Selection and removes itself from the back stack.

---

## Task

### No new module — everything lives in `app`

Splash has no data dependencies and no ViewModel. The route constant and nav extension go in `app/src/main/java/com/maxot/seekandcatch/navigation/SplashNavigation.kt`; the composable goes in `app/src/main/java/com/maxot/seekandcatch/ui/SplashScreen.kt`.

**`SplashNavigation.kt`**
```
const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashScreen(onSplashComplete: () -> Unit)
```

**`SplashScreen.kt`** — `@Composable SplashScreen(isAppReady: () -> Boolean, onSplashComplete: () -> Unit)`

Layout (vertical centred, full-screen dark background = `backgroundDark`):
1. `Image` — `ic_launcher_foreground` drawable, ~120 dp, no tint override (keep existing pixel-art colours)
2. `Text` — "Seek & Catch" in `MaterialTheme.typography.displayLarge` (Press Start 2P 20 sp), colour `onSurfaceDark`
3. `PixelProgressBar` (see below) — width fills 80 % of screen, height 12 dp, bottom-centre area

`LaunchedEffect` logic:
- Record `startTime = System.currentTimeMillis()`
- Poll in a `while (!isAppReady() || elapsedMs < 1500)` loop with `delay(16)` ticks
- On exit → `onSplashComplete()`

### New component: `PixelProgressBar` in `core:designsystem`

```kotlin
@Composable
fun PixelProgressBar(
    progress: Float,        // 0f..1f
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    fillColor: Color = MaterialTheme.colorScheme.primary,
)
```

- Outer container uses `drawPixelBorders` (same 2 px pixel border as `PixelBorderBox`)
- Fill is a solid rectangle that grows left→right, snapped to integer pixel columns (no smooth animation artefacts)
- Animation: `animateFloatAsState` with `tween(1500, easing = LinearEasing)` targets `if (isAppReady()) 1f else 0.9f` — so it fills to 90 % during load and snaps to 100 % on ready

### Nav wiring changes

- `SeekCatchNavHost`: change `startDestination = SPLASH_ROUTE`, add `splashScreen(onSplashComplete = { navController.navigate(GAME_MAIN_ROUTE) { popUpTo(SPLASH_ROUTE) { inclusive = true } } })`
- Pass `isAppReady = { uiState.value is MainActivityUiState.Success }` down from `MainActivity` → `SeekAndCatchApp` → `SeekCatchNavHost` → `splashScreen`

---

## Acceptance Criteria

- [ ] App launches and shows the custom splash (not a blank screen or instant game selection jump)
- [ ] Logo (`ic_launcher_foreground`) is visible, centred horizontally and vertically (above the title)
- [ ] "Seek & Catch" title is rendered in Press Start 2P font
- [ ] `PixelProgressBar` animates from 0→100 % over ~1.5 s
- [ ] App navigates to Game Selection automatically; splash is not reachable via back button
- [ ] Minimum display time is ≥ 1.5 s even when DataStore loads in < 100 ms
- [ ] `SplashScreen.kt` and `PixelProgressBar.kt` each have at least one `@Preview`
- [ ] No Firestore imports anywhere in `feature:splash` or `core:designsystem` changes
- [ ] No business logic in Composables (timing logic lives in `LaunchedEffect`, not inline lambdas)
- [ ] Project builds without errors (`./gradlew build`)
- [ ] No new lint warnings introduced
- [ ] Passes check-arch (`./claude/skills/check-arch/check.sh` exits 0)

---

## Files to Create

- `app/src/main/java/com/maxot/seekandcatch/navigation/SplashNavigation.kt` — route constant + `NavGraphBuilder.splashScreen()` extension
- `app/src/main/java/com/maxot/seekandcatch/ui/SplashScreen.kt` — `SplashScreen` composable + preview
- `core/designsystem/src/main/java/com/maxot/seekandcatch/core/designsystem/component/PixelProgressBar.kt` — reusable pixel-art progress bar + preview

## Files to Modify

- `app/src/main/java/com/maxot/seekandcatch/navigation/SeekCatchNavHost.kt` — add `splashScreen()`, change `startDestination`, thread `isAppReady`
- `app/src/main/java/com/maxot/seekandcatch/ui/SeekAndCatchApp.kt` — accept and pass down `isAppReady` lambda
- `app/src/main/java/com/maxot/seekandcatch/MainActivity.kt` — derive `isAppReady` from `uiState` and pass to `SeekAndCatchApp`
- `docs/PRD.md` — add Screen 10.0 Splash Screen entry
- `docs/DESIGN_SYSTEM.md` — document `PixelProgressBar` component under Section 4

---

## Notes

**Doc updates required before implementation:**
- **PRD.md §10**: Add `### 10.0 Splash Screen` — shown once on cold start; auto-advances to Game Selection; not reachable via back navigation.
- **DESIGN_SYSTEM.md §4**: Add `PixelProgressBar` — parameters, colour token usage, animation spec.
- **TECH_SPEC.md**: Note that splash lives in the `app` module under `ui/` and `navigation/`; no new module needed.

**Design decisions:**
- No ViewModel — splash has no own data to fetch; `isAppReady` is passed in as a lambda from `MainActivity` where `uiState` already lives.
- `ic_launcher_foreground` is reused as the logo; no new asset needed for MVP.
- The pixel-snap fill on `PixelProgressBar` (integer columns) preserves the retro aesthetic — smooth sub-pixel animation would look out of place.
- The system `installSplashScreen()` call in `MainActivity` can remain; it covers the window-background flash before Compose renders — the in-app splash immediately follows it.
