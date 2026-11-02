# Product Readiness Scorecard — SALi-HCNSEC

_Assessment date: 2026-11-02 · Scope: repository state as observed on main_

## Overall verdict

**Beta — approaching Production-ready.** The strongest of the four mobile repos. Core product mechanics (multi-provider streaming, secure key storage, workspace analysis, CI) are in place. The remaining gaps are: blocking lint, evidence of coverage numbers, store listing assets, an onboarding polish pass, and opt-in observability.

| Dimension | Score | Status |
| --- | --- | --- |
| UX/UI (Material 3, edge-to-edge, dark theme, streaming) | 8/10 | Good |
| Automated tests (unit + Robolectric + Roborazzi screenshots) | 6/10 | Present, coverage unproven |
| CI/CD (build, tests, signed APK, release notes) | 7/10 | Good; lint not blocking yet |
| Security & privacy (Keystore, consent, redaction, no tracking) | 9/10 | Excellent; policies just added |
| i18n (EN/FA, RTL, in-app switching) | 7/10 | Good; scale-out plan needed |
| Onboarding & first-run UX | 5/10 | Functional; needs a polish pass |
| Analytics & observability | 3/10 | Deliberately absent; needs opt-in plan |
| Performance (R8, batched streaming, precompiled markdown) | 8/10 | Good, tuned |
| App store readiness | 4/10 | Legal docs now exist; listing assets missing |
| Docs & contributor experience | 8/10 | Strong README + roadmap; templates being added |
| Safety & moderation (consent, key isolation, output framing) | 7/10 | Good for a BYOK app |
| Release identity (stable signing) | 5/10 | Ephemeral keys unless secrets configured |

## Evidence-based notes

- CI runs blocking unit tests and a signed, minified release APK with verification (apksigner, SHA-256, build report) — see .github/workflows/build-apk.yml.
- Android lint is currently diagnostic-only due to an AGP/KSP toolchain crash; it must become blocking before any commercial release.
- No coverage report is published; 70–80% target on core logic is not yet verifiable.
- versionCode 2 / versionName 1.1 — versioning exists but there is no changelog discipline yet.
- No LICENSE file at the repository root — licensing must be chosen before any public distribution claim.

## 30 / 60 / 90-day plan to Production + Global-ready

### Days 0–30 — Release hygiene
1. Configure the four stable signing secrets (RELEASE_KEYSTORE_BASE64, STORE_PASSWORD, KEY_PASSWORD, KEY_ALIAS) so updates install in place.
2. Make Android lint blocking (fix the AGP/KSP crash first).
3. Publish a coverage report to CI; establish a baseline and a minimum threshold for core logic.
4. Add CHANGELOG.md with Keep a Changelog discipline; tie version bumps to releases.
5. Choose and add a LICENSE.
6. Screen-test matrix: minSdk 24 low-end device pass, RTL pass, TalkBack pass, font-scaling pass.

### Days 31–60 — Product polish
1. First-run onboarding pass per docs/UX_ONBOARDING_SPEC.md (privacy notice → provider setup → first successful answer).
2. Complete empty/loading/error/retry/cancellation states across the workspace flows.
3. Add UI regression tests for the top 5 user journeys (chat send, provider add, ZIP import, version compare, settings language switch).
4. Release as **public beta** to a closed testing group with a feedback channel.

### Days 61–90 — Global readiness
1. i18n scale-out per docs/LOCALIZATION.md (Arabic, Turkish, and one more target-market language) with native-quality review.
2. Opt-in, privacy-respecting observability: crash reporting that users can enable in Settings, off by default, documented in PRIVACY.md.
3. Google Play listing: screenshots, description, age rating, Data safety form (mapped 1:1 from PRIVACY.md), Play Integrity.
4. Staged production rollout (10% → 50% → 100%) with per-stage health checks.

## Scoring rubric

- **Prototype**: core idea works in ideal conditions.
- **MVP**: main user path is stable; edge cases unhandled; no release discipline.
- **Beta**: feature-complete for the target use case; CI and tests protect regressions; known polish gaps.
- **Production-ready**: store-distributable with stable identity, observability, support and legal docs.
- **Global product-ready**: production-ready + localized, accessibility-audited, with per-region performance and compliance awareness.
