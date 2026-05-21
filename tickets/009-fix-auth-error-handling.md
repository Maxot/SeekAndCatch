# Ticket 009 — Fix Silent Error Swallowing in AuthRepositoryImpl

**Status:** Done
**Depends on:** (none)

---

## Context

`AuthRepositoryImpl.getUserId()` catches all exceptions and returns an empty string `""` without logging the error to Crashlytics. This means:

1. Auth failures are invisible in production — they never appear in the Crashlytics dashboard.
2. Callers receive `""` and silently write leaderboard records with an empty userId or fail to load the user profile with no trace.
3. Debugging auth regressions requires guessing — there is no signal in the logs.

The fix is to log the exception to Crashlytics before returning the fallback, so auth failures are visible and actionable in production.

Reference: TECH_SPEC §16 Firebase Integration; CLAUDE.md "What Never To Do" rule 5.

---

## Task

In `data/src/main/java/com/maxot/seekandcatch/data/repository/AuthRepositoryImpl.kt`, update `getUserId()`:

```kotlin
override suspend fun getUserId(): String {
    return try {
        val result = firebaseAuthDataSource.getOrCreateUser()?.uid
        result ?: throw IllegalStateException("UID null after anonymous sign-in")
    } catch (e: Exception) {
        Firebase.crashlytics.recordException(e)
        Log.w(TAG, "getUserId failed", e)
        ""
    }
}
```

Also add a `TAG` constant at the top of the file if not already present:
```kotlin
private const val TAG = "AuthRepositoryImpl"
```

Add the Crashlytics import:
```kotlin
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
```

Note: Crashlytics is already in the project (`firebase-crashlytics` in BOM). No new dependency needed.

---

## Acceptance Criteria

- [ ] Auth exceptions are recorded with `Firebase.crashlytics.recordException(e)` before returning `""`.
- [ ] `Log.w(TAG, ...)` call present for local debugging.
- [ ] `TAG` constant defined in `AuthRepositoryImpl`.
- [ ] No Firestore or Firebase Auth imports added outside the `data` layer.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] `check-arch` exits 0.
- [ ] No unit tests required (logging side-effect; the catch branch is infrastructure, not business logic).

---

## Files to Modify

- `data/src/main/java/com/maxot/seekandcatch/data/repository/AuthRepositoryImpl.kt` — add Crashlytics logging in the catch block

---

## Notes

Do not change the return type or the fallback value `""` — callers already handle empty string as "not authenticated". This is a pure observability improvement, not a behaviour change.
