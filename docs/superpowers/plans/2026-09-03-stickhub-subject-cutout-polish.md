# StickHub Subject Cutout and Polish Implementation Plan

> **For agentic workers:** Execute the phases in order. Build and test after each phase. This checkout has no Git metadata, so do not attempt commits or resets.

**Goal:** Replace the broken opaque-image import heuristic with an on-device subject-cutout experience, then make the local sticker library safe, responsive, and consistently polished.

**Architecture:** Keep StickHub local-first. Add a narrowly scoped ML Kit `SubjectCutoutProcessor` that installs and invokes the optional Google Play services model, while UI state remains in a dedicated cutout sheet. Refactor storage/backup around validated metadata and atomic file writes; keep haptics, motion, and Lucide icon choices centralized rather than scattered through screens.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Coil 2.7, SQLiteOpenHelper, ML Kit Subject Segmentation `16.0.0-beta1`, Composable Horizons Lucide Android icons `2.2.1`.

## Global Constraints

- Minimum SDK stays 24; target SDK stays 35 unless an API guard requires no configuration change.
- Subject segmentation runs on-device after a one-time Google Play services model download; do not upload images or add a backend.
- Do not use `ImageAlphaUtil`'s white-background flood-fill for opaque photos.
- Keep transparent input transparent; do not run it through segmentation or white removal.
- No emoji in UI. All authored UI icons are Lucide; remove Material Icons Extended.
- Use semantic platform haptics, never `Vibrator`/one-shot vibration for routine touch feedback.
- Do not auto-seed stickers after a user deletes/imports their library.
- Never add signing secrets to project files, logs, documentation, or responses.
- Preserve manual `.stickhub` backup; disable Android Auto Backup to make the privacy claim true.

---

## Locked File Structure

- Modify: `app/build.gradle.kts` — dependencies and secure release-signing configuration only.
- Modify: `app/src/main/AndroidManifest.xml` — ML Kit prefetch metadata, disable Auto Backup, remove obsolete vibration permission.
- Modify: `gradle/libs.versions.toml` only if aliases are used for the two new dependencies.
- Create: `app/src/main/java/com/hkm/stickhub/data/cutout/SubjectCutoutProcessor.kt` — model availability, segmentation, callback/result mapping, no Compose UI.
- Create: `app/src/main/java/com/hkm/stickhub/data/cutout/CutoutModels.kt` — stable result and error types.
- Create: `app/src/main/java/com/hkm/stickhub/util/BitmapDecodeUtil.kt` — bounded, orientation-correct, ARGB bitmap decode and alpha detection.
- Create: `app/src/main/java/com/hkm/stickhub/util/StickerFileSafety.kt` — pure filename/canonical-path validation shared by the provider and import code.
- Create: `app/src/main/java/com/hkm/stickhub/ui/components/SubjectCutoutSheet.kt` — source preview, shimmer candidate affordance, hold-to-select, metadata and save state.
- Create: `app/src/main/java/com/hkm/stickhub/ui/theme/StickHubMotion.kt` — centralized motion specs and press-state modifier.
- Create: `app/src/main/java/com/hkm/stickhub/ui/haptics/StickHubHaptics.kt` — Compose semantic haptic facade.
- Modify: `StickerRepository.kt`, `StickHubDbHelper.kt`, `BackupHelper.kt`, `StickerContentProvider.kt`, `ClipboardHelper.kt`, `OverlayService.kt`, `StickHubApp.kt`, all authored Compose component files, and the two overlay drawable assets.
- Add focused unit tests for haptic policy/file-name validation/backup metadata round trip. Replace template tests; do not claim device coverage that was not run.

## Phase 1: Safety and Data Correctness

1. Remove hard-coded release credentials. Use four environment variables (`STICKHUB_STORE_FILE`, `STICKHUB_STORE_PASSWORD`, `STICKHUB_KEY_ALIAS`, `STICKHUB_KEY_PASSWORD`) only when all are present; debug must build when none are present. Add the local secret file pattern to `.gitignore`; do not create a secret file.
2. Set `android:allowBackup="false"`. Add ML Kit model prefetch metadata under `<application>`.
3. Raise DB version to 2. Replace mutation-in-`onOpen` with an idempotent transactional v1-to-v2 migration: merge legacy category labels by moving sticker rows first, ensure the destination category, then remove legacy rows.
4. Make category deletion transactional: move all its stickers to `General`, then delete only non-default category. Remove automatic starter seeding entirely.
5. Restore all backup metadata (`isFavorite`, `createdAt`, `usageCount`) and increment import count only after storage plus database insertion succeeds. Validate ZIP manifest/version and entry names in a staging directory, constrain entry count/size, cleanup in `finally`, and merge only valid content.
6. Validate provider URIs with an exact `UriMatcher`, safe basename and canonical-root check. Preserve an exported read-only provider only because inter-app clipboard needs it; do not expose paths outside `files/stickers`.

## Phase 2: On-Device Subject Cutout

1. Add ML Kit Subject Segmentation dependency and `SubjectCutoutProcessor`; process static image input off the main thread. Explicitly check/request the Play services module and represent downloading, analyzing, ready, empty, unavailable, and failure states.
2. Decode a source bitmap no larger than 2048 px on its longest side, fix EXIF orientation, and reject unreadable/too-small images gracefully. Respect the ML Kit 512 px quality floor.
3. For opaque photos, request multi-subject confidence masks. Build each selected cutout in ARGB_8888 from original pixels plus a soft alpha mask, crop with padding, and save as PNG. Do not blindly persist a model-supplied bitmap without alpha verification.
4. For inputs containing actual transparency, bypass segmentation and import the bounded transparent bitmap unchanged.
5. Add a single composable cutout sheet. It must show a fit source preview and subtle shimmer/outline candidate affordance once ready. Long press inside a candidate chooses it; map touch coordinates through the displayed image bounds. After selection, animate into a checkerboard transparent preview, then collect title/category/tags and save. With no candidate, give clear retry/choose-another-photo UI; never silently save the opaque original.
6. Use a monotonically increasing request token and cancellation checks so a previous URI or model callback cannot overwrite a newer selection.

## Phase 3: Performance and Clipboard/Overlay Reliability

1. Route all new imported/cutout saves through bounded decode and atomic write helpers. Never read whole images into memory or mutate original files in place.
2. Make Sticker Studio decode and bitmap transforms IO/default-dispatcher work. Debounce caption/slider work, cancel stale jobs, replace preview only when ready, and release intermediate bitmaps safely. Keep the final output bounded.
3. Replace synchronous overlay bitmap decoding with Coil/ImageLoader or an equivalent sampled thumbnail pipeline. Debounce search, limit displayed items, and only mutate native views on Main. Guard rapid panel open/close with generation/state and clamp draggable positions on-screen.
4. Use one safe content URI for clipboard. Show app snackbar feedback only on Android 12L and below because Android 13+ supplies its own copy preview.

## Phase 4: Design System — Haptics, Icons, and Motion

1. Remove `HapticUtil` and `VIBRATE`. Implement `StickHubHaptics` using `View.performHapticFeedback` / `LocalHapticFeedback`, API guards, and no global-setting override.
2. Map events: long press -> LongPress; toggle -> ToggleOn/ToggleOff (fallback virtual key); successful copy/save/import/export -> Confirm; genuine failure -> Reject where available; discrete slider/category movement -> one SegmentTick/CLOCK_TICK. Never haptic close, clear, typing, every click, or an action before it succeeds.
3. Add only `com.composables:icons-lucide-android:2.2.1`, remove Material Icons Extended, and replace all authored Compose/native overlay icons with matching Lucide iconography. Use local archive icons, not cloud imagery, for offline backup.
4. Centralize motion. Use short, restrained effects: press scale 0.97-ish, no large bouncy 0.88 scale. Animate selection top bar, chips, clipboard banner, empty/grid state, cutout progress/result, sheet internals and overlay. Apply `Modifier.animateItem()` in the grid. Respect the system animator duration scale; do not create perpetual decorative motion.
5. Remove duplicate `FilterChip` plus `combinedClickable` click handlers; every interaction has exactly one semantic handler.

## Phase 5: Verification

1. Run `./gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon --max-workers=1` using Android Studio JBR on Windows. Fix every failure.
2. Inspect source to prove no `androidx.compose.material.icons`, `Icons.Default`, `Vibrator`, `VibrationEffect`, hard-coded signing credential, or emoji UI remains.
3. On a physical GMS Android device, record results for: person, pet/object, two subjects, white subject on white background, transparent PNG, EXIF-rotated JPEG, large photo, first download offline, model-ready offline, no candidate, cancel/reselect race, copy, overlay search/open-close, backup metadata restore, and delete-category reassignment.
4. Report exact changed files, commands/results, device coverage, known ML Kit beta/GMS limitation, and anything not verified. Do not claim full completion from compilation alone.
