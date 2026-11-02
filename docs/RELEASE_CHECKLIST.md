# Release Checklist — SALi-HCNSEC

Complete every item before publishing a release. A release that skips an item must be labelled a test artifact, not a commercial release.

## 1. Code state
- [ ] main is green in CI (tests + assembleRelease + signature verification).
- [ ] No known P0 issues open against the release.
- [ ] versionCode and versionName bumped in app/build.gradle.kts.
- [ ] CHANGELOG.md updated for this version.

## 2. Signing identity
- [ ] RELEASE_KEYSTORE_BASE64, STORE_PASSWORD, KEY_PASSWORD, KEY_ALIAS secrets are configured and unchanged since the previous release (stable identity = in-place updates).
- [ ] CI build report shows the expected signature; the APK verifies with apksigner.

## 3. Verification
- [ ] Unit tests pass on CI; coverage report reviewed against the threshold.
- [ ] Android lint reviewed (must be blocking once the AGP/KSP crash is fixed).
- [ ] Release APK installed over the previous release version (in-place update path verified).
- [ ] Manual smoke test: add provider key → send chat → stream stops correctly → import ZIP → consent dialog → analysis → derived workspace.

## 4. Store / distribution
- [ ] Store listing (title, description, screenshots EN + FA) updated.
- [ ] Data safety form reconciled with PRIVACY.md (1:1, no drift).
- [ ] Age rating and content questionnaire updated.
- [ ] Privacy policy URL reachable (PRIVACY.md rendered).
- [ ] Release notes drafted from CHANGELOG.md.

## 5. Rollout
- [ ] Staged rollout percentages chosen (10 → 50 → 100) with health criteria per stage.
- [ ] A rollback plan exists (previous APK re-publishable, keystore intact).
- [ ] Post-release monitoring plan active (GitHub issues + crash channel if observability is enabled).
