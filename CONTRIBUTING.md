# Contributing to SALi-HCNSEC

Thanks for your interest in improving SALi-HCNSEC. Before opening a PR, please read docs/PRODUCT_QUALITY_ROADMAP.md — especially the product principles (evidence before conclusions, privacy and explicit consent, fail visibly and safely). Contributions that violate them will not be merged.

## Setup

1. Clone and open the project in **Android Studio**.
2. Toolchain: **JDK 21**, Gradle 9.3.1, AGP 9.1.1, compileSdk 36, minSdk 24.
   - The repository intentionally has no gradlew wrapper jar; CI provisions Gradle directly.
3. Copy .env.example to .env if you need local build-config defaults. **Never commit real API keys** — the runtime app asks the user for keys, and source must stay secret-free.

## Building and testing

- Debug build: gradle :app:assembleDebug
- Unit tests (blocking in CI): gradle :app:testDebugUnitTest
- Lint: gradle :app:lintDebug (currently diagnostic due to a toolchain issue)
- Release build: gradle :app:assembleRelease (falls back to the debug keystore locally)

## Ground rules

1. **Small, single-purpose PRs.** One concern per PR makes review possible.
2. **Tests come with the code.** New logic in workspace handling, diffs, routing, provider clients, or security needs a unit test; new screens need a Roborazzi screenshot test.
3. **No behavior regressions on privacy**: key handling, consent dialogs, and redaction logic are protected — changes there need explicit justification in the PR.
4. **Follow the architecture**: data/domain/ui packages (rough Clean Architecture). UI code goes in ui/, provider contracts in data/api, routing decisions in domain/router.
5. **Localization**: every new user-facing string must be added to the AppStrings data class and to both language instances (see docs/LOCALIZATION.md).
6. **Commit messages**: short imperative subject line (e.g. "fix: guard ZIP entry-count limit"). Scope prefixes: fix, feat, docs, test, chore.
7. **Draft PRs are fine** early; mark them clearly and ask for targeted feedback.

## Reporting bugs

Use the bug-report issue template. Always include: app version (versionName), Android version, device, provider involved, and logs with **secrets removed**.

## Security issues

Do not open public issues for vulnerabilities — see SECURITY.md.
