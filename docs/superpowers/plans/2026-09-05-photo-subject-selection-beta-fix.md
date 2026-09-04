# Photo Subject Selection Beta Fix Implementation Plan

> **For agentic workers:** Execute this plan inline, task-by-task, with the
> existing TDD and verification gates. Do not delegate the implementation.

**Goal:** Make the manual photo-subject picker reliably receive long-presses
inside the modal sheet, resolve the intended candidate, and clearly label the
experimental interaction as Beta.

**Architecture:** Keep ML Kit inference and bitmap ownership unchanged. Extract
gesture decisions into a small pure policy, use a custom pointer detector for
manual mode that observes the initial pointer pass (so the sheet/scroll parent
cannot hide the down event), and resolve a long press through confidence-first
hit testing with a conservative bounds fallback for thin/edge masks.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Compose pointer input,
Google ML Kit Subject Segmentation, JUnit 4.

## Global Constraints

- Preserve the existing Auto detect flow, clipboard import, save/copy flow, and
  lifecycle/bitmap recycling behavior.
- Use only Kotlin and Jetpack Compose; no new dependency or permission.
- Keep all UI text in English, use Lucide icons, and do not add emoji.
- The manual mode remains opt-in and is labeled `BETA`.
- Run tests/lint and the signed Release/R2/ntfy delivery workflow after the
  completed Android change.

---

### Task 1: Lock manual gesture and relaxed edge-hit behavior with tests

**Files:**
- Modify: `app/src/test/java/com/hkm/stickhub/CutoutSelectionPolicyTest.kt`
- Create: `app/src/test/java/com/hkm/stickhub/CutoutGesturePolicyTest.kt`

**Interfaces:**
- `CutoutGesturePolicy.shouldSelectOnTap(mode: CutoutInteractionMode)` returns
  `true` only for Auto mode.
- `CutoutGesturePolicy.shouldSelectOnLongPress(mode: CutoutInteractionMode)`
  returns `true` for both modes.
- `CutoutSelectionPolicy.selectForManualLongPress(candidates, point)` returns
  the confidence-selected candidate, or a containing candidate when the point is
  inside a subject bounds but the sampled edge confidence is below the normal
  threshold.

- [ ] **Step 1: Write failing tests**

Add tests that assert manual taps are not accepted, long presses are accepted,
and a manual press near a valid subject edge still resolves to that candidate
when its sampled confidence is below `MIN_CONFIDENCE` but above a relaxed
fallback threshold. Keep the existing background rejection test unchanged.

- [ ] **Step 2: Run the targeted tests and verify the expected red failure**

Run:
`$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat :app:testDebugUnitTest --tests com.hkm.stickhub.CutoutGesturePolicyTest --tests com.hkm.stickhub.CutoutSelectionPolicyTest --no-daemon --max-workers=1 --console=plain`

Expected: compilation fails because the new policy entry points do not yet
exist.

### Task 2: Implement pure gesture/selection policies

**Files:**
- Create: `app/src/main/java/com/hkm/stickhub/data/cutout/CutoutGesturePolicy.kt`
- Modify: `app/src/main/java/com/hkm/stickhub/data/cutout/CutoutSelectionPolicy.kt`

**Interfaces:**
- `CutoutGesturePolicy.shouldSelectOnTap` and
  `CutoutGesturePolicy.shouldSelectOnLongPress` as defined in Task 1.
- `CutoutSelectionPolicy.selectForManualLongPress` first uses the existing
  confidence ranking, then allows only candidates whose point is inside their
  normalized bounds and whose sampled confidence is at least `0.12f`; ties use
  smallest area then stable id.

- [ ] **Step 1: Implement the smallest policy bodies**

Reuse `selectAtNormalizedPoint` for the confidence-first path. Add a private
relaxed hit ranking only for manual long press; never change the strict method
used by existing tests or Auto behavior.

- [ ] **Step 2: Run targeted tests and verify green**

Run the command from Task 1. Expected: all policy tests pass.

### Task 3: Replace the fragile manual pointer detector and add Beta label

**Files:**
- Modify: `app/src/main/java/com/hkm/stickhub/ui/components/SubjectCutoutSheet.kt`

**Interfaces:**
- `CandidatesSelectionView` receives the existing
  `CutoutInteractionMode` and calls `CutoutGesturePolicy` for tap behavior.
- Manual mode uses `awaitEachGesture` + `awaitFirstDown(requireUnconsumed =
  false, pass = PointerEventPass.Initial)` + `awaitLongPressOrCancellation`.

- [ ] **Step 1: Use the initial pointer pass for manual mode**

Keep the current `detectTapGestures` path for Auto mode. In Manual mode, read
the down event even if the sheet/scroll parent consumed it, animate the pressed
candidate, wait for long-press cancellation, and call
`selectForManualLongPress` only after the long-press threshold. A short release
does nothing; a background long press calls the existing reject callback.

- [ ] **Step 2: Add the Beta label without changing default mode**

Render `BETA` as a small Material 3 label inside the “Choose subject” FilterChip
and include it in the helper copy. Keep Auto detect selected by default.

- [ ] **Step 3: Run the complete test suite and lint**

Run:
`$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat :app:testDebugUnitTest :app:lint --no-daemon --max-workers=1 --console=plain`

Expected: BUILD SUCCESSFUL with no new lint errors.

### Task 4: Release verification and checkpoint

**Files:**
- Modify: `app/build.gradle.kts` only if the version must be incremented from
  the current 5.2.0/52 artifact.

- [ ] **Step 1: Build and verify the signed Release APK**

Use the configured HKM keystore environment variables, run
`:app:assembleRelease`, verify with `apksigner`, and record the final size and
SHA-256 without printing credentials.

- [ ] **Step 2: Upload and verify delivery**

Upload the completed APK to the configured Personal R2 `filesend` object,
verify its public URL with HEAD and matching content length, then send exactly
one ntfy message containing the APK name, final URL, and complete changelog.

- [ ] **Step 3: Commit the checkpoint**

Run `git diff --check`, commit the scoped change as
`fix: stabilize beta subject selection`, and confirm the worktree is clean.
