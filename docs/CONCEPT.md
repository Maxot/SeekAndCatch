<!-- Version: v1.0 · Last updated: 2026-05-19 · Status: Active -->

# CONCEPT.md — Seek and Catch

## What It Is

Seek and Catch is a minimalist reflex-training mobile game built for Android. The player faces a scrolling or flashing grid of coloured geometric shapes and must tap only the items that match a single active goal — a colour or a shape — as fast and as accurately as possible. There are no levels to complete and no win condition: the game ends only when the player makes a wrong tap or runs out of health from missed correct items. Every session is a test of sustained attention, perceptual speed, and composure under escalating pressure.

---

## Core Principles

1. **One failure path only — wrong tap is instant death.** Any wrong tap ends the game immediately, without exception. This is non-negotiable and is never softened by difficulty, power-ups, or retries.
2. **Skill is expressed through endurance, not completion.** There are no win conditions. A long session is success. The score and coefficient are the only signals of mastery.
3. **Pressure must be earned, not imposed.** Speed and density scale with the player's own coefficient, not with an external clock or timer. The player controls how hard the game gets by playing well.
4. **The UI exists to not distract.** The game field is the focus. Everything else — health, coefficient, goal — is peripheral information that must never compete with the action.
5. **Offline-first.** Gameplay must function with no internet connection. Cloud sync (leaderboard) is best-effort and must never degrade gameplay.
6. **No monetisation, no ads, ever.** The product is the experience. No dark patterns, no interruptions, no paywalls.
7. **Two modes, one ruleset.** Flow (scrolling) and Flash (blinking grids) feel different but share identical scoring, health, coefficient, and failure logic. No mode gets special exceptions.

---

## Audience and Entry Points

**Primary audience:** Mobile gamers aged 0-99 who enjoy reflex and attention games (e.g. reaction trainers, colour-match games). Players who appreciate a pure, distraction-free skill loop without gacha or energy systems.

**Entry points:**
- Organic: word of mouth, Play Store discovery.
- The game requires no tutorial — the goal is always visible, and the first wrong tap teaches the rules.

---

## Tone and Aesthetic

The visual language is **retro pixel art**: deep forest-green backgrounds, yellow-green UI accents, and the "Press Start 2P" font throughout. The aesthetic is deliberately lo-fi and arcade-era — it signals that this is a skill game, not a casual time-killer. The palette is calm but high-contrast, optimised for fast visual scanning.

Sound and haptic design mirror the visual restraint: tight feedback sounds, no background noise in menus, pulsing music during gameplay that mutes on pause.

Tone of copy: **terse and direct**. No filler text, no encouragement, no apologies. Labels are single words or short phrases. The game speaks through action and feedback, not text.

---

## Economic Model

None. No monetisation, no in-app purchases, no ads. The app is a free release. Future sustainability is out of scope for the current roadmap.

---

## Roadmap Phases

**Phase 1 — MVP (current):** Core gameplay (Flow + Flash), difficulty selection, health and coefficient system, anonymous Firebase Auth, leaderboard (Firestore), settings (sound, music, vibration, dark mode, language, colorblind mode). Android only, portrait.

**Phase 2 — Post-MVP:** Player statistics and session history, online leaderboard expansion (pagination, filters), optional Google account linking to persist scores.

**Phase 3 — Future:** Thematic UI skins, power-ups (must not violate core principle 1), AI-generated gameplay insights, potentially iOS.

---

## Key Risks and Defences

| Risk | Defence |
|---|---|
| Firestore failure crashes gameplay | Leaderboard is async and non-blocking; game never awaits cloud results during play |
| Wrong-tap rule feels punishing to new players | No softening — onboarding happens through a 3-second countdown and visible goal |
| Coefficient formula too aggressive → game unwinnable past early sessions | Speed formula has a hard floor (`actualDurationPercentage ≥ 0.35`) defined in TECH_SPEC |
| State leak on Restart produces unfair games | Restart invariant enforced at engine level — full reset, no cached state allowed |
| Colorblind players cannot distinguish goal items | Colorblind mode in Settings; shapes are always distinct from colours |
