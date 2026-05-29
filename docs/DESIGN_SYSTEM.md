<!-- Version: v1.0 · Last updated: 2026-05-19 · Status: Active -->

# Design System — Seek and Catch

---

## 1. Aesthetic Principles

1. **Arcade / retro pixel art.** Every visual element references 8-bit or 16-bit game aesthetics. Borders, buttons, and panels use hard pixel-stepped corners, not smooth Material3 rounded corners.
2. **Forest-green darkness.** The default theme is dark: deep forest-green backgrounds, yellow-green accents. Even the "light" theme stays in the green family — it is not white/grey.
3. **Maximum scannability.** Items on the game field must be identifiable in under 100ms. High contrast between shapes, colours, and background is non-negotiable.
4. **Minimalist information hierarchy.** Only game-critical data (score, health, coefficient, goal) is visible during play. Navigation chrome disappears.
5. **Press Start 2P everywhere.** A single pixel font (`press_start_2p`) is used for all text. No variable-weight fallback. This is a brand identity choice, not a stylistic option.

---

## 2. Color Palette

All tokens are defined in `core/designsystem/src/main/java/com/maxot/seekandcatch/core/designsystem/theme/Color.kt`.
The theme is applied via `SeekAndCatchTheme` in `Theme.kt`.

### Dark Theme (primary)

| Semantic Name | Token | Hex | Usage |
|---|---|---|---|
| Primary | `primaryDark` | `#D6D68D` | Button borders, outlines, active UI |
| On Primary | `onPrimaryDark` | `#1A3B20` | Text on primary-coloured surfaces |
| Primary Container | `primaryContainerDark` | `#BEBE7A` | Button fill |
| Background | `backgroundDark` | `#0E1A13` | Page background |
| Surface | `surfaceDark` | `#1A3B20` | Dialogs, cards |
| On Surface | `onSurfaceDark` | `#D6D68D` | Text on surface |
| Surface Variant | `surfaceVariantDark` | `#224F31` | Secondary card backgrounds |
| Secondary | `secondaryDark` | `#3D6D50` | List items, chips |
| Error | `errorDark` | `#9B2A2A` | Error states, wrong-tap feedback |
| Error Container | `errorContainerDark` | `#5D1A1A` | Error containers |
| Outline | `outlineDark` | `#D6D68D` | Pixel border middle ring |

### Light Theme (system-toggle)

| Semantic Name | Token | Hex | Usage |
|---|---|---|---|
| Primary | `primaryLight` | `#D6D68D` | Same yellow-green accent |
| Background | `backgroundLight` | `#1A3B20` | Lighter forest green |
| Surface | `surfaceLight` | `#254D30` | Card backgrounds |

### Special / Game Colours (not in MaterialTheme)

| Name | Variable | Hex | Usage |
|---|---|---|---|
| Gold | `gold` | `#FFD700` | 1st place leaderboard medal |
| Silver | `silver` | `#C0C0C0` | 2nd place leaderboard medal |
| Bronze | `bronze` | `#CD7F32` | 3rd place leaderboard medal |
| Red | `red` | `#DC0A0A` | Error indicator / health loss flash |

> Note: `LocalColorblindMode` is a `CompositionLocal<Boolean>` provided by `SeekAndCatchTheme`. All item-rendering Composables that use colour for identification should check this flag and use shape-only identification when true.

---

## 3. Typography

All styles use a single font family: **Press Start 2P** (`R.font.press_start_2p`).
Defined in `core/designsystem/src/main/java/com/maxot/seekandcatch/core/designsystem/theme/Type.kt` as `appTypography`.

| Style | Size | Line Height | Letter Spacing | Usage |
|---|---|---|---|---|
| `displayLarge` | 20sp | 24sp | 0.1sp | Major screen titles |
| `displayMedium` | 16sp | 20sp | 0.1sp | Section headings |
| `displaySmall` | 10sp | 14sp | 0.1sp | Small labels, captions |
| `titleLarge` | 18sp | 20sp | 0sp | Screen-level titles |
| `titleMedium` | 14sp | 18sp | 0.1sp | Panel section titles |
| `titleSmall` | 12sp | 16sp | 0.1sp | Sub-section headings |
| `bodyLarge` | 14sp | 18sp | 0.1sp | Primary body text |
| `bodyMedium` | 12sp | 16sp | 0.1sp | Secondary body text |

`pixelFont` is a standalone `FontFamily` exported for use in custom `Text()` calls that bypass typography slots.

> **Note:** All font weights in Press Start 2P are `FontWeight.Normal` — the font only has a single weight. `FontWeight.SemiBold` is declared in title styles but has no visual effect. Do not attempt to add bold variants.

---

## 4. Spacing Scale

No formal spacing system is currently defined in a single token file. The following values are used consistently across the codebase and should be treated as the de-facto scale:

| Token (recommended name) | Value | Usage |
|---|---|---|
| `spacing.xs` | 4dp | Tight icon padding |
| `spacing.sm` | 8dp | Component internal padding |
| `spacing.md` | 16dp | Standard content padding (PixelBorderBox default) |
| `spacing.lg` | 20dp | Card inner padding |
| `spacing.xl` | 40dp | Button horizontal padding (PixelButton default) |
| `spacing.xxl` | 50dp | Spacers between major sections |

> These values are currently hardcoded inline. They should be migrated to a `SpacingTokens` object in `core:designsystem` when a spacing refactor ticket is created.

---

## 5. Shapes

Defined in `core/designsystem/src/main/java/com/maxot/seekandcatch/core/designsystem/theme/Shape.kt` as `Shapes`.

- Standard Material3 `Shapes` object applied via `MaterialTheme(shapes = Shapes, ...)`.
- `RoundedTriangleShape.kt` — custom `Shape` for triangular item rendering using canvas path operations.
- Pixel borders are drawn via `drawPixelBorders()` in `PixelBorderBox.kt` — a Canvas drawing function that produces three concentric pixel-stepped rings (outer: dark green, middle: yellow-green, inner: dark green, fill: surface colour).

---

## 6. Component Catalog

All reusable Composables live in `core/designsystem/src/main/java/com/maxot/seekandcatch/core/designsystem/component/`.

### PixelButton
File: `component/PixelButton.kt`

A clickable button with pixel-art border styling via `drawPixelBorders`.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `modifier` | Modifier | Modifier | Standard modifier |
| `onClick` | `() -> Unit` | — | Click handler |
| `buttonColor` | Color | `MaterialTheme.colorScheme.primaryContainer` | Fill colour |
| `borderColor` | Color | `MaterialTheme.colorScheme.primary` | Border accent colour |
| `paddingValues` | PaddingValues | `PaddingValues(40.dp)` | Internal padding |
| `content` | `@Composable RowScope.() -> Unit` | — | Button content |

States: normal (clickable), no disabled state currently implemented.

### PixelBorderBox
File: `component/PixelBorderBox.kt`

A container with three-ring pixel border drawn via Canvas. Used for cards, panels, and info sections.

| Parameter | Type | Default |
|---|---|---|
| `modifier` | Modifier | Modifier |
| `outerBorderColor` | Color | `#1B3B24` |
| `middleBorderColor` | Color | `#D6D68D` |
| `innerBorderColor` | Color | `#1B3B24` |
| `backgroundColor` | Color | `#254D30` |
| `content` | `@Composable () -> Unit` | — |

Border layers: outer 4dp, middle 6dp, inner 4dp. Corners are pixel-stepped (not rounded).

### PixelFigureDrawer
File: `component/PixelFigureDrawer.kt`

Renders a game figure (circle, square, triangle) with a given colour. Used in game field layouts.

### PixelToggle
File: `component/PixelToggle.kt`

A pixel-styled toggle/switch for settings (sound, music, vibration, etc.).

### UserInfoPanel
File: `component/UserInfoPanel.kt`

Displays the current user's name/ID in the Account screen.

### PixelProgressBar
File: `component/PixelProgressBar.kt`

A horizontal progress bar with pixel-art border styling. Uses `drawPixelBorders` for the outer frame, with a pixel-snapped fill rectangle drawn on top of the track.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `progress` | `Float` (0f–1f) | — | Current fill level; caller manages animation |
| `modifier` | Modifier | Modifier | Standard modifier; caller sets width and height |
| `trackColor` | Color | `MaterialTheme.colorScheme.surfaceVariant` | Empty track background colour |
| `fillColor` | Color | `MaterialTheme.colorScheme.primary` | Fill colour |

Fill width is pixel-snapped (`(progress * innerWidth).toInt()`) to preserve the retro aesthetic. Animation (e.g. `animateFloatAsState`) is the caller's responsibility. Used in `SplashScreen` with a `tween(1500, LinearEasing)` driving 0→1 over the minimum splash duration.

### drawPixelBorders (utility function)
File: `component/PixelBorderBox.kt`

Standalone `DrawScope` extension used by `PixelBorderBox`, `PixelButton`, and `PixelProgressBar`. Call directly on a `Modifier.drawBehind {}` block or inside a `Canvas` composable for inline pixel border styling.

---

## 7. Navigation Philosophy

- **Single activity** (`MainActivity`).
- **Single `NavHost`** (`SeekCatchNavHost`) with `startDestination = SPLASH_ROUTE` (auto-advances to `GAME_MAIN_ROUTE`).
- **Top-level destinations** (bottom navigation): Game Selection, Leaderboard, Account.
- **Push (non-top-level):** FlowGameScreen, FlashGameScreen, GameResultScreen — pushed onto the back stack from Game Selection.
- **Modal:** Pause dialog (in-place overlay, not a navigation destination), Settings dialog (modal bottom sheet or dialog over the current screen).
- Navigation extension functions pattern: `navController.navigateToXxx(navOptions)` defined in each feature's `navigation/` package.
- `NavOptions` are passed through but default to `null` — callers provide pop-to or single-top behaviour when needed.

---

## 8. Screen Structure

Each screen is structured as:
1. A public `@Composable fun XxxScreen(viewModel = hiltViewModel(), ...)` — connects ViewModel and handles navigation callbacks.
2. A private `@Composable fun XxxScreenContent(...)` — pure UI, receives state and lambdas, has `@Preview`.
3. Layout sub-composables in `ui/layout/` for complex game-field sections.

---

## 9. Microcopy and Tone of Voice

**Approved patterns:**
- Short, imperative labels: "Start", "Restart", "Back", "Pause", "Resume".
- Direct status: "Score: 1420", "x2.5", "HP: 3".
- Mode/difficulty names in ALL CAPS matching enum names: "FLOW", "FLASH", "EASY", "NORMAL", "HARD".

**Forbidden patterns:**
- Encouragement or consolation copy ("Nice try!", "Keep going!").
- Multi-sentence instructions during gameplay.
- Ellipsis in button labels ("Loading...").
- Emoji in production UI.

---

## 10. Interaction Patterns

### Loading State
- `CircularProgressIndicator` from Material3 shown centred while data loads.
- Game screen: `isLoading = true` in UiState while game engine initialises.

### Error State
- Leaderboard offline: display "—" for rank. No error banner.
- Auth failure: silent (game proceeds without leaderboard submission).
- UI never shows an error screen that blocks navigation.

### Empty State
- Empty leaderboard: `LazyColumn` with no items → Composable should show a "No records yet" text message.
  <!-- inferred from code — verify with author -->

### Transitions
- Default Compose navigation transitions (no custom animations defined at NavHost level).
- Game mode carousel uses `SingleSelectionLazyRow` with smooth scrolling.
- Game field auto-scroll uses `animateScrollBy` with `LinearEasing` tween.

### Game Feedback Timing
- Correct tap: fragment particle animation + light haptic + sound — simultaneous.
- Miss: red flash overlay + shake + strong haptic + sound — simultaneous.
- `VisualFeedbackManager.triggerLifeWasted()` drives both the shake and red flash via `isLifeWasted: Boolean` in UiState.

---

## 11. Forbidden Visual Patterns

- Do not use `RoundedCornerShape` for game UI components — use pixel borders instead.
- Do not use dynamic colour (Material You / `dynamicDarkColorScheme`) — commented out in `Theme.kt` intentionally.
- Do not use serif or sans-serif fonts for game UI text — Press Start 2P only.
- Do not add `@Preview` functions with white/grey backgrounds — always wrap with `SeekAndCatchTheme`.
- Do not use `Modifier.background()` for card backgrounds without the pixel border wrapper — it breaks the visual language.

---

## 12. Implementation Notes

- **Theme object:** `MaterialTheme` via `SeekAndCatchTheme(darkTheme, isColorblindModeEnabled)` applied in `MainActivity`.
- **Color tokens:** `MaterialTheme.colorScheme.primary`, `.surface`, `.background`, etc.
- **Special colours:** `gold`, `silver`, `bronze`, `red` — accessed directly from the `theme` package import (not from `MaterialTheme`).
- **Colorblind mode:** `LocalColorblindMode.current` — read in any Composable that renders figure colours.
- **Component locations:**
  - Reusable components: `core/designsystem/src/.../component/`
  - Game field layouts: `feature/gameplay/src/.../ui/layout/`
  - Feature screens: `feature/*/src/.../ui/`
