Technical Specification v1.2
Date: 26.01.2026
Status: Active / Source of Truth
Derived from: Codebase + PRD + Jira Tickets

1. Scope & Goals
   This document defines authoritative gameplay rules, state transitions, scoring, health logic, leaderboard behavior, and UX guarantees for SeekAndCatch.
   Anything not specified here:
- must be considered undefined
- must not be changed implicitly
- requires spec update before implementation

2. Game Modes
2.1 Supported Modes
- FLASH
- FLOW (with randomized scrolling direction)

3. Game State Machine
3.1 Shared States
- Idle
- Created
- Countdown (3s)
- Started
- Paused
- Finished

3.2 State Rules
- Countdown
    - Goals are generated once
    - No input accepted
    - Countdown sound plays
- Started
    - Input enabled
    - Timers active
- Paused
    - Timers frozen
    - Animations stopped
    - Input disabled
- Finished
    - No further mutations allowed
    - Result snapshot created

4. Goal System
- Goal type:
    - Color-based (e.g. all red)
    - Shape-based (e.g. all triangles)
- Goal changes after a fixed level duration (defined in GameParams)
- Goal conflicts are forbidden by generator

5. Grid & Item Rules
5.1 Grid Size by Difficulty
| Difficulty | Items per Row |
| :--- | :--- |
| Easy | 3 |
| Normal | 4 |
| Hard | 5 |

5.2 Distribution
- Random placement
- At least one valid goal item guaranteed
- No conflicting goals

6. Health & Failure Logic (Unified)
This section overrides existing inconsistent behavior

6.1 Initial Health
| Difficulty | HP |
| :--- | :--- |
| Easy | 5 |
| Normal | 3 |
| Hard | 1 |

6.2 Failure Rules (ALL MODES)
| Event | Result |
| :--- | :--- |
| Wrong tap | Immediate Game Over |
| Missed correct item | Coefficient drop |
| Missed item + coefficient == x1 | −1 HP |
| HP reaches 0 | Game Over |

❗ No other instant-death paths allowed

6.3 Health Recovery
- Triggered after passing N items without a miss
| Difficulty | Items Required |
| :--- | :--- |
| Easy | 25 |
| Normal | 50 |
| Hard | 100 |

7. Coefficient System
7.1 Base Rules
- Starts at x1
- Multiplies score
- Influences speed(scroll for flow, set changes for flash)

7.2 Increase Logic
- Increased by correct tap
- Increment value configurable (coefficientStep)

7.3 Decrease Logic
- Missed correct item: `newCoefficient = max(1.0, current / 2)`
❗ No coefficient drop sound

8. Scoring
`score += basePoints * coefficient`
| Difficulty | Base Points |
| :--- | :--- |
| Easy | 10 |
| Normal | 15 |
| Hard | 20 |

9. FLOW Speed Model
9.1 Formula
`actualDurationPercentage = 1.0 - coefPercentage - timePercentage`
Where:
- `coefPercentage = (coefficient² / 100)`
- `timePercentage = ((seconds / 30) * 5) / 100`

9.2 Clamp
`min(actualDurationPercentage) = 0.35`

10. Audio & Haptics
10.1 Sounds
- Countdown start (3s)
- Correct tap
- Wrong tap
- Game over
- New best score

10.2 Music
- Background loop per mode
- Muted on pause

10.3 Vibration
- Correct tap (light)
- Wrong tap / Game over (strong)
- Patterns configurable

11. Result Screen
11.1 Display
- Final Score
- Best Score (local + remote)
- Leaderboard Rank
- “Restart” CTA
- “To Main” CTA

11.2 Restart Rules
- Same mode + difficulty
- Full state reset
- Countdown replayed
- No cached state allowed

12. Leaderboard System (Firebase)
12.1 Auth
- Auto anonymous login on first launch
- Optional Google account linking
- User ID remains stable

12.2 Firestore Model
`leaderboard/{userId}:`
- userId
- userName
- score
- gameMode
- difficulty

12.3 Submission Rules
- Submit only if score > previous best
- Offline → queue or skip silently

12.4 Rank Fetch
- Rank fetched after submission
- Offline → display “—”
- Failure must never crash UI

13. Pause UX Guarantees
- Timers stopped
- Animations frozen
- Input disabled
- Resume restores exact state

14. Non-Goals (Explicit)
- Multiplayer
- Cheating protection
- Server-side validation

15. Invariants (Must Never Break)
- Wrong tap = instant game over
- Goals do not change mid-game
- Result screen always reachable
- Restart never leaks state
- Leaderboard failure never crashes app
