# First-Run Onboarding Spec

Target: take a brand-new user from install to **first successful streamed answer** in under 3 minutes, with zero confusion about privacy or what they need to bring (their own API key).

## Design principles

1. **Local-first is a feature, say it once clearly** — "No account. Your keys stay encrypted on this device."
2. **Never block exploration** — the user can enter the app and read everything before adding any key; setup is skippable.
3. **First success within one screen** — the moment a key validates and models load, show immediate confirmation.
4. **Fail with a way forward** — every provider error message must contain one concrete next step (wrong key, no network, provider quota).

## Flow

**Step 1 — Welcome (new)**
- One sentence of value: provider-neutral AI workspace, all your models in one place.
- Language selector visible immediately (EN/فارسی) — not buried in Settings.
- "Get started" → Step 2.

**Step 2 — Privacy notice (new, lightweight)**
- Three bullets, not a wall of text: keys encrypted with Android Keystore · no account · nothing leaves the device except requests you make.
- Link to PRIVACY.md.
- Continue → Step 3.

**Step 3 — Provider setup (existing, polish)**
- Recommended free-tier provider card first, then the full provider grid.
- Each provider row: logo, one-line description, "Get a key" deep link to the provider's console.
- Paste key → inline validation with the existing validating/connectedModels strings.
- On success: models count toast + "Start chatting" CTA.
- "Skip for now" always available → home in read-only mode with a persistent, non-blocking "Connect a provider" banner.

**Step 4 — First success action**
- Pre-seeded suggested prompts (analyze a project ZIP, solve a hard problem, refactor code) — the existing empty-state cards.
- First streamed answer must include the reasoning accordion in collapsed state (curiosity hook) and the provider/model used (transparency hook).

**Step 5 — Retention hooks (existing behaviors to surface)**
- Pinned conversations, workspace list, export-as-Markdown.
- A returning-user state: "Continue where you left off" on the home screen.

## States that must be designed explicitly

| State | Requirement |
| --- | --- |
| No provider yet | Read-only mode + skippable banner, no dead-ends |
| Invalid key | Inline error + link to provider console |
| Network offline | Retry with countdown, queued draft preserved |
| Provider quota / 429 | Plain-language explanation + which provider was hit |
| RTL | Entire flow must mirror, tested for FA |

## Metrics (once opt-in analytics exist)

- % completing Step 3 within first session.
- Time to first streamed answer.
- % of sessions reaching first success without an error.
- 7-day return rate of users who completed Step 3 vs. skipped.

## Acceptance criteria

- A new user with no key can use the app read-only and is never blocked.
- A user with a key reaches a streamed answer in <= 3 taps after paste.
- Every error state names the provider and gives one actionable next step.
- The flow is fully localized (EN/FA) and RTL-correct from day one.
