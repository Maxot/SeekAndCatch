# Ticket 005 — Add Sign-Out and Account Deletion

**Status:** Draft
**Depends on:** (none)

---

## Context

The app uses anonymous Firebase Auth. There is currently no way for a user to sign out or delete their account and data. Google Play requires that apps which collect user data (even anonymously) provide an in-app mechanism to delete that data. The EU GDPR and Apple/Google store policies mandate this for any app that creates a persistent user identity.

Currently `AuthUseCase` only has `autoRegisterIfNeeded()`. The `AccountScreen` shows user info but has no sign-out or delete option.

Reference: Google Play policy — Data deletion requirements (2023); PRD §10.4 Account Screen.

---

## Task

### 1. `FirebaseAuthDataSource` — add sign-out and delete methods

```kotlin
suspend fun signOut()
suspend fun deleteAccount(): Boolean  // returns false if deletion fails
```

Implementation:
```kotlin
override suspend fun signOut() {
    Firebase.auth.signOut()
}

override suspend fun deleteAccount(): Boolean = try {
    Firebase.auth.currentUser?.delete()?.await()
    true
} catch (e: Exception) {
    Log.w(TAG, "Failed to delete account", e)
    false
}
```

### 2. `AuthRepository` interface + `AuthRepositoryImpl` — expose the new methods

```kotlin
suspend fun signOut()
suspend fun deleteAccount(): Boolean
```

`AuthRepositoryImpl` delegates directly to `FirebaseAuthDataSource`.

### 3. `UserRepository` + `UserRepositoryImpl` — add `deleteUser(userId: String)`

When an account is deleted, the Firestore `users/{uid}` document must also be deleted.

### 4. `AuthUseCase` — add `deleteAccountAndData()` and `signOut()`

```kotlin
suspend fun signOut() {
    authRepository.signOut()
}

suspend fun deleteAccountAndData() {
    val userId = authRepository.getUserId()
    if (userId.isNotEmpty()) {
        userRepository.deleteUser(userId)
    }
    authRepository.deleteAccount()
}
```

### 5. `AccountViewModel` — add `signOut()` and `deleteAccount()` actions

Wire up to the use case. After deletion, emit a navigation event to return the user to the game selection screen (or restart the anonymous auth flow via `autoRegisterIfNeeded()`).

### 6. `AccountScreen` — add UI buttons

- "Sign Out" button — calls `viewModel.signOut()`
- "Delete Account" button — shows a confirmation dialog before calling `viewModel.deleteAccount()`

Use `PixelButton` from `core:designsystem`. Strings go in `strings.xml`.

The confirmation dialog must clearly state: "This will permanently delete your account and all your scores."

---

## Acceptance Criteria

- [ ] User can sign out from `AccountScreen`.
- [ ] User can delete their account from `AccountScreen` after confirming in a dialog.
- [ ] On account deletion, the Firestore `users/{uid}` document is deleted.
- [ ] On account deletion, the app re-registers anonymously (or navigates to a blank state cleanly — no crash).
- [ ] No Firebase Auth or Firestore imports outside the `data` layer.
- [ ] Confirmation dialog shown before deletion — no accidental deletes.
- [ ] All new strings are in `strings.xml`.
- [ ] `check-arch` exits 0.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] Unit tests for `AccountViewModel`: sign-out calls use case, delete-account calls use case, error during deletion does not crash.

---

## Files to Modify

- `data/src/main/java/.../data/firebase/datasource/auth/FirebaseAuthDataSource.kt` — add `signOut()`, `deleteAccount()`
- `data/src/main/java/.../data/repository/AuthRepository.kt` — add interface methods
- `data/src/main/java/.../data/repository/AuthRepositoryImpl.kt` — implement
- `data/src/main/java/.../data/repository/UserRepository.kt` — add `deleteUser()`
- `data/src/main/java/.../data/repository/UserRepositoryImpl.kt` — implement
- `core/domain/src/main/java/.../core/domain/AuthUseCase.kt` — add `signOut()`, `deleteAccountAndData()`
- `feature/account/src/main/java/.../feature/account/AccountViewModel.kt` — add actions
- `feature/account/src/main/java/.../feature/account/ui/AccountScreen.kt` — add buttons + confirmation dialog

## Files to Create

- `feature/account/src/test/java/.../feature/account/AccountViewModelTest.kt`

---

## Notes

After `signOut()` or `deleteAccount()`, call `autoRegisterIfNeeded()` immediately to re-establish a new anonymous session. This keeps the game playable without requiring a login screen.

Leaderboard scores are keyed by `userId`. When a user deletes their account, their leaderboard entries are NOT deleted in this ticket — that is out of scope for MVP per PRD §13.
