<!-- Version: v1.2 · Last updated: 2026-05-19 · Status: Active -->

# Product Requirements Document — Seek and Catch

**Project:** Seek and Catch
**Owner:** Product / Project Manager
**Version:** 1.2
**Date:** January 26, 2026

---

## 1. Executive Summary

Seek and Catch is a fast-paced reflex-based mobile game focused on endurance and precision. The player interacts with a grid of items and must continuously select items that match the current goal. The game has no fixed endpoint — the player plays as long as they can survive, while speed and pressure scale with performance.

The game supports two distinct gameplay modes: **Flow** and **Flash**, which differ in how items appear and move.

---

## 2. Product Goal

Enable players to survive as long as possible and achieve the highest possible score through fast reactions, attention, and consistency.

- There are no win conditions — only eventual failure.
- Session duration is player-determined (ends only on failure).
- Difficulty is fixed per session; speed scales continuously via coefficient.
- Skill mastery is expressed via high coefficient and long survival time.

**Success Metrics (MVP):**
- Primary metric: Score
- No retention, monetization, or social KPIs are tracked in MVP.

---

## 3. Key Terms & Definitions

| Term | Description |
|---|---|
| Game Field | A grid where items appear or move |
| Item / Figure | A shape (circle, square, triangle) with a colour |
| Goal | A rule defining which items are correct (e.g. "all red" or "all triangles") |
| Right Item | Any item matching the current goal |
| Wrong Item | Any item not matching the goal |
| Score | Points gained from selecting right items |
| Speed | Movement or refresh rate of items |
| Coefficient | Continuous multiplier affecting score and speed |
| Health | Player life; consumed when missed items reduce coefficient below x1 |
| Difficulty | Defines grid size, item density, and starting health |
| Game Mode | Flow or Flash |

---

## 4. Entities and Data Model

### Figure
- `id: Int` — unique index within current set
- `shape: FigureType` — CIRCLE, SQUARE, TRIANGLE
- `color: Color` — the item's colour
- Generated per-round; not persisted.

### Goal
- `type: GoalType` — COLOR or SHAPE
- `value: Any` — the target colour or shape enum value
- One active goal at a time; generated once at game start; never changes mid-session.

### GameDifficulty (enum)
- EASY: rowWidth=3, lifeCount=5, scorePoint=10, coefficientStep=0.25, itemsToRecoverLife=25
- NORMAL: rowWidth=4, lifeCount=3, scorePoint=15, coefficientStep=0.20, itemsToRecoverLife=50
- HARD: rowWidth=5, lifeCount=1, scorePoint=20, coefficientStep=0.10, itemsToRecoverLife=100

### GameMode (enum)
- FLOW — scrolling grid
- FLASH — blinking grid

### LeaderboardRecord (Firestore document: `leaderboard/{userId}`)
- `userId: String` — Firebase anonymous UID (document key)
- `userName: String?` — display name if linked
- `score: Int` — final score
- `gameMode: String` — GameMode enum name
- `difficulty: String` — GameDifficulty enum name

### UserConfig
- `isDarkTheme: Boolean`
- `isSoundEnabled: Boolean`
- `isMusicEnabled: Boolean`
- `isVibrationEnabled: Boolean`
- `isColorblindModeEnabled: Boolean`
- `language: String`
- Persisted in DataStore Preferences.

---

## 5. Game Modes

### 5.1 Flow Mode
- Items auto-scroll continuously through the screen in a random direction (up or down, chosen at game start).
- Speed increases proportionally to the coefficient.
- Player reacts to moving items in real time.

### 5.2 Flash Mode
- Items appear at random positions on a `gridWidth × gridWidth` grid.
- Each cycle, `visibleAtOnce = max(1, gridWidth − 1)` items are shown simultaneously, guaranteed to include at least one goal-matching item.
- Flash display duration and spawn period both decrease as coefficient rises (see TECH_SPEC Flash Speed Model).
- Density (items per flash) depends on difficulty only; it does not scale with coefficient.
- No continuous scrolling.

Both modes share the same scoring, coefficient, and health rules.

---

## 6. Core Gameplay Rules

**Critical:** A wrong tap ends the game immediately. A missed right item reduces the coefficient and may consume health.

### 6.1 Game Start
1. Player selects Game Mode (Flow / Flash) and Difficulty (Easy / Normal / Hard) on the Game Selection screen.
2. A 3-second countdown plays before gameplay begins.
3. During countdown: goal is generated, no input is accepted.
4. Coefficient starts at x1. Health is set by difficulty.

### 6.2 Goals
- Type: Color-based (e.g. all red) or Shape-based (e.g. all triangles).
- Goals never conflict.
- Only one active goal at a time.
- Goals do not change mid-session.
- At least one valid goal item is guaranteed to be visible at all times.

### 6.3 Correct Tap
- Increases score: `score += scorePoint × coefficient`
- Increases coefficient: `coefficient += coefficientStep`
- Speed scales proportionally to coefficient.
- No upper limit on coefficient.
- Visual: item breaks into fragments that fly from centre and fade.
- Haptic: light vibration.
- Audio: correct-tap sound.

### 6.4 Wrong Tap (Miss Click)
- **Immediately ends the game (instant Game Over).**
- No coefficient or health reduction applied — just game over.
- This rule is consistent across all game modes and difficulties.

### 6.5 Missed Right Item
- Reduces coefficient: `newCoefficient = max(1.0, current / 2)`
- If coefficient is already at x1 and a miss occurs → −1 HP.
- Visual: red flash overlay on Game Info Panel + shake animation.
- Haptic: strong vibration.
- Audio: miss sound (no separate coefficient-drop sound).

### 6.6 Health System
- Starting health depends on difficulty (Easy=5, Normal=3, Hard=1).
- Max health is always 5 regardless of difficulty.
- Health is only reduced by missed right items, never by wrong taps.
- **Health recovery:** After passing N items without a miss, regain 1 HP.
  - Easy: 25 items, Normal: 50 items, Hard: 100 items.
- Health reaching zero → Game Over.

---

## 7. Difficulty Rules

### Grid Size
| Difficulty | Grid Columns |
|---|---|
| Easy | 3 |
| Normal | 4 |
| Hard | 5 |

### Starting Health
| Difficulty | Starting Health |
|---|---|
| Easy | 5 HP |
| Normal | 3 HP |
| Hard | 1 HP |

### Base Score Points
| Difficulty | Base Points |
|---|---|
| Easy | 10 |
| Normal | 15 |
| Hard | 20 |

---

## 8. Speed Model (Flow Mode)

```
actualDurationPercentage = 1.0 - coefPercentage - timePercentage
coefPercentage = (coefficient² / 100)
timePercentage = ((seconds / 30) × 5) / 100
min(actualDurationPercentage) = 0.35  ← hard floor
```

---

## 9. Game Over Flow

1. Gameplay stops immediately.
2. Player is navigated to the Result Screen.
3. Final score is saved locally.
4. Final score is uploaded to Firebase leaderboard (only if score > previous best).
5. If offline → skip silently, never block or crash.

---

## 10. Screens

### 10.1 Game Selection Screen (Main / Start)
- Select Game Mode (Flow / Flash) via animated carousel with live previews.
- Select Difficulty (Easy / Normal / Hard).
- Start Game button.
- Navigation to Leaderboard and Account via bottom navigation bar.

### 10.2 Flow Game Screen
- Scrolling game field (lazy grid).
- Current goal (always visible).
- Score, Coefficient indicator, Health indicator.
- Pause button → Pause Dialog.

### 10.3 Flash Game Screen
- Grid of blinking items.
- Current goal, Score, Coefficient, Health.
- Pause button → Pause Dialog.

### 10.4 Game Result Screen
- Final score.
- Best score (local + remote).
- Leaderboard position (if online; "—" if offline or request failed).
- Restart CTA (same mode + difficulty, full state reset, countdown replayed).
- Back to Main Menu CTA.

### 10.5 Leaderboard Screen
- Filterable list of scores.
- Filter by game mode and difficulty via chips.
- Medal colours for top 3 (gold, silver, bronze).

### 10.6 Account Screen
- User info panel.
- Anonymous user management.
- Optional Google account linking (post-MVP).

### 10.7 Settings Dialog
- Sound effects on/off.
- Music on/off.
- Vibration on/off.
- Dark theme on/off.
- Language selection.
- Colorblind mode on/off.

---

## 11. Audio & Haptics

### Sound Effects
- Countdown start (3s before game).
- Correct tap.
- Wrong tap / Game Over.
- Missed item (health or coefficient loss).
- New best score.

### Music
- Background loop per game mode.
- Muted on pause.
- Separate music or silence on menus.

### Vibration
- Light: correct tap.
- Strong: wrong tap, game over, health/coefficient loss.
- All configurable via Settings.

### Visual Feedback
- **Shake animation:** triggered on health or coefficient loss.
- **Red flash overlay:** triggered on health or coefficient loss, shown on Game Info Panel.
- **Fragment particles:** item breaks into fragments on correct tap.

---

## 12. Non-Functional Requirements

- Platform: Android (portrait only).
- Minimum SDK: 26, Target SDK: 35.
- Target FPS: 60.
- Offline gameplay with cloud sync.
- Firebase used for leaderboard storage (Firestore) and anonymous Auth.
- Optional Google account linking.
- No ads, no monetisation.
- Supports dark/light theme and colorblind mode.
- Locale-aware (language configurable in settings).

---

## 13. Out of Scope (MVP)

- Power-ups
- UI skins / themes
- Multiplayer
- Social sharing
- Paid player accounts
- Monetisation of any kind
- AI-generated questions or insights
- Cheating protection / server-side validation
- Server-side score validation

---

## 14. Future Enhancements (Post-MVP)

- Player statistics and session history
- Online leaderboard expansion (pagination, more filters)
- Google account linking for persistent cross-device identity
- Thematic UI skins
- Power-ups (must not compromise the instant-death rule)
- AI-generated gameplay insights
- iOS port

---

## 15. Non-Goals

- This is not a multiplayer game.
- This is not a puzzle game — goal type never changes mid-session.
- Seek and Catch does not teach or explain rules through text tutorials.

---

## 16. Invariants (Must Never Break)

- Wrong tap = instant game over, no exceptions.
- Goal remains fixed for the entire session.
- Result screen is always reachable after game over.
- Restart never leaks previous game state.
- Leaderboard failure never crashes the app.
- Coefficient floor is x1 — it never goes below 1.
