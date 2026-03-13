Product Requirements Document (PRD)
Project Name: Seek and Catch
Owner: Product / Project Manager
Version: 1.2
Date: January 26, 2026

1. Executive Summary
Seek and Catch is a fast-paced reflex-based mobile game focused on endurance and precision. The player interacts with a grid of items and must continuously select items that match the current goal. The game has no fixed endpoint — the player plays as long as they can survive, while speed and pressure scale with performance.
The game supports two distinct gameplay modes: Flow and Flash, which differ in how items appear and move.

2. Product Goal
Enable players to survive as long as possible and achieve the highest possible score through fast reactions, attention, and consistency.
There are no win conditions — only eventual failure.

3. Success Metrics
Primary metric: Score
Session duration is player-determined (ends only on failure)
Difficulty is fixed per session; speed scales continuously via coefficient
Skill mastery is expressed via high coefficient and long survival time
No retention, monetization, or social KPIs are tracked in MVP.

4. Key Terms & Definitions
| Term | Description |
| :--- | :--- |
| Game Field | A grid where items appear or move |
| Item | A shape (circle, square, triangle) with a color |
| Goal | A rule defining which items are correct (e.g. “all red” or “all triangles”) |
| Right Item | Any item matching the current goal |
| Wrong Item | Any item not matching the goal |
| Score | Points gained from selecting right items |
| Speed | Movement or refresh rate of items |
| Coefficient | Continuous multiplier affecting score and speed |
| Health | Player life; consumed on mistakes when coefficient is minimal |
| Difficulty | Defines grid size, item density, and starting health |
| Game Mode | Flow or Flash |

5. Game Modes
5.1 Flow Mode
Items are auto-scrolled continuously through the screen in random direction (up or down)
Speed increases proportionally to the coefficient
Player reacts to moving items in real time

5.2 Flash Mode
Items appear instantly at random positions on the grid
After a short interval, items are replaced with a new set
Refresh rate and density depend on difficulty and coefficient
No continuous scrolling

Both modes share the same scoring, coefficient, and health rules.

6. Core Gameplay Rules
Important: A wrong tap ends the game immediately. A missed right item reduces coefficient and may consume health.

6.1 Game Start
Player selects:
Game Mode (Flow / Flash)
Difficulty (Easy / Normal / Hard)
Initial speed is defined by game mode
Coefficient starts at x1
Health is set based on difficulty
First goal is generated

6.2 Goals
Goal type:
Color-based (e.g. all red items)
Shape-based (e.g. all triangles)
Goals never conflict
Only one active goal at a time
Goal changes after a fixed level duration

6.3 Correct Tap
Increases score
Increases coefficient based on straight correct tap count
Required number of consecutive correct taps depends on difficulty
Speed scales proportionally to coefficient
There is no upper limit on coefficient.

6.4 Wrong Tap (Miss Click)
Immediately ends the game (instant Game Over)
No coefficient or health reduction is applied
This rule is consistent across all game modes.

6.5 Missed Right Item
Reduces coefficient
If coefficient reaches x1 and a miss occurs → health is reduced
If health reaches zero → Game Over

6.6 Health System
Starting health depends on difficulty
Health is only reduced by missed right items, never by wrong taps
Health can be recovered by passing a defined number of items without misses
When health reaches zero → Game Over

7. Difficulty Rules
7.1 Grid Size
| Difficulty | Grid Columns |
| :--- | :--- |
| Easy | 3 |
| Normal | 4 |
| Hard | 5 |

7.2 Starting Health
| Difficulty | Starting Health |
| :--- | :--- |
| Easy | High |
| Normal | Medium |
| Hard | Low |
(Exact numeric values are defined in the Technical Specification.)

8. Game Over Flow
Triggered when health reaches zero
Gameplay stops immediately
Player is taken to the Result Screen
Final score is saved locally
Final score is uploaded to Firebase leaderboard

9. Screens
9.1 Main Screen
Start Game
Select Game Mode
Select Difficulty
Scores
Settings

9.2 Game Screen
Game field (grid)
Current goal (always visible)
Score
Coefficient indicator
Health indicator
Pause button

9.3 Result Screen
Final score
Best score
Leaderboard position
Restart game
Back to Main Menu

9.4 Settings Screen
Sound effects on/off
Music on/off
Vibration on/off
Dark theme on/off
Language selection
Colorblind mode

10. Audio & Haptics
10.1 Sound Effects
Correct tap
Wrong tap (game over)
Missed item (coefficient / health loss)
Game start countdown sound (3 seconds before gameplay begins)

10.2 Music
Background music during gameplay
Separate music or silence on menus

10.3 Vibration
Light vibration on correct tap
Strong vibration on missed item or game over
Configurable via Settings

11. Non-Functional Requirements
Platform: Android
Orientation: Portrait only
Target FPS: 60
Offline gameplay with cloud sync
Firebase used for leaderboard storage
Anonymous auto-authentication on first launch
Ability to link anonymous account with Google
No ads
No monetization

12. Out of Scope (MVP)
Power-ups
UI skins
Multiplayer
Social sharing
Player accounts
Monetization
AI-generated questions or insights

13. Future Enhancements (Post-MVP)
AI-generated insights/questions by category
Online leaderboard expansion
Player statistics and history
Thematic UI skins
Power-ups
