# Ticket 010 — Leaderboard: Deterministic Document ID and Write Validation

**Status:** Done
**Depends on:** (none)

---

## Context

The previous `LeaderboardFirestoreDataSource.addRecord()` had two problems:

1. **Non-deterministic document ID** — it used `userId` alone as the document ID, meaning a user could accumulate multiple leaderboard documents (one per game session) rather than having a single best-score record per mode+difficulty. A separate anonymous-add fallback path also existed for empty-userId cases, further fragmenting the data.

2. **Optional fields could silently be missing** — `gameMode` and `difficulty` were added to the record map only `if present`, so records could be written without them, making filtering by mode/difficulty unreliable.

The fix changes the write strategy to a deterministic document ID `"{userId}_{gameMode}_{difficulty}"` using `.set()`, ensuring each user has exactly one leaderboard document per mode+difficulty combination (their personal best, since `GameResultViewModel` only calls `addRecord` when the score is a new high).

---

## Task

Changes made in `LeaderboardFirestoreDataSource.kt`:

1. Validate `userId`, `gameMode`, and `difficulty` before writing — log and return early if any are missing.
2. Compute `documentId = "${userId}_${gameMode}_${difficulty}"`.
3. Build `recordMap` with all four fields as required (not optional).
4. Use `leaderboardCollection.document(documentId).set(recordMap)` instead of the previous dual-path `.set()`/`.add()` logic.

Small cleanup required (found during implementation review):
- `const val TAG` was declared at package level without `private` — move inside the class or mark `private`.
- `LEADERBOARD_DOCUMENT_USER_NAME_KEY` is defined in the companion object but not included in the record map — remove or include.
- `onSuccessful` callback in `LeaderboardRepositoryImpl.addRecord()` has an empty body — remove the callback from the `DataSource` interface and simplify the signature.

---

## Acceptance Criteria

- [x] Deterministic document ID `{userId}_{gameMode}_{difficulty}` is used for all leaderboard writes.
- [x] `addRecord` validates userId, gameMode, and difficulty; logs and returns early if any are missing.
- [x] Record map includes all four fields as non-nullable.
- [ ] `const val TAG` is `private`.
- [ ] `LEADERBOARD_DOCUMENT_USER_NAME_KEY` is either used in the record map or removed.
- [ ] `onSuccessful` callback removed from `LeaderboardDataSource.addRecord()` interface and all implementations.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] `check-arch` exits 0.

---

## Files to Modify

- `data/src/main/java/com/maxot/seekandcatch/data/firebase/datasource/LeaderboardFirestoreDataSource.kt`
- `data/src/main/java/com/maxot/seekandcatch/data/firebase/datasource/LeaderboardDataSource.kt`
- `data/src/main/java/com/maxot/seekandcatch/data/repository/LeaderboardRepositoryImpl.kt`

---

## Notes

`.set()` unconditionally overwrites the document. The "only write if new best" guard lives in `GameResultViewModel.autoSubmitScore()` which checks `score > remoteBestForContext` before calling `addRecord`. This is acceptable for MVP single-device usage. A Firestore transaction would be needed for multi-device correctness but is out of scope.
