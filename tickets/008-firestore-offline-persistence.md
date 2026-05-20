# Ticket 008 — Enable Firestore Offline Persistence

**Status:** Draft
**Depends on:** (none)

---

## Context

The app makes no Firestore reads succeed offline. When a user launches the game without network:
- The leaderboard fetch hangs and eventually emits a `Failed` state.
- User profile data is unavailable.

Firestore SDK has built-in disk persistence that caches the last-fetched data locally and serves it while offline, then syncs when the network returns. Enabling it requires a one-line settings change. This gives users a usable experience on their second launch even without connectivity.

Reference: Firebase Firestore docs — Enable offline data; TECH_SPEC §16 Firebase Integration.

---

## Task

Enable Firestore offline persistence in the Hilt module that provides the `FirebaseFirestore` instance. If no such provider exists (the datasources currently call `Firebase.firestore` directly), add a singleton provider.

**Option A — if a Firestore provider already exists in `DataModule`:**
```kotlin
@Provides
@Singleton
fun provideFirestore(): FirebaseFirestore {
    val settings = firestoreSettings {
        isPersistenceEnabled = true
    }
    return Firebase.firestore.apply { firestoreSettings = settings }
}
```

**Option B — if datasources call `Firebase.firestore` directly (current state):**
1. Add the `@Provides` function above to `DataModule`.
2. Inject `FirebaseFirestore` into each datasource via `@Inject constructor(private val db: FirebaseFirestore)` instead of calling `Firebase.firestore` directly.
3. Update `DataModule` `@Binds` entries accordingly.

Do not use `FirebaseFirestoreSettings.Builder` (deprecated) — use the `firestoreSettings { }` DSL.

---

## Acceptance Criteria

- [ ] `isPersistenceEnabled = true` is set on the `FirebaseFirestore` instance used by all datasources.
- [ ] A `@Singleton`-scoped `FirebaseFirestore` is provided via Hilt (not recreated per datasource).
- [ ] No `Firebase.firestore` direct calls remain in datasource classes (they receive the instance via injection).
- [ ] Project builds without errors (`./gradlew build`).
- [ ] `check-arch` exits 0.
- [ ] Manual test: load leaderboard, enable airplane mode, kill and relaunch — leaderboard shows cached data instead of error state.

---

## Files to Modify

- `data/src/main/java/.../data/di/DataModule.kt` — add `provideFirestore()` with persistence enabled
- `data/src/main/java/.../data/firebase/datasource/LeaderboardFirestoreDataSource.kt` — inject `FirebaseFirestore`
- `data/src/main/java/.../data/firebase/datasource/UserFirestoreDataSource.kt` — inject `FirebaseFirestore` (if it exists)
- Any other datasource that calls `Firebase.firestore` directly — check with `grep -rn "Firebase.firestore" data/src --include="*.kt"`

---

## Notes

Firestore persistence stores data in an on-device SQLite database managed by the SDK. The cache size defaults to 100 MB which is fine for this app's data volume.

Enabling persistence only affects reads from Firestore. Writes are always queued and synced when online — this is Firestore's default behaviour and does not change.
