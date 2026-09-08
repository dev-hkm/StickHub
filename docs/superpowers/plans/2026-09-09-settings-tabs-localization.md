# Settings Tabs and Localization Implementation Plan

**Goal:** Reorganize StickHub Settings into compact modern tabs and add a persistent English/Tiếng Việt switch without breaking existing setting callbacks or decorative themes.

**Architecture:** Keep `SettingsScreen` as the single stateful settings surface, add a small local language preference and typed UI string resolver, and gate the existing LazyColumn sections by a selected tab. Use Material 3 `ScrollableTabRow`, animated tab content, and safe text wrapping instead of nested scrolling or a second settings navigation stack.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, SharedPreferences, existing Lucide icons and theme typography.

## Global Constraints

- Preserve all existing settings callbacks and persisted values.
- Default language remains English; Vietnamese is opt-in and local-only.
- No new runtime permission or dependency.
- Keep all existing visual themes; use system/Ubuntu fallback for missing Vietnamese glyphs.
- No emoji; use existing Lucide icons.

### Task 1: Language preference and strings boundary

**Files:**
- Create: `app/src/main/java/com/hkm/stickhub/ui/i18n/AppLanguage.kt`
- Create: `app/src/main/java/com/hkm/stickhub/ui/i18n/StickHubStrings.kt`
- Modify: `app/src/main/java/com/hkm/stickhub/ui/StickHubApp.kt`

- [ ] Add `AppLanguage { ENGLISH, VIETNAMESE }`, `LanguagePreferences.get/set`, and a `StickHubStrings` value object with stable labels for settings tabs, header, language selector, theme controls, and common actions.
- [ ] Load the preference at app start, pass it to `StickHubApp`, and persist changes locally without changing system locale.
- [ ] Keep the resolver pure so Compose recomposes immediately and falls back to English for any untranslated key.

### Task 2: Tabbed Settings shell

**Files:**
- Modify: `app/src/main/java/com/hkm/stickhub/ui/components/SettingsScreen.kt`

- [ ] Add `SettingsTab` with General, Quick Stickers, Library, and Backup & Privacy labels.
- [ ] Add a `ScrollableTabRow` below the header with animated indicator, haptic tick, and `AnimatedContent` for the selected body.
- [ ] Preserve one `LazyColumn` per tab using remembered scroll states; never nest a LazyColumn.
- [ ] Keep the back button and edge-to-edge insets intact.

### Task 3: Move existing sections behind tabs

**Files:**
- Modify: `app/src/main/java/com/hkm/stickhub/ui/components/SettingsScreen.kt`

- [ ] Gate current Appearance/theme and library display controls behind General.
- [ ] Gate overlay bubble, popup appearance, category/search/title visibility, start filter, and after-copy controls behind Quick Stickers.
- [ ] Gate WhatsApp packs, category management, export/import, and stats behind Library.
- [ ] Gate cloud backup and privacy cards behind Backup & Privacy.
- [ ] Do not alter any existing callback implementation or persistence.

### Task 4: Localized settings copy and font safety

**Files:**
- Modify: `app/src/main/java/com/hkm/stickhub/ui/components/SettingsScreen.kt`
- Modify: `app/src/main/java/com/hkm/stickhub/ui/theme/Type.kt`

- [ ] Replace visible settings headings, tabs, language controls, action labels, and common descriptions with `StickHubStrings` values.
- [ ] Add a Vietnamese-safe fallback `FontFamily` using the existing Ubuntu/system fonts for strings containing Vietnamese diacritics when a decorative theme font lacks glyph coverage.
- [ ] Apply `maxLines`, `softWrap`, `TextOverflow`, and flexible row weights to prevent clipping in narrow screens and long Vietnamese labels.

### Task 5: Verification and release

**Files:**
- Modify: `app/build.gradle.kts` version metadata.

- [ ] Build signed Release with the configured HKM keystore.
- [ ] Verify no new dependency or permission was introduced and inspect Git diff for accidental callback changes.
- [ ] Commit the implementation, upload the APK to Personal R2, verify its public `HEAD`, then send exactly one `filesend` ntfy message.
