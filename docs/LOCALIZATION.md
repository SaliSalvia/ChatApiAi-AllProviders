# Localization & Internationalization Guide

SALi-HCNSEC uses a **dependency-free localization layer** (no Android strings.xml per-locale resources for the product UI). This document explains how it works and how to add a language correctly.

## How the current layer works

- All user-facing product strings live in app/src/main/java/com/example/ui/localization/AppStrings.kt.
- An AppStrings data class declares one property per UI string. Lambda properties (e.g. connectedModels, steps, modelCount) handle pluralization.
- Two instances exist today: englishStrings (base, complete) and persianStrings, built with englishStrings.copy(...) so any untranslated key falls back to English.
- The active language is an AppLanguage enum in the settings layer. rememberAppStrings(language) returns the right instance, and LocalAppStrings is the CompositionLocal the whole UI reads.
- Layout direction (RTL/LTR) follows the language automatically; the UI is Compose-only, so there are no XML layouts to mirror.

## Adding a new language (step by step)

1. **Add the enum value** in AppLanguage (settings layer), e.g. ARABIC.
2. **Create the strings instance** in AppStrings.kt: arabicStrings = englishStrings.copy(language = AppLanguage.ARABIC, ...). Translate **every** property; anything not overridden silently falls back to English, so review the diff for missed keys.
3. **Wire the picker**: add the language to the Settings language selector and add its native display name (the language labels themselves are per-language properties).
4. **Verify RTL**: run the app with the new language and check navigation, dropdowns, message bubbles, the reasoning accordion, and the markdown renderer direction.
5. **Add screenshot tests** for at least: home/chat empty state, settings, model picker — in the new language (Roborazzi supports this).
6. **Have a native speaker review** the translations before release; machine translation is a draft, not a deliverable.

## Translation rules for this codebase

- API prompts, provider names, and user-created content are intentionally **not** translated.
- Keep strings short: they must fit the existing compact Material 3 layout.
- Use the lambda properties for anything count-like; never concatenate numbers into fixed strings.
- Do not embed markdown or HTML in translated strings.
- Preserve the brand names (Salvia-H.Ai, SalarSalvia) untranslated.

## RTL / locale quality checklist (per language, per release)

- [ ] Direction mirrors correctly in lists, drawers, and the model picker.
- [ ] Punctuation and mixed-direction text render sensibly.
- [ ] Dates/numbers shown to the user respect the locale.
- [ ] Font fallback covers all glyphs of the new language on minSdk 24 devices.
- [ ] Screenshots in the store listing exist in the new language.
- [ ] PRIVACY.md language note updated if the language introduces region-specific requirements.

## Priority order for scale-out (from the product roadmap)

1. English (base) — done.
2. Persian (فارسی) — done.
3. Arabic — highest leverage next (RTL infrastructure already proven).
4. Turkish / Indonesian — largest Android growth markets for this category.
