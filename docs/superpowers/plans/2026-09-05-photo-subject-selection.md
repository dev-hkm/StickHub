# Photo subject selection plan

## Goal

Extend the existing device-photo cutout sheet with two explicit interaction modes:

- `Auto detect`: preserve the current behavior and automatically open the first
  detected subject in the existing sticker details/save flow.
- `Choose subject`: keep the complete source photo visible and let the user press
  and hold a desired object. The touch is mapped into source-image normalized
  coordinates and resolved against ML Kit's confidence masks, then the selected
  candidate enters the existing details/copy/save flow.

The feature must reuse the current on-device ML Kit subject-segmentation pass and
must not add a second model, network dependency, or alter clipboard/import flows.

## Implementation surface

- Add a pure `CutoutSelectionPolicy` in `data/cutout` that maps a normalized point
  to the best candidate using confidence, smallest containing area, and stable id
  tie-breakers.
- Add unit tests for outside points, confidence-mask rejection, overlapping
  candidates, and normalized touch-coordinate mapping.
- Add a `CutoutInteractionMode` state to `SubjectCutoutSheet`, with a compact
  Material 3 selector. Default to `Auto` so existing users see the same flow.
- In manual mode, do not auto-adopt the first candidate. Render the full source
  image, require a long press, and resolve the press through the pure policy.
  Keep the existing subtle press animation, haptic success/reject callbacks, and
  details form unchanged after selection.
- Preserve lifecycle/recycling/request-gate behavior and all current fallback
  states. Do not introduce a persistent setting unless required by the existing
  architecture.
- Bump the app version for the feature, run unit tests/lint/release build, and
  follow the standing signed-release delivery workflow after implementation.

## Verification

1. Run the new selection-policy test while it is intentionally red.
2. Implement the policy and make the targeted test green.
3. Run the complete unit-test suite and lint.
4. Assemble the signed Release APK, verify signing, upload it to the configured
   R2 public URL, and send the single required ntfy delivery message.
5. Inspect the final diff and confirm no clipboard or sticker-storage behavior
   was removed.

## Acceptance criteria

- Picking a photo with one or more detected subjects still auto-opens the first
  subject exactly as before when `Auto detect` remains selected.
- Selecting `Choose subject` before or after analysis leaves the source photo
  visible; a short tap alone does not select a subject.
- Pressing and holding inside a subject's confident mask selects that subject,
  shows the existing sticker preview/details UI, and allows copy/save.
- Pressing and holding on background or a low-confidence mask produces only the
  existing subtle reject feedback and never selects an unrelated subject.
- Overlapping candidates resolve to the candidate with the strongest confidence
  at the actual touch point; ties prefer the smaller region and then stable id.
- Changing mode is animated through the existing content transition and does not
  leak or recycle a bitmap still in use.
