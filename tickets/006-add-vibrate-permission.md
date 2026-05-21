# Ticket 006 — Add VIBRATE Permission to AndroidManifest

**Status:** Done
**Depends on:** (none)

---

## Context

The app has a vibration toggle in `SettingsDialog` (`isVibrationEnabled`), meaning vibration is a supported and user-visible feature. The `VIBRATE` permission must be declared in `AndroidManifest.xml` for the OS to allow vibration calls. Unlike `INTERNET` (which Firebase merges automatically from its own manifest), `VIBRATE` is not merged from any dependency — it must be declared explicitly.

On Android 13+, an undeclared `VIBRATE` permission causes the vibration call to be silently ignored. The user-facing impact is that the vibration setting appears to do nothing.

---

## Task

Add the permission to `app/src/main/AndroidManifest.xml`, before the `<application>` tag:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

No code changes required.

---

## Acceptance Criteria

- [ ] `<uses-permission android:name="android.permission.VIBRATE" />` is present in `AndroidManifest.xml`.
- [ ] Project builds without errors (`./gradlew build`).
- [ ] Vibration works on a physical device when the setting is enabled.

---

## Files to Modify

- `app/src/main/AndroidManifest.xml` — add `VIBRATE` permission

---

## Notes

`VIBRATE` is a normal permission (not dangerous) — no runtime permission dialog is shown to the user. It simply needs to be declared.
