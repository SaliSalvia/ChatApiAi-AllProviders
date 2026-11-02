# Security Policy — SALi-HCNSEC

## Supported versions

The main branch is the only supported version. Release APKs are produced from main by the CI workflow.

## Security architecture (what is already in place)

- **API keys**: encrypted at rest with AES-GCM via the Android Keystore; never bundled in the APK; never sent to a provider other than the one they belong to.
- **No secrets in source**: provider keys are entered by the user at runtime; the repository only ships an .env.example placeholder, which CI treats as build-config defaults, not credentials.
- **Remote context consent**: before any project context is sent to a provider, the app requires explicit confirmation and applies best-effort credential-path omission and inline-secret redaction.
- **Archive safety**: imported ZIPs are protected against Zip Slip path traversal, oversized entries/archives, and excessive entry counts.
- **Network**: custom provider endpoints must be HTTPS.

## Reporting a vulnerability

Please report vulnerabilities by opening a **private security advisory** in this repository (Security tab → Report a vulnerability). Include:

1. Affected component and build (commit SHA or version name).
2. Step-by-step reproduction or proof of concept.
3. Impact assessment.

Please do **not** open public issues for exploitable vulnerabilities.

## Scope

In scope: the Android app in this repository, its CI workflow configuration, and the signing/release process.
Out of scope: third-party provider APIs, social engineering, physical attacks, and abuse of provider free tiers.

## Commitment

Reports will be acknowledged, and fixes prioritized for the next release cycle. Security fixes are noted in release notes without exposing exploitation details.
