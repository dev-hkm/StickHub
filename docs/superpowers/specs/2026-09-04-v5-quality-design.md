# StickHub 5.0 — reliability and interaction release

## Decision and boundaries

User authorized autonomous design, implementation, validation and delivery. Preserve the installed package, SQLite schema/data, clipboard copy/import, cutout creation, 19 light/dark themes and View-based system overlay. Work incrementally in the existing checkout; do not convert the overlay or database framework. No analytics, account, cloud runtime, new theme, or sticker-pack sharing in this release. Untracked design files 03–09 remain user-owned.

Chosen approach: data-safety first plus focused interaction upgrades. A wholesale rewrite would expand regression risk; adding more themes would not address the observed failures. Checkpoint `checkpoint/pre-v5-3.2.2` retains the original source.

## Acceptance contract

1. Successful database commits must never lose their image because snapshot refresh or caller cancellation fails. Edits must preserve the previous image on storage errors. Backup restore validates before mutating, bounds metadata and image expansion, preserves format, metadata and manual order, and accepts its own exported supported libraries. Existing stickers never disappear during merge.
2. Overlay image work is bounded/cancellable and stale results cannot target replaced content. Layout is clamped and recalculated after closed/open rotations and resizing. Opacity layers remain independent. Live dragging previews values without per-frame preferences writes or grid rebuilding; release persists the final value. Temporary invisible controls remain recoverable.
3. Settings gains ready-to-use appearance presets (Balanced, Floating stickers, Discreet), explicit independent layer controls and honest apply-on-release shadow copy. Existing custom values remain unchanged until user acts. Controls are labeled for accessibility and use existing theme tokens/Lucide icons.
4. Clipboard observation only runs while the app is resumed, with an immediate recheck on return. Permission and overlay running state are reconciled on resume. Navigation and long operations do not turn cancellation into a user-facing failure.
5. Cutout save prevents duplicate taps, retains the candidate on failure, and dismisses only after successful save. Stale segmentation results cannot replace newer requests. Very narrow/large images respect a bounded model input allocation. Tag drag survives order updates and cancels without committing accidental changes.
6. Add regression tests that fail on the reported behaviors, not just constants/source snippets. Use JVM tests and Robolectric for Android data behavior where possible. No emulator/ADB requirement. Fresh lint and signed Release build plus delivery verification are mandatory; no physical-device smoothness claims without evidence.

## Architecture

Keep repositories as the durable source of truth. Extract small pure policies for preview coalescing, archive safety and drag/request state as appropriate. Transient preview travels to the running service through explicit intent payloads and does not touch persistent preferences. Service retains its own lifecycle-owned jobs. Settings owns local slider state and only publishes committed values to the parent.

## Delivery

Version 5.0.0, versionCode above 44, same applicationId/signature. Build Release only (JVM task named testDebugUnitTest is not a Debug APK). Verify signing and APK metadata, upload to the configured private-credential Personal R2 bucket, verify public download, send one ntfy with short changelog. Commit tested changes and record evidence/remaining device checks.
